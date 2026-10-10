# QSO Vision version history

This timeline combines the earliest retained Codex project brief, workspace
documentation, APK backups, and later user-requested updates. The original
v1.0 source snapshot was not retained, so the v1.0 entry is explicitly a
reconstructed milestone rather than a byte-for-byte historical source import.

## v1.0 — reconstructed project baseline

- Android/Kotlin MMDVM QSO Vision project established.
- Dark futuristic monitoring dashboard concept.
- BrandMeister talkgroup monitoring and official Hoseline fallback defined.
- Live metadata provider boundary and conservative country resolution defined.

## v1.1

- Single-talkgroup monitoring baseline.
- Native dashboard with WebView-based Hoseline fallback.
- Demo provider explicitly separated from live traffic.
- Lifecycle and audio-focus handling documented.

## v1.2

- BrandMeister metadata integration and current/last-heard filtering.
- Talkgroup selection persistence and QSO history rendering.
- Network/browser compatibility diagnostics.

## v1.3

- Audio, metadata, toolbar, screen-dimming, and compatibility improvements.
- Android 7+ support and no-Google-services compatibility work.

## v1.3.1

- Toolbar and control interaction refinements.
- Build code 5, version name 1.3.1, documented in `docs/toolbar-1.3.1.md`.

## v1.3.2 / v1.3.2a build 6

- Responsive landscape dashboard.
- Portrait/landscape manual rotation control and orientation icon.
- 70:30 landscape current-QSO/history layout.
- Shared QSO Vision / by 9W3SPY header.
- Landscape status-bar hiding through native Android insets APIs.
- Removed bundled GeckoView to preserve the lightweight APK size.
- Final version name: `1.3.2a`, version code: `6`.
