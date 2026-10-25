# Frosted glass: our own ⅛-resolution sky on Android 11, RenderEffect on 12+

Status: accepted, pending the measurement on the Philips TV (#17)

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
- Still to measure on the TV (#17): the frame rate on Home with the sun animating behind the dock, and whether the glass looks right. If the ⅛ copy turns out too expensive there, revisit Haze's RenderScript path with that measurement.
