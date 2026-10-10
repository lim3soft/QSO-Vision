# QSO Vision 1.3 — feedback update / test report

Tested on this host on 2026-10-09. No hypervisor or host VM configuration was changed.

## Implemented

- Official Hoseline WebView moved into a foreground media-playback service with
  MediaSession, stop notification, audio focus and a partial wake lock. Activity
  stop/rotation no longer destroys audio. Mute and Exit stop the service; metadata
  remains independent of mute. No external audio player or invented stream API.
- Screen menu: always on, 30 seconds, 1/2/3/5/10/15 minutes, or Smart. Timed modes
  dim after touch inactivity; Smart dims after 10 seconds without confirmed PTT.
  Dimming sets this window to 5%, never changes the phone's system setting. Touch
  restores normal brightness; confirmed PTT restores it in Smart mode. Leaving
  the app clears the window override. This does not unlock/wake a manually locked
  phone. The screen stays awake at 5% while this dashboard is foreground.
- Callsign autosize maximum increased from 48sp to 72sp (+50%). Single-line
  autosizing protects long callsigns; orange #FF8A18 with a subtle orange shadow.
- 65 thin animated bars, ~30fps only during confirmed PTT. Decorative activity
  animation, NOT measured audio amplitude (Hoseline does not expose that here).
- Rounded Material buttons, cyan primary action, outlines/ripples, Material
  dialogs, system-bar insets, and fewer unnecessary history view rebuilds.
- Old-engine extractor compatibility: removed String.matchAll and
  NodeList.forEach dependencies. Talkgroup matching remains DestinationID/equal.

## Android 7 findings and fix

The API24 emulator's bundled Google WebView is 69.0.3497.100. Initially both
BrandMeister pages failed with SSL_UNTRUSTED (3). The website certificates use
Let's Encrypt. Android 7.0 lacks the appropriate ISRG roots.

The app now includes official ISRG Root X1/X2 public certificates, scoped ONLY to
brandmeister.network and its subdomains via Android Network Security Config.
System trust remains in effect. Cleartext is disabled; hostname, expiration and
certificate-chain checks are NOT bypassed. No user-installed CA is trusted by
this configuration. Certificates were fetched from:

- https://letsencrypt.org/certs/isrgrootx1.pem
- https://letsencrypt.org/certs/isrg-root-x2.pem
- Root X1 SHA256: 96:BC:EC:06:26:49:76:F3:74:60:77:9A:CF:28:C5:A7:CF:E8:A3:C0:AA:E1:1A:8F:FC:EE:05:C0:BD:DF:08:C6

After this change Hoseline loaded and created an audio track on API24, including
while the screen was asleep. BUT the current BrandMeister Last Heard website
then hit `Uncaught SyntaxError: Unexpected token .` in WebView69. That is a
website/browser compatibility problem, not the app's extractor. Users with old
WebViews must update Chrome / Android System WebView to the latest version
compatible with their device and make sure it is the active WebView provider.
The app now reports this explicitly and offers a WebView app-store link in
Browser compatibility. Chrome controls the provider on some Nougat devices.

No updated-WebView API24 end-to-end callsign success is claimed: an attempted
legacy package download was denied (HTTP403), and was not bypassed. The old
emulator has no signed-in Play Store. Do not describe Android7 metadata as fully
fixed for unupdated devices. Chrome119 is the final Nougat-supported major;
Nougat itself and that browser generation no longer receive current security
updates. A currently supported Android device is preferable for Internet use.

References:
- https://letsencrypt.org/docs/certificate-compatibility/
- https://developer.android.com/privacy-and-security/security-config
- https://groups.google.com/a/chromium.org/g/chromium-dev/c/B9AYI3WAvRo/m/tpWwhw4KBQAJ

## Executed checks

- `ANDROID_HOME=/usr/lib/android-sdk /opt/gradle-8.9/bin/gradle assembleDebug testDebugUnitTest lintDebug --console=plain`: PASS.
- Nine JVM tests: PASS (four screen-policy tests include all seven timer values,
  10-second Smart boundary, speech wake, touch wake, reset and always-on).
- Lint: 0 errors, 29 warnings; existing translation/resource/dependency warnings
  remain. Kapt also reports its Kotlin language-version fallback warning.
- `node qso-browser-check/legacy-extractor-test.cjs`: PASS. Clearly isolated test
  fixture, absent legacy JS APIs, TG exclusion, active marker, country/history.
- `node qso-browser-check/filter-test.cjs`: PASS with real official website
  results for 91 → 46020 → 91; no off-talkgroup rows returned.
- API35 Pixel AVD / WebView124: APK installed/launched; real filtered history
  visible, native UI inspected in portrait and landscape. Scroll is needed for
  lower controls/history in landscape. No app crash in inspected logs.
- Rotation retained the same Hoseline ServiceRecord and original creation time.
- Smart setting persisted as dimSeconds=-1; actual display diagnostics reported
  `Display Brightness=0.05` after inactivity. Voice wake logic was unit-tested;
  a genuine on-air transition was not captured in the screen-brightness test.
- API35: app-owned AAudio track was `state:started`, `mutedState:none` while
  `mWakefulness=Asleep`, after Home and Sleep. Verified again after more than a
  minute. This is runtime playback evidence, not a subjective listening test.
- API24 Pixel-profile AVD: install/launch and new buttons work. After trust fix,
  Hoseline loaded, and AudioFlinger showed active stream3 track owned by the app
  while `mWakefulness=Asleep`. Later idle checks showed no active track; sustained
  reception across future transmissions on this old WebView remains unverified.
- APK signature verification: PASS; Android debug certificate.

Both emulators run `-no-audio`: no claim of listening through a physical speaker
or checking intelligibility. OEM battery savers, long Doze periods, Bluetooth,
phone-call interruption, long-duration RF activity and physical phones remain
unverified. Users should allow background battery operation if their vendor
kills the service. Notification Stop, Mute, Exit or force-stop intentionally
stop playback. Browser/provider session restrictions still apply.

## Artifact

- Version 1.3 / code4; minSdk24, target/compile35.
- `app/build/outputs/apk/debug/app-debug.apk`
- SHA256: ed720488294a1f6746ef5e78dc6be02da16fbf560c1a5763e3667e5407ae2e83
- Install over the existing identically signed app; do not uninstall just to
  update, because uninstalling removes saved settings.

Existing history persistence and other original-project limitations are not
expanded by this feedback update. No demo events are introduced into monitoring.
