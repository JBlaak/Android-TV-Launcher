# Android TV Launcher Makefile
#
# Run 'make help' to see all available commands with descriptions.
#

SHELL := /bin/bash
.ONESHELL:
.SHELLFLAGS := -eu -o pipefail -c
.DELETE_ON_ERROR:
MAKEFLAGS += --warn-undefined-variables
MAKEFLAGS += --no-builtin-rules

ADB ?= adb
PACKAGE = dev.jblaak.tvlauncher
HOME_ACTIVITY = $(PACKAGE)/.HomeActivity
PREVIOUS_HOME ?=

# ---------------------- COMMANDS ---------------------------

run.debug: # Install the debug build over adb, make it Home and open it
	@./gradlew :app:assembleDebug
	@$(ADB) install -r app/build/outputs/apk/debug/app-debug.apk
	@$(ADB) shell cmd package set-home-activity $(HOME_ACTIVITY)
	@$(ADB) shell am start -n $(HOME_ACTIVITY)

run.debug.api30: # Install the debug build and open it on the API 30 rendering path (no RenderEffect)
	@./gradlew :app:assembleDebug
	@$(ADB) install -r app/build/outputs/apk/debug/app-debug.apk
	@$(ADB) shell cmd package set-home-activity $(HOME_ACTIVITY)
	@$(ADB) shell am force-stop $(PACKAGE)
	@$(ADB) shell am start -n $(HOME_ACTIVITY) --ez force_api30_rendering true

run.release: # Install the signed release build over adb, make it Home and open it
	@./gradlew :app:assembleRelease
	@$(ADB) install -r app/build/outputs/apk/release/app-release.apk
	@$(ADB) shell cmd package set-home-activity $(HOME_ACTIVITY)
	@$(ADB) shell am start -n $(HOME_ACTIVITY)

home.list: # List the installed Home apps
	@$(ADB) shell pm query-activities --brief -a android.intent.action.MAIN -c android.intent.category.HOME

home.restore: # Hand Home back to another launcher: make home.restore PREVIOUS_HOME=<package>/<activity>
	@test -n "$(PREVIOUS_HOME)" || { echo "Set PREVIOUS_HOME, for example from make home.list"; exit 1; }
	@$(ADB) shell cmd package set-home-activity $(PREVIOUS_HOME)
	@$(ADB) shell input keyevent KEYCODE_HOME

test: # Unit tests, lint and snapshot verification
	@./gradlew test lintRelease verifyPaparazziRelease -PallWarningsAsErrors=true

snapshots.record: # Record Paparazzi snapshots
	@./gradlew recordPaparazziRelease

snapshots.verify: # Verify Paparazzi snapshots
	@./gradlew verifyPaparazziRelease

ktlint: # Lint code
	@.ops/ktlint "**/src/**/*.kt" "**/*.kts" "!**/build/**" --color

ktlint.format: # Format code
	@./.ops/ktlint -F "**/src/**/*.kt" "**/*.kts" "!**/build/**"

update-scripts: # Updates helper binary like ktlint
	@./.ops/ktlint --update
	@$(MAKE) ktlint.format

# -----------------------------------------------------------
.PHONY: help test

.DEFAULT_GOAL := help
TAB := '%-30s'

help:
	@printf '\n    Available make commands:\n\n'
	@grep -E '^([a-zA-Z0-9_-]+\.?)+:.+#.+$$' $(MAKEFILE_LIST) \
		| sed 's/:.*#/: #/g' \
		| awk 'BEGIN {FS = ":[: ]+#[ ]+"}; {printf "    make $(TAB) # %s\n", $$1, $$2}'
	@echo ""
