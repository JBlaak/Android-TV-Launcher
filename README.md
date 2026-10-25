# Android TV Launcher

A minimalist Android TV launcher, built to fit one specific home. It holds five apps and nothing else, over a living
sky that follows the real sun, moon, weather and wind as seen through a west-facing window in Enschede. It also runs
as the TV's screensaver.

It's made for one living room, but you're welcome to use it as a base for your own.

| Sunset | Night with moon | Storm |
|---|---|---|
| ![Sunset](app/src/test/snapshots/images/dev.jblaak.tvlauncher.home_HomeSnapshotTests_netflix_focused%5BEnglish%5D.png) | ![Night with moon](app/src/test/snapshots/images/dev.jblaak.tvlauncher.home_HomeSnapshotTests_night_with_moon%5BEnglish%5D.png) | ![Storm](app/src/test/snapshots/images/dev.jblaak.tvlauncher.home_HomeSnapshotTests_storm%5BEnglish%5D.png) |

These are Paparazzi snapshots; the full set is in [`app/src/test/snapshots/images`](app/src/test/snapshots/images).
The design, and the sky, sun, moon and weather models behind it, are in [`docs/DESIGN.md`](docs/DESIGN.md).

Modules:

- `sky`: pure Kotlin. Turns a moment, a place and the weather into the sky to draw.
- `weather`: the [Open-Meteo](https://open-meteo.com) adapter. It needs no API key.
- `app`: the Android TV app (`dev.jblaak.tvlauncher`), registered as the Home screen. Android 11 (API 30) and up.

## Make it your own

Everything specific to this home is in
[`HomeConfig`](app/src/main/kotlin/dev/jblaak/tvlauncher/home/HomeConfig.kt):

- **Location:** `Window.location`, the latitude and longitude for the sun, the moon and the weather.
- **Window direction:** `Window.facing`, the compass direction the screen "looks" towards (270° is west). The sun
  and moon only show when they would be visible through that window.
- **Apps:** `apps`, in dock order. Each `AppConfig` has the package names to launch (the first installed one wins),
  a name, a one-line description, a logo and its size. Also add new packages to `<queries>` in
  `AndroidManifest.xml`, so Android lets the launcher see them, and to the `App` enum, which picks each tile's
  focus animation.
- **Brand colours:** each app's `BrandColors` (the focused tile's gradient and glow) and `LogoColors` (the logo when
  the tile is idle and focused).

The time zone and language follow the TV's own settings.

## Build

Needs JDK 21 and the Android SDK.

```bash
make help              # list all commands
make test              # unit tests, lint and Paparazzi verification
make ktlint            # check the code style (make ktlint.format fixes it)
make snapshots.record  # re-record the Paparazzi snapshots after a UI change
```

Paparazzi snapshots are committed in `app/src/test/snapshots`.

### Release signing

Without signing details, release builds come out unsigned (`app-release-unsigned.apk`) and Gradle prints a warning.
CI never signs. To sign with your own key, create `~/.android-tv-launcher/signing.properties`:

```properties
storeFile=/path/to/your-release-key.jks
storePassword=...
keyAlias=...
keyPassword=...
```

To keep the file somewhere else, set `tvlauncher.signing=/path/to/signing.properties` in
`~/.gradle/gradle.properties`. Keep the keystore and the properties file out of the repository.

## Install over adb

Turn on developer options and network debugging on the TV, then connect to it with `adb connect <tv-ip>:5555`. With
more than one device attached, point every command at the right one by setting `ANDROID_SERIAL` (or
`ADB="adb -s <serial>"` for the make targets).

```bash
make run.debug     # build the debug app, install it, make it Home and open it
make run.release   # the same for the signed release build
```

### Set it as Home

`make run.debug` and `make run.release` already do this. By hand:

```bash
adb shell cmd package set-home-activity dev.jblaak.tvlauncher/.HomeActivity
adb shell am start -n dev.jblaak.tvlauncher/.HomeActivity
```

Press Home on the remote to check that the launcher appears.

### Restore the previous launcher

The previous launcher stays installed. List the Home apps to find its activity, then hand Home back to it:

```bash
make home.list
make home.restore PREVIOUS_HOME=<package>/<activity>
```

This runs `adb shell cmd package set-home-activity <package>/<activity>`.

## Screensaver

The living sky is also a screensaver, "Living sky" ("Levende lucht" in Dutch): the full sky without the dock, with a
small clock and weather line that moves to a new spot every minute. Pick it, and the idle delay, in the TV's
screensaver settings.

Some TVs hide third-party screensavers. Select it over adb instead:

```bash
adb shell settings get secure screensaver_components   # note the current value, to go back to it later
adb shell settings put secure screensaver_components dev.jblaak.tvlauncher/.SkyScreensaver
adb shell settings put secure screensaver_enabled 1
adb shell am start -n com.android.systemui/.Somnambulator   # start it now instead of waiting
```

## Languages

The launcher follows the TV's language: Dutch when the TV is set to Dutch, English otherwise. Dates follow the locale
too.

- Every string has an English version in `app/src/main/res/values/strings.xml` and a Dutch one in
  `app/src/main/res/values-nl/strings.xml`. Lint fails the build when a Dutch translation is missing.
- Names that are the same in both languages (app names, "NPO 1 · NPO 2 · NPO 3 live") and the Dutch moments
  ("Fijne Koningsdag") live only in `values/` and are marked `translatable="false"`.
- Every Paparazzi scene with text runs in English and Dutch: give the snapshot test a `@TestParameter locale: Locale`
  and use `Television.copy(locale = locale.tag)`, like `HomeSnapshotTests`. Scenes without text, such as the sky
  snapshots, run in one locale.

## Developer options

Debug builds have a few shortcuts for working without a TV or real weather. Release builds have none of them.

- **All apps installed:** debug builds show all five tiles, even when the apps aren't installed, as on an emulator.
- **Extras:** pass these when starting Home from adb. Force-stop the app first
  (`adb shell am force-stop dev.jblaak.tvlauncher`), because Home reads them only when it starts.

  ```bash
  # Start at another local time; time runs on from there
  adb shell am start -n dev.jblaak.tvlauncher/.HomeActivity --es start_time 2026-10-25T17:05
  # Show any weather (clear, warm, clouds, rain, storm, snow, fog) instead of the live weather;
  # any other name shows none at all, as when offline
  adb shell am start -n dev.jblaak.tvlauncher/.HomeActivity --es weather rain --es temperature 9
  # With a weather, set the wind: where it blows from (degrees) and its speed (km/h)
  adb shell am start -n dev.jblaak.tvlauncher/.HomeActivity --es weather storm --es wind_from 180 --es wind_speed 60
  # Use the Android 11 glass (a low-resolution copy of the sky) on a newer device; make run.debug.api30 does this
  adb shell am start -n dev.jblaak.tvlauncher/.HomeActivity --ez force_api30_rendering true
  ```

- **Emulator:** on the Android TV emulator, Google's own launcher has a higher priority than any other Home app and
  keeps winning. Turn it off with `adb shell pm disable-user --user 0 com.google.android.tvlauncher` (and back on
  with `adb shell pm enable com.google.android.tvlauncher`). Frame rates on the emulator don't reflect a real TV.

## License

[MIT](LICENSE). The bundled Figtree font is under the SIL Open Font License
([`docs/licenses/Figtree-OFL.txt`](docs/licenses/Figtree-OFL.txt)).
