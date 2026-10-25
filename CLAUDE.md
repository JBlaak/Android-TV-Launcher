# Android TV Launcher

A minimal Android TV home screen for five apps. The background follows the real sun, moon and weather in Enschede.
Design decisions, the window the TV stands in for and the app list live in `docs/DESIGN.md`. Building, installing and
the debug developer options are in `README.md`.

## Conventions

- **Modules:** three, `sky` (pure Kotlin), `weather` (the Open-Meteo adapter) and `app`. Comments are allowed in `sky`, where the astronomy needs them; elsewhere, let names carry the meaning.
- **Skills:** load `kotlin-style` before writing Kotlin, and build test-first with `tdd`.
- **Languages:** every change adds its Dutch strings (`values-nl`) alongside the English ones, and its Paparazzi scenes run in English and Dutch. See "Languages" in the README.
- **Snapshots:** Paparazzi snapshots are committed. Re-record them with `make snapshots.record` after a UI change.
- **Android 11:** minSdk is 30 and lint guards against API 31+ calls. Check the API 30 rendering path (no `RenderEffect` blur) with the debug option in the README.
- **Fakes stay in debug:** the debug developer options (fake installed apps, time and weather overrides) live in `app/src/debug`; release builds use the real adapters.
- **Frame rates:** emulator frame rates don't reflect a real TV. Tune particle counts on the TV.
- **PRs:** one branch and PR per issue.

## Agent skills

### Issue tracker

Issues live in GitHub Issues on JBlaak/Android-TV-Launcher (`gh` CLI). See `docs/agents/issue-tracker.md`.

### Triage labels

Default vocabulary: `needs-triage`, `needs-info`, `ready-for-agent`, `ready-for-human`, `wontfix`. See `docs/agents/triage-labels.md`.

### Domain docs

Single-context: one `GLOSSARY.md` and `docs/adr/` at the repo root. See `docs/agents/domain.md`.
