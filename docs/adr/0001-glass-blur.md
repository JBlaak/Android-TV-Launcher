# Frosted glass: our own ⅛-resolution sky on Android 11, RenderEffect on 12+

Status: accepted, measured on the Philips TV (#17). Whether the glass looks frosted enough from the couch still needs a check by eye.

## Context

The dock, the weather pill and the Settings gear are frosted glass: the sky shows through them, softly blurred (`backdrop-filter: blur(20–30px)` in the v1 mock-up). The TV runs Android 11 (API 30, 32-bit ARM, MediaTek), and `RenderEffect` only exists from API 31. The sky animates: the sun's rays turn continuously, and later layers add weather and sky life. The spec (#1) says to spike Haze's opt-in RenderScript blur for API ≤ 30 first, and to draw the sky behind the glass a second time at about ⅛ resolution if Haze lags visibly behind the sky or costs too much.

## What we found

We read the API ≤ 30 path of Haze 2.0.1 (`RenderScriptBlurVisualEffectDelegate`):

- It records the content behind the glass into a scaled layer, renders that into an `ImageReader` surface, and blurs it with `ScriptIntrinsicBlur` in a coroutine. Until a new blur is ready it draws the previous output. So it's always at least one frame behind anything that moves, and here the sky moves every frame while the sun is up.
- Every changed frame is drawn into a RenderScript `Allocation` surface and awaited before the blur runs. On a 32-bit MediaTek TV, RenderScript intrinsics may run on the CPU.
- RenderScript has been deprecated since API 31.
- Haze picks the path from `Build.VERSION.SDK_INT`. On the API 31 emulator it always uses `RenderEffect`, so its Android 11 path can't be seen, let alone measured, without the TV.

Our sky is procedural. One frame is a pure function of the scene, the app glow and the animation time, drawn in 1280×720 reference pixels. Drawing it a second time, offset to where the glass sits, gives an exact copy of what's behind the glass in the same frame. At ⅛ resolution the dock’s copy is 213 × 30 pixels instead of 1704 × 240.

Measured on an Android TV 12 (API 31) emulator with the TV's 1920×1080 screen, Home at 17:05 with the sun and rays animating, 10 s of `dumpsys gfxinfo` (emulator frame times don't reflect the TV):

| Path | Frames | Janky | 50th / 90th / 99th percentile |
| --- | --- | --- | --- |
| API 31: `RenderEffect` blur | 306 | 0 | 7 / 11 / 13 ms |
| API 30 (forced): ⅛-resolution copy | 307 | 0 | 6 / 9 / 12 ms |

Both hold the 30 fps cap from #5.

