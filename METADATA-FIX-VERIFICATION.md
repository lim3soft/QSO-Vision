# Metadata fix verification (in progress)

The previously shipped APK did not have a verified live metadata integration.
Build success, unit tests, and lint did not demonstrate network functionality.

Observed on this host after enabling nested virtualization:
- KVM acceleration check succeeds; /dev/kvm exists.
- The previous /lh/socket.io WebSocket performs a handshake and heartbeat but delivered no activity in a 35-second check.
- The current official Last Heard page receives live activity through its own connection. The app now lets the official page manage that connection and uses only the visible search controls and rendered rows.
- The shared `app/src/main/assets/lastheard.js` script created a rule, set Destination ID, equal, then TG91, and submitted Search. It returned 200 genuine TG91 rows including active A46UNX (OM) and completed EA8DFX (ES).
- Initial rule creation and separate render cycles for field/value changes were missing in prior extraction attempts. The old Kotlin-embedded JavaScript also had overescaped regexes.

The new app removes the obsolete handwritten socket connection and JavaScript bridge. Its hidden official WebView supplies native fields, with metadata status text. Audio is still the separate Hoseline WebView.

Remaining verification: APK on Android emulator, TG switching and mute independence. Android 35 first boot required resetting unused AVD data due to an encryption initialization failure. No user phone data was modified.

Limitations: rendered-page integration depends on the BrandMeister website and Android System WebView compatibility; 1-second sampling may miss very short active states; only source-confirmed active rows are displayed as ON AIR. Native history is a bounded in-memory snapshot of completed source rows, not full persistent Room event storage. Relative age and duration come from the source; exact timestamps are not fabricated. Audio synchronization is not assumed.
