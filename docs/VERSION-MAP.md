# QSO Vision version map

Repository: https://github.com/lim3soft/QSO-Vision

This map separates preserved artifacts from reconstructed history. The original
v1.0 source directory was overwritten in the Codex workspace; its original
project brief and source-generation trace are retained in Codex history.

| Version | Source status | APK download | Main changes |
|---|---|---|---|
| 1.0 | Reconstructed from the original Codex project trace; exact source snapshot unavailable | No preserved APK | Initial Android/Kotlin MMDVM QSO Vision dashboard, BrandMeister/Hoseline integration boundary, futuristic monitoring UI |
| 1.1 | Documented baseline in current source lineage | [Pre-1.3 APK](https://github.com/lim3soft/QSO-Vision/blob/main/artifacts/QSO-Vision-before-1.3.apk) | Single-talkgroup monitoring, WebView Hoseline fallback, demo separation, lifecycle/audio handling |
| 1.2 | Documented/reconstructed milestone; exact source snapshot unavailable | No separately preserved APK | Metadata integration, talkgroup persistence, current/last-heard filtering, QSO history |
| 1.3 | Current source lineage documented in `docs/feedback-1.3.md` | [1.3 APK](https://github.com/lim3soft/QSO-Vision/blob/main/artifacts/QSO-Vision-1.3.apk) | Metadata, audio, browser compatibility, and dimming improvements |
| 1.3.1 | Current source lineage documented in `docs/toolbar-1.3.1.md` | [1.3.1 APK](https://github.com/lim3soft/QSO-Vision/blob/main/artifacts/QSO-Vision-1.3.1.apk) | Toolbar and control refinements; version code 5 |
| 1.3.2a build 6 | [Current source](https://github.com/lim3soft/QSO-Vision/tree/main) | [1.3.2a APK](https://github.com/lim3soft/QSO-Vision/blob/main/artifacts/QSO-Vision-1.3.2a-build6.apk) | 70:30 landscape layout, rotation control, full-screen landscape, licensing, crash fixes |

## Codex source trace

The earliest retained project-generation and implementation trace is stored on
the build host at:

`/root/.codex/sessions/2026/10/08/rollout-2026-10-08T13-13-44-01a119ee-859f-7851-9834-af9be2e7a1f7.jsonl`

That trace is not included in this repository because it contains unrelated
Codex session data and is not a clean source snapshot.