With every layer in place (#16), a storm at dusk is the heaviest scene: 25 Oct 17:30, storm with SW 75 km/h, so 90 drops, 3 rain curtains, 9 scud clouds, leaves, lightning and 30 window beads, each drawn once for the screen and once per glass element. Same emulator, 10 s of `dumpsys gfxinfo`, debug build:

| Path | Scene | Frames | Janky | 50th / 90th / 99th percentile |
| --- | --- | --- | --- | --- |
| API 31: `RenderEffect` blur | sky only | 320 | 0 | 10 / 14 / 18 ms |
| API 30 (forced): ⅛-resolution copy | sky only | 310 | 0 | 10 / 13 / 16 ms |
| API 31: `RenderEffect` blur | moving focus along the dock every 0.5 s | 640 | 0 | 18 / 20 / 22 ms |
| API 30 (forced): ⅛-resolution copy | moving focus along the dock every 0.5 s | 636 | 0 | 18 / 19 / 21 ms |

The sky holds its ~30 fps with no janky frames on either path, and the dock's focus moves add frames at the display rate on top. These are emulator numbers: they don't reflect the TV's MediaTek GPU, so the particle counts are unchanged. The TV measurement is on #17.

### On the Philips TV

Measured on the Philips TV (Android 11, API 30, armeabi-v7a, 1920×1080 at 60 Hz), so always the ⅛-resolution copy. The app was compiled with `cmd package compile -m speed-profile -f` before measuring, and each run is 15 s of `dumpsys gfxinfo` after 6 s of warm-up. Focus moves are one D-pad press about every 0.5 s along the dock.

Release builds have no debug extras, so the sunset and storm scenes ran on a local, debug-signed build of the same code with R8 minification on and `debuggable` off. The live row is the signed release build on 25 Oct at 23:13, a cloudy night.

| Build | Scene | Frames | Janky | 50th / 90th / 95th / 99th percentile |
| --- | --- | --- | --- | --- |
| release | live: cloudy night, sky only | 453 | 0 | 7 / 8 / 9 / 10 ms |
| release | live: cloudy night, focus moves | 731 | 437 | 17 / 23 / 32 / 57 ms |
| minified debug | 17:05 clear, sun behind the dock, sky only | 453 | 268 | 16 / 18 / 19 / 21 ms |
| minified debug | 17:05 clear, sun behind the dock, focus moves | 779 | 643 | 20 / 28 / 36 / 46 ms |
| minified debug | 17:30 storm, SW 75 km/h, sky only | 440 | 440 | 21 / 24 / 25 / 29 ms |
| minified debug | 17:30 storm, SW 75 km/h, focus moves | 701 | 689 | 27 / 53 / 57 / 81 ms |
| plain debug (no R8) | 17:30 storm, SW 75 km/h, sky only | 202 (10 s) | 202 | 30 / 34 / 36 / 42 ms |

gfxinfo counts a frame as janky when it misses the 16.7 ms deadline of a 60 Hz display, so every frame of a 30 fps sky that takes more than 16.7 ms is counted as janky even though it's on time for 30 fps.

- **The sky holds 30 fps on the TV, the storm at dusk included.** 440 frames in 15 s is 29 fps, and the 99th percentile (29 ms) stays under the 33 ms budget. The ⅛ copy is drawn in the same frame as the sky, so it keeps up with the turning rays, rain and beads by construction. Screenshots show the sun and drops behind the dock as soft smudges.
- **The dock doesn't hold 60 fps while focus moves.** On a calm night the release build reaches 17 / 23 ms (about 50–60 fps). At sunset it reaches 20 / 28 ms, and in a storm 27 / 53 ms. During a focus move the whole sky, with its three glass copies, is drawn at the display rate instead of 30 fps. Lower particle counts would only help the storm case: even a clear sunset misses 16.7 ms, so we left the counts unchanged. Holding 60 fps would need the sky's own layers to stay at 30 fps while the dock animates. That's possible follow-up work, not a change to the particle counts.
- **The plain debug build isn't representative.** Without R8 it renders the storm at about 20 fps.
- The screensaver (sky without glass, cloudy night) rendered 0 janky frames, at 5 ms per frame, over 65 s, including a glide of the clock line.

## Decision

We don't add Haze. `Modifier.frostedGlass(shape, radius)` draws the sky behind the element itself, on both paths. Only the blur step differs, chosen by `LocalRendering`:

- **API 30 (the TV):** the sky is drawn into an `ImageBitmap` at ⅛ resolution and stretched back up with bilinear filtering. It's a cheap blur that stays in step with the sky because it is the same frame.
- **API 31+:** the sky is recorded into a `GraphicsLayer` with a real `BlurEffect` (CSS σ 15 / 22.5 dp, so a radius of 26 / 39 dp).

`SkyBackground` offers its drawing to the content on top as `LocalSkyBackdrop`, so every sky layer added later (moon, weather, sky life) shows up behind the glass with no extra work. Debug builds can force the API 30 path with `--ez force_api30_rendering true` (`make run.debug.api30`).

## Consequences

- The glass never lags the sky and adds no dependency. RenderScript isn't used.
- Every sky layer must draw in the 1280×720 reference pixels, full-screen fills included (`ReferenceSize`). A fill that relies on the `DrawScope`'s own size covers the screen in the sky Canvas but only a corner of the copy behind the glass, which leaves the copy see-through: until #16 the sky's base colour did this, so rain and beads showed sharply through the dock.
- The sky is drawn once more per glass element per frame: three times at ⅛ resolution on the TV. The cost grows with every sky layer, so heavy particle layers should be checked behind the glass too.
- ⅛ resolution is a weaker blur than the mock-up's 20–30 px: rays and stars soften, but the sun's core stays recognisable behind the dock. If it looks too sharp on the TV, a smaller copy (1/12, 1/16) is a one-number change.
- On the TV the ⅛ copy is cheap enough for the sky's 30 fps, even in a storm at dusk (see "On the Philips TV"), so Haze's RenderScript path isn't needed. The dock's focus moves drop below 60 fps because the whole sky redraws with them. Whether the glass looks frosted enough from the couch is still open on #17.
