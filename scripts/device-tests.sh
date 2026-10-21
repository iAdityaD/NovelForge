#!/usr/bin/env bash
set -euo pipefail
trap 'adb pull /sdcard/Android/data/com.novelforge.app/files/screenshots app/build/screenshots-device || true' EXIT
./gradlew :app:assembleDebug :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.timeout_msec=120000
