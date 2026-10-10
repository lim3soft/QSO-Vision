# QSO Vision — resume after VM power cycle

## Immediate next steps

1. Verify `vmx` in `/proc/cpuinfo`, load `kvm_intel` if necessary, check `/dev/kvm`, and run `/usr/lib/android-sdk/emulator/emulator -accel-check`.
2. Boot existing AVD `QsoVisionPixel` with Android SDK `/usr/lib/android-sdk`. Google APIs Android 35 x86_64 image and emulator are installed. Run headless with software graphics as required. Do not wipe the AVD unnecessarily.
3. Install `/root/app/build/outputs/apk/debug/app-debug.apk`, launch `com.mmdvm.qsovision/.MainActivity`, collect logcat and WebView diagnostics, reproduce missing callsigns on TG91 and user-selected TG46020.
4. Diagnose actual live metadata transport with observed traffic and errors before building another APK. Audio reportedly works; callsign and history do NOT work on user's device. Prior build/unit/lint successes did not validate live integration.
5. Fix and test genuine live callsign/history filtering, TG switches, mute independence, and active/end transitions. Copy verified APK to `/home/debian/app-debug.apk`. Report precise live/device test evidence and remaining limitations.

## Host and restart

This Debian workspace is Proxmox VM 102, UUID 3991597b-f93d-4dd8-bb91-e026958205f4. Verified by matching guest DMI UUID to qm config.
Correct Proxmox host: 192.168.0.199, root. Credentials were supplied in conversation; intentionally not saved here.
Do NOT change 192.168.14.50: that was the user's mistaken original host.
Nested Intel virtualization is already enabled (Y) on correct Proxmox host.
VM 102 CPU change from x86-64-v2 to host is saved as pending. A graceful full shutdown/start, not guest reboot alone, activates it.
User explicitly authorized restarting THIS host via Proxmox after saving task state.
Other VM 100 (OpenWRT) must be left running. Do not reboot Proxmox itself.

Guest interface ens18 originally has 192.168.14.58/24 and 192.168.100.100/24; default gateway 192.168.14.254.
Added temporary 192.168.0.18/24 initially with noprefixroute. User subsequently authorized connected route `192.168.0.0/24 dev ens18 src 192.168.0.18`; it is now installed. Default route unchanged. These temporary additions may disappear on shutdown; restore only as needed under existing authorization.

## Project

Project root /root; Kotlin Android app, package com.mmdvm.qsovision. Label QSO Vision; min API24, compile/target35.
Primary file app/src/main/java/com/mmdvm/qsovision/MainActivity.kt. Single TG native dashboard, input Monitor, mute, Exit, hidden Hoseline playback.
Build: ANDROID_HOME=/usr/lib/android-sdk /opt/gradle-8.9/bin/gradle --no-daemon --offline --max-workers=2 assembleDebug testDebugUnitTest lintDebug
Latest delivered APK SHA256: 79e89d9f988ba16baabf4c2d21349ac0b29f0fd55f3b2210b73f294a8e53f35e. Check actual files on resume.

## Metadata investigation / known problems

Numerous unverified DOM parser tweaks failed. Do not repeat claims that successful builds prove live data works.
Official REST spec saved at docs/brandmeister-openapi.json; no documented live Last Heard endpoint found there. Absence from REST spec does not prove other feeds do not exist.
Public third-party reference https://github.com/n8acl/bm_monitor uses python-socketio with https://api.brandmeister.network and socketio_path /lh/socket.io, transports websocket, mqtt payload events. Current availability/permissions/protocol must be verified; reference source alone is not live verification.
Current app injects handwritten Engine.IO4/WebSocket handling into remote Last Heard WebView, exposes QsoVision JavascriptInterface, uses liveSocketStarted flag. This can fail due to navigation discarding injected script, CSP, timing, protocol assumptions, connection errors, or event shape. Do not mark connected before handshake. Add visible metadata diagnostics and use a proper client if verified appropriate.
Current code filters DestinationID then processes SourceCall/Start/Stop/Event (Session-Start/Update/Stop). Recheck TG inside UI-thread callback to prevent switch races. It lacks robust dedupe/timeouts and country enrichment.
Remaining old DOM parser has overescaped JavaScript regex in Kotlin raw strings, may never create initial search rule, incorrectly marks latest completed row active, and adds only first row. Avoid presenting it as functional.
Country currently globe in direct event handler. History memory-only in MainActivity despite Room scaffolding. Full requirements are not met.
No physical Android or emulator runtime test has succeeded yet. Emulator previously failed because CPU VMX was not exposed.
Browser diagnostics tools under /root/qso-browser-check (Playwright and Chromium); elevated execution may be required for network/sockets. Previous sandbox DNS failures must be retried with authorized escalation rather than treated as service failure.

## User expectations

Keep native dashboard; do not display BrandMeister webpage. Use actual BrandMeister metadata, no fake/demo normal activity. Single entered TG controls audio/live/history; mute must not stop metadata. Current caller neon orange, title QSO VISION with smaller by 9W3SPY. User requests frequent meaningful progress updates and emulator test on this host before more installation attempts.
