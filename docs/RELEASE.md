# First LTS milestone: release candidate

The app targets Android 16 (API 36), retains support for Android 8.0+, and uses the existing Kotlin / Compose / Room modular architecture. The candidate is not automatically a published LTS release.

## Release build

CI builds a minified unsigned release App Bundle in addition to the installable debug APK. To sign a release, provide these environment variables securely in the release environment:

- NOVELFORGE_KEYSTORE: absolute path to the owner's private keystore
- NOVELFORGE_STORE_PASSWORD
- NOVELFORGE_KEY_ALIAS
- NOVELFORGE_KEY_PASSWORD

Run `./gradlew :app:bundleRelease :app:lintRelease`. Keep passwords and the keystore out of git. Without signing credentials, the bundle remains unsigned and cannot be submitted as a signed release. Debug APKs are for testing and may not update previous debug builds if the key changed. Export every project backup before uninstalling any build.

## Candidate acceptance

Automated gates cover domain and persistence tests, migration preservation, real Android UI journeys, lint, architectural dependency checks, and release compilation. The workspace tabs are Overview, Write, and AI. AI intentionally displays Coming soon and makes no network calls.

Before publishing: perform physical-device keyboard, low-storage, accessibility/TalkBack, large-project and 16 KB native-library compatibility checks; install the signed release on a clean device and upgrade a prior signed version; review Play pre-launch results; configure the owner's Play listing, support contact, privacy-policy URL, content rating and Data safety answers. Publication and signing require the owner's release account and key; neither has been created or silently substituted.

## Privacy summary for the listing

NovelForge stores writing, project metadata and normalized cover images locally. No account, advertising SDK, analytics service, cloud sync, AI service or internet permission is present. Image selection and backup/export use Android's system pickers. Exported files are stored at the destination chosen by the user, whose provider may have its own privacy policy. Deleting a project removes its local records; exported backups must be deleted separately. Android automatic cloud backup is disabled.

A support contact and hosted policy URL must be supplied by the publisher before submission. This summary describes the current build and should be rechecked whenever network features are added.

## LTS maintenance

Keep schema migrations and backup readers backward-compatible. Make data-safety fixes before cosmetic enhancements. Preserve stable document IDs. Version patches independently from future AI features. Keep release notes and a verified-source commit in PROGRESS.md for every distributed build. Do not call an untested build LTS solely because its version starts with 1.0.

Current target requirement checked against https://support.google.com/googleplay/android-developer/answer/11926878 on 25 October 2026.
