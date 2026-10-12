# QSO Vision

## Versions and downloads

See [VERSION-MAP.md](docs/VERSION-MAP.md) for the version-by-version source
status, changes, and APK downloads.

| Version | APK |
|---|---|
| 1.0 | No preserved APK; reconstructed from the original project trace |
| 1.1 | [Download](https://github.com/lim3soft/QSO-Vision/blob/main/artifacts/QSO-Vision-before-1.3.apk) |
| 1.2 | No separately preserved APK |
| 1.3 | [Download](https://github.com/lim3soft/QSO-Vision/blob/main/artifacts/QSO-Vision-1.3.apk) |
| 1.3.1 | [Download](https://github.com/lim3soft/QSO-Vision/blob/main/artifacts/QSO-Vision-1.3.1.apk) |
| 1.3.2a build 6 | [Download](https://github.com/lim3soft/QSO-Vision/blob/main/artifacts/QSO-Vision-1.3.2a-build6.apk) |
| 1.3.3 build 7 | [Download](https://github.com/lim3soft/QSO-Vision/blob/main/artifacts/QSO-Vision-1.3.3-build7.apk) |

## Release changelog

### 1.3.3 build 7

- Added resilient BrandMeister DOM and column discovery for callsign/history data.
- Added metadata structure diagnostics when the upstream page changes.
- Added physical-resolution and density-aware scaling for compact phones.
- Added orientation-aware sizing while preserving the existing QSO layout.

### 1.3.2a build 6

- Added responsive 70:30 landscape layout for current QSO and recent history.
- Moved `by 9W3SPY` beside the QSO Vision title.
- Added full-screen landscape status-bar handling.
- Improved action-button alignment and history scrolling.
- Fixed the landscape view reparenting crash.
- Removed the obsolete 30-second lock-screen option.
- Removed GeckoView to keep the APK lightweight.

### 1.3.1

- Refined toolbar and action controls.
- Improved screen dimming and monitoring controls.
- Preserved BrandMeister metadata and Hoseline audio behavior.

### 1.3

- Improved metadata filtering, audio handling, browser compatibility, and
  connection diagnostics.

### 1.2 and 1.1

- Added talkgroup selection persistence, current/last-heard filtering, QSO
  history rendering, WebView Hoseline fallback, and lifecycle/audio handling.

The complete dated record is available in [CHANGELOG.md](CHANGELOG.md).

## License

QSO Vision source code is licensed under the Apache License 2.0. See
[LICENSE](LICENSE), [NOTICE](NOTICE), and
[THIRD_PARTY_LICENSES.md](THIRD_PARTY_LICENSES.md) for dependency and
attribution information.

## Version 1.1 — current behavior

See [TESTING-1.1.md](TESTING-1.1.md) for the current single-talkgroup implementation.
Hoseline runs behind the native dashboard with optional in-app player controls.
Mute stops its WebView; unmute reloads the selected talkgroup. No system volume is changed.
The local clock includes seconds. Live native PTT metadata is **not connected**.
Demo is now explicitly opt-in; demo callsigns and filtered demo history are never presented as live traffic.
The older dual-channel notes below describe previous development and are superseded by this version.

An Android 7.0+ Kotlin dashboard for read-only BrandMeister monitoring. The default build is deliberately honest about upstream capabilities: the included `DemoQsoProvider` is visibly labeled DEMO and exists for UI/lifecycle verification. Replace it with an authorized adapter that emits validated QSO events; no fabricated public BrandMeister realtime endpoint is used.

## Build

With Android SDK/Gradle available:

```bash
./gradlew clean assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`

For a release, create a keystore outside source control, add a `signingConfigs` block in `app/build.gradle.kts` (prefer Gradle properties or environment variables), then run `./gradlew assembleRelease`. Never commit the keystore or API tokens.

## Integration boundary

BrandMeister documents user API keys for account/hotspot operations and documents Hoseline as a web-based talkgroup audio player. The public documentation reviewed for this project does not define a public realtime Last Heard/PTT event API or a native audio-stream contract. Therefore:

* Audio fallback is the official URL `https://hose.brandmeister.network/?subscribe=<TG>` opened by the user; the app does not scrape or bypass it.
* Metadata is abstracted behind `QsoProvider`. A production adapter should consume an authorized/local MMDVM or hotspot event feed and confirm start/end events before updating the UI.
* Country resolution is conservative and returns a globe when the offline prefix set cannot resolve a callsign; it must be expanded from a licensed/maintained prefix dataset before release.

## Current scope / known limitations

This checked-in baseline provides a responsive dark dashboard, talkgroup switching, lifecycle-safe ViewModel state, a 500-item in-memory history cap, conservative country display, and the provider seam. Room persistence, foreground audio service, CSV export, settings, signed artifacts, and physical API-24/current-device smoke testing remain dependent on an Android SDK/Gradle environment and a validated authorized event source. The current workspace had no Android SDK or Gradle installation available, so no APK build or live BrandMeister audio test is claimed.

The official Hoseline behavior and API-key boundary are documented by BrandMeister: https://help.brandmeister.network/last-heard-dash/hoseline/ and https://help.brandmeister.network/hotspots/api-manager/.
