# Android TV Launcher

A minimal personal Android TV home screen for five apps, with a background that follows the real sun and weather in
Enschede. The design lives in [`docs/DESIGN.md`](docs/DESIGN.md).

Modules:

- `sky`: pure Kotlin. Turns a moment, a place and the weather into the sky to draw.
- `weather`: the Open-Meteo adapter.
- `app`: the Android TV app (`dev.jblaak.tvlauncher`), registered as the Home screen.

## Build

Needs JDK 21 and the Android SDK.

```bash
make help              # list all commands
make test              # unit tests, lint and Paparazzi verification
make ktlint            # check the code style (make ktlint.format fixes it)
make snapshots.record  # re-record the Paparazzi snapshots after a UI change
```

Paparazzi snapshots are committed in `app/src/test/snapshots`.

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

## Install over adb

Connect to the TV first (`adb connect <tv-ip>:5555`). With more than one device attached, point every command at the
right one by setting `ANDROID_SERIAL` (or `ADB="adb -s <serial>"` for the make targets).

```bash
make run.debug     # build the debug app, install it, make it Home and open it
make run.release   # the same for the signed release build
```

Both run these adb commands after installing:

```bash
adb shell cmd package set-home-activity dev.jblaak.tvlauncher/.HomeActivity
adb shell am start -n dev.jblaak.tvlauncher/.HomeActivity
```

Press Home on the remote to check that the launcher appears.

Debug builds take a few extras when started from adb, to check the sky on the emulator:

```bash
# Start at another local time; time runs on from there
adb shell am start -n dev.jblaak.tvlauncher/.HomeActivity --es start_time 2026-10-25T17:05
# Force the API 30 rendering path (no RenderEffect), as on the TV; make run.debug.api30 does this
adb shell am start -n dev.jblaak.tvlauncher/.HomeActivity --ez force_api30_rendering true
# Show any weather (clear, warm, clouds, rain, storm, snow, fog) instead of the live weather, or none at all (offline)
adb shell am start -n dev.jblaak.tvlauncher/.HomeActivity --es weather rain --es temperature 9
```

Force-stop the app first (`adb shell am force-stop dev.jblaak.tvlauncher`), because Home reads them only when it starts.

On the Android TV emulator, Google's own launcher has a higher priority than any other Home app and keeps winning. Turn
it off there with `adb shell pm disable-user --user 0 com.google.android.tvlauncher` (and back on with
`adb shell pm enable com.google.android.tvlauncher`).

## Screensaver

The app also offers the living sky as the TV's screensaver ("Living sky", "Levende lucht" in Dutch): the full sky
without the dock, with a small clock and weather line that moves to a new spot every minute. Pick it, and the idle
delay, in the TV's own screensaver settings.

If the TV's settings don't list it (Philips may hide third-party screensavers), select it over adb:

```bash
adb shell settings get secure screensaver_components   # note the current value, to go back to it later
adb shell settings put secure screensaver_components dev.jblaak.tvlauncher/.SkyScreensaver
adb shell settings put secure screensaver_enabled 1
```

The TV's idle delay still applies. To see it straight away, start the screensaver now:

```bash
adb shell am start -n com.android.systemui/.Somnambulator
```

## Restore Projectivy as Home

One command hands Home back to Projectivy, which stays installed:

```bash
adb shell cmd package set-home-activity com.spocky.projengmenu/.ui.home.MainActivity
```

`make home.restore-projectivy` runs it and presses Home. If Projectivy's activity name ever changes, list the Home
apps with `adb shell pm query-activities --brief -a android.intent.action.MAIN -c android.intent.category.HOME`.

## Release signing

Release builds are signed with a personal key that never enters this repository. The keystore and its passwords are in
1Password ("Android TV Launcher release key").

1. Save the keystore from 1Password to `~/.android-tv-launcher/release.jks`.
2. Create `~/.android-tv-launcher/signing.properties`:

   ```properties
   storeFile=/Users/<you>/.android-tv-launcher/release.jks
   storePassword=<from 1Password>
   keyAlias=<from 1Password>
   keyPassword=<from 1Password>
   ```

3. Run `make run.release`.

To keep the file somewhere else, set `tvlauncher.signing=/path/to/signing.properties` in `~/.gradle/gradle.properties`.

Without the properties file the build still works: release builds come out unsigned
(`app-release-unsigned.apk`), and Gradle prints a warning. CI never signs.
