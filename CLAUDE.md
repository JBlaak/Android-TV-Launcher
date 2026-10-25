# Android TV Launcher

A minimal personal Android TV home screen for five apps. The background follows
the real sun and weather in Enschede. Design decisions, the TV's placement and
the app list live in `docs/DESIGN.md`.

## Building it

> Temporary, personal setup for building v1. Issue #17 moves this out before the repo is presented publicly.

The plan is the spec in issue #1 and its sub-issues (#2 onward), linked as GitHub issue dependencies. Pick the next ticket whose blockers are all closed.

- **Principles:** follow MoveLab-Connect-Android at `/Users/jorisblaak/Development/MoveLab-Studio/MoveLab-Connect-Android`. Mirror its build setup, testing style and conventions, except where the spec (#1) deliberately differs: three modules (`sky`, `weather`, `app`), no Acorn, English and Dutch only, comments allowed in `sky`, and Paparazzi snapshots in git.
- **Skills:** load `kotlin-style` before writing Kotlin, and build every ticket test-first with `tdd`.
- **PRs:** one branch and PR per ticket. Request a review from @JBlaak; never assign them.
- **Device:** test on the emulator **`TV_Launcher_API31`**, an Android TV image with the TV's screen (1920×1080, density 320 → 960×540 dp) and 2.8 GB RAM. Start it with `emulator -avd TV_Launcher_API31`. Install every build there and check it by eye before calling a ticket done. It may also be the Home app there.
- **Emulator gaps:** the real target is a Philips TV on Android 11 (API 30, 32-bit ARM, MediaTek), but no Android TV 11 ARM image exists. So:
  - Keep minSdk 30 and let lint guard against API 31+ calls.
  - Give debug builds a way to force the API 30 rendering paths (no RenderEffect blur) so the fallback can be seen on the emulator.
  - None of the five streaming apps are installed on the emulator. Debug builds may use a fake `InstalledApps` that reports all five as installed; never in release.
  - Frame rates on the emulator don't reflect the TV. Don't tune particle counts on it.
- **Checks for the real TV:** wherever a ticket asks for a measurement or check "on the TV", do what's possible on the emulator. Then add the TV-only part as a checklist item in #17, and still close the ticket.

## Agent skills

### Issue tracker

Issues live in GitHub Issues on JBlaak/Android-TV-Launcher (`gh` CLI). See `docs/agents/issue-tracker.md`.

### Triage labels

Default vocabulary: `needs-triage`, `needs-info`, `ready-for-agent`, `ready-for-human`, `wontfix`. See `docs/agents/triage-labels.md`.

### Domain docs

Single-context: one `GLOSSARY.md` and `docs/adr/` at the repo root. See `docs/agents/domain.md`.
