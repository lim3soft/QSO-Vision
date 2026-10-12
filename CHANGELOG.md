# QSO Vision changelog

For the version-to-source and APK mapping, see [docs/VERSION-MAP.md](docs/VERSION-MAP.md).

This changelog is reconstructed from the retained Codex project trace,
workspace notes, APK filenames, and verified implementation changes. Entries
marked as reconstructed do not represent a preserved historical source tree.

## 2026-10-11 — 1.3.2a build 6

- Refined responsive landscape UI for the QSO dashboard.
- Set the landscape content split to approximately 70% current QSO / 30%
  recent history.
- Moved `by 9W3SPY` beside the `QSO VISION` title in portrait and landscape.
- Kept the action controls grouped inside the current-QSO pane and aligned to
  its right edge.
- Added native landscape status-bar hiding with Android insets APIs.
- Preserved vertical scrolling for the history panel.
- Removed the 30-second lock-screen option after user feedback.
- Redesigned the screen-rotation icon to show portrait/landscape devices.
- Fixed the landscape reparenting crash by capturing views before moving them.
- Added Apache-2.0 project licensing and third-party license documentation.

## 2026-10-12 — 1.3.3 build 7

- Added resilient BrandMeister Last Heard table and column discovery after DOM changes.
- Added explicit metadata structure diagnostics instead of silently showing empty history.
- Added physical-resolution and density-aware dashboard scaling for compact phones.
- Added orientation-aware sizing while ignoring the device accessibility font-scale setting.
- Retained APK: `artifacts/QSO-Vision-1.3.3-build7.apk`.

## 2026-10-09 — 1.3.2a build 6

- Removed the experimental GeckoView dependency after APK size review.
- Restored system WebView playback; final APK returned to approximately 7.6 MB.
- Added manual rotation control and landscape layout behavior.
- Added display resolution/DPI profile capture on first launch.
- Added the requested dim modes: Always on, 30 seconds, 1 minute, 5 minutes,
  and Smart 10 seconds of silence.

## 2026-10-09 — 1.3.2 experimental build

- Experimentally bundled GeckoView for browser/audio independence.
- The resulting APK was approximately 504 MB because it included native
  browser libraries for multiple CPU architectures.
- This build was retained separately but is not part of the final release.

## 2026-10-08 — 1.3.1

- Toolbar and control interaction refinements.
- Documented as version code 5 / version name 1.3.1 in
  `docs/toolbar-1.3.1.md`.
- Retained APK: `artifacts/QSO-Vision-1.3.1.apk`.

## 2026-10-08 — 1.3

- Added feedback-driven metadata, audio, browser compatibility, and screen
  dimming improvements.
- Retained APK: `artifacts/QSO-Vision-1.3.apk`.

## Earlier project milestones — reconstructed

- **1.2:** BrandMeister metadata integration, talkgroup selection persistence,
  current/last-heard filtering, and history rendering.
- **1.1:** Single-talkgroup monitoring baseline, WebView Hoseline fallback,
  explicit demo separation, lifecycle, and audio-focus handling.
- **1.0:** Original MMDVM QSO Vision project brief and dashboard baseline,
  reconstructed from the earliest retained Codex project request. The exact
  original v1.0 source and APK were not retained in this workspace.
