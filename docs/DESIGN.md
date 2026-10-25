# Android TV Launcher — design

A minimal, personal Android TV home screen for five apps. Its background follows
the real sun and weather.

**Version 1 (25 Oct 2026)** is the reference to build from. Its full mock-up source (layout, sky, sun, weather
and wind logic) is in [`docs/mockups/dock-v1.dc.html`](mockups/dock-v1.dc.html).

This document describes v1 **as it shipped** (spec #1). Where the build deliberately differs from the
mock-up, it says so. Those choices are collected under [Where v1 differs from the mock-up](#where-v1-differs-from-the-mock-up).

## Setting

| | |
|---|---|
| Location | Enschede, NL — 52.2215° N, 6.8937° E |
| Time zone | Europe/Amsterdam (CET/CEST) |
| TV placement | On the **west wall**: the screen acts as a window looking **west (270°)**. Viewed through it, south is on the left and north on the right. |

## Apps

In dock order. Verify package names on the TV with `adb shell pm list packages`.

| App | Package | Notes |
|---|---|---|
| NPO TV | `tech.blaak.npotv` | Own app, [JBlaak/NPO-TV](https://github.com/JBlaak/NPO-TV). Icon: the apple core from its `ic_launcher_foreground`. |
| Netflix | `com.netflix.ninja` | |
| Disney+ | `com.disney.disneyplus` | No bundled logo. The tile shows Disney+'s own TV banner from the system, with tile colours taken from it, or the "Disney+" wordmark when there is no banner. |
| Plex | `com.edde746.plezy` | Launches [Plezy](https://github.com/edde746/plezy), a Plex client. The tile shows the **Plex** logo and name on purpose, not Plezy's icon. Fallback if Plezy isn't installed: `com.plexapp.android`. |
| YouTube | `com.google.android.youtube.tv` | |

An app that isn't installed has no tile (Plex first falls back to the Plex app). Coming back Home puts focus on
the app that was opened last. Holding OK on a tile does nothing.

## Layout

- Top left: greeting and date ("Good evening · Sun 25 Oct"), with a large clock (`HH:mm`, 24 h) under it.
- Top right: a glass pill showing the weather icon, condition, temperature and the next sunrise or sunset ("Rain 9° · Sunset 17:18"). At wind speeds of 50 km/h and up, the wind replaces the sunrise/sunset time ("Storm 8° · Wind SW 75 km/h").
- **Alignment:** everything lines up with the tile edges, not the dock's outer frame. The top row and the label row are inset 19 px (the dock's 18 px padding plus its 1 px border). That way the greeting, clock and app name start at the first tile's left edge, and the pill and gear group ends at the last tile's right edge.
- **Settings:** a round glass gear button (50 px) to the right of the weather pill in the top row. One press **up** from the dock reaches it. It opens the system settings (`android.settings.SETTINGS`). The dock holds only the five apps: no Settings tile and no extra items. When the gear is focused, it turns white with a dark icon, and the line above the dock reads "Settings — Network, display, sound and apps".
- Bottom: the focused app's name and a one-line description, above a frosted glass bar with five tiles.
- Tiles you're not on are dark glass with the logo in brand colour. Logos that are dark (Plex) or text (Disney+) are lightened to near-white.
- The focused tile widens (flex 1.7 vs 1), fills with the app's brand gradient, gets a 2 px white ring and casts a glow in its own colour.
- **Playful focus:** when a tile gains focus, its logo does a small move of its own and the tile dips, overshoots and settles. NPO TV wobbles, Netflix pulses "ta-dum", a cyan sparkle sweeps through Disney+ (a softer white shine over a system banner), Plex slides in from the left, and YouTube nudges forward. The tile that has focus when Home opens stays still.
- Font: Figtree (300/500/800), bundled with the app under the SIL Open Font License ([`licenses/Figtree-OFL.txt`](licenses/Figtree-OFL.txt)).
- Mock-ups are 1280×720 px. The TV's 1920×1080 screen at density 320 is 960×540 dp, so every mock-up size is scaled by 0.75 to dp. The mock-up's own 72 × 64 px margins become 54 × 48 dp (108 × 96 px on the TV, 5.6% and 8.9% of the screen), which keeps everything inside the usual overscan-safe area.

## Background: sky by sun elevation

The base colour and two soft glows are blended between keyframes by the sun's
elevation (degrees). There's no flat black.

| Elevation | Base | Warm glow (c1) | Cool glow (c2) | Strength |
|---|---|---|---|---|
| ≤ −12 (night) | `rgb(16,18,36)` | violet `80,70,170` | teal `40,100,140` | 0.40 |
| −6 (blue hour) | `rgb(26,30,64)` | `90,100,200` | lavender `160,100,210` | 0.55 |
| −1 (sunset/sunrise) | `rgb(50,30,58)` | orange `255,125,85` | rose `205,85,150` | 0.60 |
| 5 (golden hour) | `rgb(56,42,50)` | `255,175,95` | `230,125,115` | 0.55 |
| 15 (day) | `rgb(32,48,72)` | sky blue `115,175,235` | mint `125,210,200` | 0.50 |
| 90 | `rgb(40,60,88)` | `145,200,245` | `155,225,215` | 0.50 |

- The warm glow sits at the sun's on-screen position; the cool glow sits on the opposite side.
- The focused app's brand colour glows up from the bottom (Glow setting, default 60%).

## Sun

- **Position:** a standard low-precision solar formula (J2000 days, mean anomaly, ecliptic longitude, GMST) gives elevation and azimuth. Sunrise and sunset are where elevation crosses −0.833°. Checked for Enschede: 25 Oct 07:16 / 17:18, 21 Jun 05:11 / 21:58, 21 Dec 08:40 / 16:22.
- **On screen** (TV looks toward `view` = 270°):
  - `d = azimuth − view`, wrapped to −180…180
  - `x = 640 + d × 8` px; 8 px per degree gives a 160° wide view
  - `y = 600 − elevation × 8` px; the horizon sits behind the dock
- **Visibility:** the sun is drawn when elevation > −1° and it is less than ~1100 px from the centre, fading out over the last 300 px. In the morning it is behind the viewer, so there is no disc; a soft warm light falls from the top instead.
- **Typical paths:**
  - 25 Oct: enters from the left around 13:00 and sets just left of centre (azimuth ≈ 250°).
  - 21 Jun: high in the afternoon, then sets right of centre in the north-west (≈ 309°).
  - 21 Dec: low on the far left.
- **Look:** layered, using screen blending:
  1. A wide atmospheric glow.
  2. Two counter-rotating starbursts.
  3. A halo ring.
  4. A hot white core.
  5. A horizontal anamorphic streak.
  6. Seven lens-flare ghosts (circles, hexagons and rings) on the line from the sun through the screen centre.
- **Colour by elevation:** red-orange below 3°, warm 3–12°, near-white above that.
- **Clouds:** with clouds, fog or rain, only a soft diffuse glow shows where the sun is.

## Moon

Added after v1. It's always on, like the sun: no setting, and it shows only when it's really in view.

- **Position:** a low-precision lunar formula, accurate to about 1°:
  - mean longitude, mean anomaly and argument of latitude
  - the main 6.289° term for longitude and 5.128° for latitude
  - converted to right ascension and declination, then elevation and azimuth for Enschede
  - mapped to the screen with the same window projection as the sun (looking 270°, 8 px per degree, horizon at y = 600)
- **Phase:** age = days since the new moon of 2000-01-06 18:14 UTC, modulo 29.530588853. Lit fraction = (1 − cos(2π · age / synodic)) / 2. It's waxing while age is under half a cycle.
- **Drawing:**
  - A 64 px disc: a faint dark full disc (earthshine) plus the lit part.
  - The lit part is the bright limb as a semicircle, closed by an elliptical terminator with x-radius `24 · |1 − 2·lit|`.
  - The lit side is the right while waxing and the left while waning, as seen from the northern hemisphere. The disc's tilt is ignored.
  - A soft drop-shadow glow surrounds it.
- **Visibility:** it's drawn when its elevation is > 0°, it's within the window's view and at least 3% is lit.
- **By weather:**
  - **Clear or warm:** full disc. At 55% opacity while the sun is up (the daytime moon).
  - **Clouds:** disc at 45% opacity.
  - **Rain, storm, snow or fog:** no disc. A soft glow at its position at night, scaled by the lit fraction.
- **Example:** on 25–26 Oct 2026 (full moon) it enters the window from the left after about 01:00. At 03:00 it's high in the south-west (azimuth 242°, elevation 39°). It sinks toward due west by dawn.

## Weather

Each weather type pulls the sky toward its own colours and adds a full-screen effect layer.

| Weather | Sky shift | Effect |
|---|---|---|
| Clear | none | Sun (day) or 42 twinkling stars (below −6°) |
| Warm | amber, stronger glows | Brighter sun and a shimmering heat haze along the bottom |
| Clouds | grey, glows ×0.7 | 7 large blurred clouds drifting slowly |
| Rain | blue-grey | 5 clouds and 54 angled streaks; beads on the window (see [Window glass](#window-glass)) |
| Storm | dark violet-grey | 9 dark clouds racing across with the wind; 90 streaks, a third of them riding gusts; 3 sweeping rain curtains; leaves tumbling past when the wind blows across the view; a lightning flash every 9 s; beads on the window |
| Snow | lighter, cool | 5 clouds and 48 flakes of different sizes swaying down; frost in the corners of the window |
| Fog | pale grey | 5 blurred bands drifting |

The star, drop and flake counts start about 40% below the mock-up's (70 stars, 90 rain and 150 storm drops,
80 flakes), because the glass draws the sky several times a frame (see [Frosted glass](#frosted-glass)). Tuning
them for the TV is a check in #17. The few large clouds, curtains and leaves keep the mock-up's counts.

- **Glass:** the dock, pill and gear blur everything behind them, weather included: no drop, flake or bead shows sharply through them.

### Wind

The direction of rain, snow, clouds and leaves follows the real wind, seen through the west-facing window.

- **Inputs:** `windFrom` is where the wind blows **from** (meteorological degrees, as in the weather data). `windSpeed` is in km/h. Without live data the defaults are: storm SW 75, rain SW 20, snow E 15, otherwise SW 12. South-west is the prevailing direction here.
- **Sideways part:** the air moves toward `windFrom + 180°`. Only the part across the view is visible:
  - `rel = (windFrom + 180 − view)`, wrapped to −180…180
  - `side = windSpeed × sin(rel)`; positive moves right, which is toward the north for a TV looking west
- **Rain tilt:** `atan(side / 32)`, where 32 km/h is a raindrop's falling speed. It's capped at ±55° so the rain stays readable. Drops travel `1020 × tan(tilt)` px sideways while falling, and their start points spread so slanted rain still covers the whole screen. In a storm, every third drop uses 1.5× the wind (gusts).
- **Examples:**
  - SW 20 km/h: rain leans about 24° and moves left to right.
  - NE wind: the same, mirrored.
  - Due west or east: straight down, because the wind blows toward or away from the viewer.
- **Snow** drifts `side × 6` px sideways over its fall.
- **Storm clouds** race across in the wind's direction, faster at higher speeds.
- **Rain curtains** sweep across in that direction, slanted at the rain's angle.
- **Leaves** only appear when `|side| > 20 km/h`.
- **On Android:** these are per-particle values, the same as in the mock-up, where the CSS uses `--rot`, `--dx` and `--sx`.
- **Data source:** [Open-Meteo](https://open-meteo.com), which needs no API key. Its WMO weather codes map onto the types above: 0–1 clear (warm from 25 °C), 2–3 cloudy, 45/48 fog, drizzle, rain and showers rain, snow and snow showers snow, 95–99 storm. It also provides `wind_direction_10m`, `wind_speed_10m` and `wind_gusts_10m`. Home fetches it when it comes to the front and the last fetch is more than 15 minutes old, and every 15 minutes while it stays in front. The last result is kept for 3 hours, also across restarts; after that the sky is clear and the pill drops the temperature.

## Window glass

The window pane itself shows the weather, as the last layer of the sky, in front of rain and geese.

- **Rain and storm:** 30 beads (5–18 px wide, 1.12× as tall) spread over the top 92% of the glass, each with a highlight, a light lower rim and a thin shadow. Every fourth bead slides: it holds still for half its 9–17 s loop, creeps 40 px down, then speeds up and runs off the bottom.
- **Snow:** frost creeps in from the bottom-left, bottom-right and top-left corners.
- They sit behind the frosted glass, like everything outside, so the dock never looks dirty.

## Sky life

- **Shooting stars:** on a clear or warm night with the sun below −8°, two meteors on 13 s and 21 s loops, about two every 20 s.
- **Geese:** a V of nine birds with flapping wings flies toward the south-west in February, March and September to November, not in a storm or fog. They fly in an evening band around sunset: the sun in the west (azimuth > 180°) between 10° and −6° elevation, on 25 Oct about 15:55 to 17:55. The flock crosses in the first 55% of a 34 s loop.

## Moments

Special days change the greeting, and some get something in the sky. The first match wins:

- **Oud & Nieuw:** 31 Dec from 23:00 the greeting reads "Almost 2027", 1 Jan before 02:00 "Happy 2027", and fireworks (100 sparks and 5 blooms) go off the whole time.
- **Koningsdag:** 27 April, or the 26th when the 27th is a Sunday. The greeting is "Fijne Koningsdag" in both languages, and bunting hangs across the top.
- **Clock change:** all day on the day the clocks change: "Clocks went back · enjoy the extra hour" or "Clocks went forward · an hour shorter night".

## Frosted glass

The dock, the weather pill and the gear are frosted glass over the sky. The TV runs Android 11, which has no
`RenderEffect`, so the glass draws the sky behind itself a second time, in the same frame
([ADR 0001](adr/0001-glass-blur.md)):

- **Android 11 (the TV):** the sky is drawn at ⅛ resolution and stretched back up, a cheap blur that never lags the sky.
- **Android 12 and up:** the sky is drawn through a real blur (26 dp for the gear and pill, 39 dp for the dock).

Every sky layer is drawn inside one backdrop, so the sky, sun, moon, weather, sky life, celebrations and
window glass all show up blurred behind the glass. Each layer draws in the mock-up's 1280×720 reference
pixels, full-screen fills included, so the copy behind the glass is as opaque as the sky itself.

## Screensaver

The living sky is also the TV's screensaver, "Living sky" ("Levende lucht"). It shows the sky without the
dock, with a small clock and weather line that glides to a new spot every minute.

## Language

English and Dutch, following the TV's language. In Dutch the date reads like "zo 25 okt" and the wind like
"Wind ZW 75 km/u".

## When something fails

If working out the sky, the sun time or the weather fails, Home logs it and falls back to the plain dock on a
night-blue background, with no sky, pill or sun time. The clock keeps running, and the next successful
weather fetch brings the sky back. Home never crashes over the sky.

## Frame rates

The sky animates at about 30 fps, and only while Home is in front and something moves. The dock's focus
moves run at the display rate (60 fps). Emulator numbers (an Android TV 12 emulator with the TV's 1920×1080 screen, storm at dusk, 25 Oct 17:30,
10 s of `dumpsys gfxinfo`) are in [ADR 0001](adr/0001-glass-blur.md). The TV's own numbers are a check in #17.

## Where v1 differs from the mock-up

- **Particle counts** start about 40% lower (see [Weather](#weather)), to leave room for the glass drawing the sky several times a frame.
- **Glass blur:** the mock-up uses CSS `backdrop-filter: blur(20–30px)`. On the TV the glass is the ⅛-resolution copy of the sky, a softer kind of blur (ADR 0001).
- **Geese** fly only in the evening band around sunset described under [Sky life](#sky-life). The mock-up only required the sun above −6°.
- **Window glass** comes last in the sky, in front of the geese and fireworks; the mock-up drew the beads before them.
- **Daytime moon behind clouds** is at 45%, the same as at night.
- **Plain dock fallback** when the sky or weather fails, which the mock-up has no need for.
- **Disney+** shows the system's TV banner instead of a bundled logo.

## Still to do

- Long-press options menu on a tile.
- A bundled Disney+ logo.
