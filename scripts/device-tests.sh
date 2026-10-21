#!/usr/bin/env bash
set -euo pipefail
trap 'adb pull /sdcard/Pictures/NovelForgeTests app/build/screenshots-device || true' EXIT
./gradlew :app:assembleDebug :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.timeout_msec=120000
