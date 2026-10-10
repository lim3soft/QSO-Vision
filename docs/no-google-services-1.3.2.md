# QSO Vision 1.3.2 — older/no-Google-services compatibility

The app does not use Google Play Services, Google Services Framework, Firebase,
or Play Store APIs for audio or callsign monitoring. It uses Android's system
WebView plus the official BrandMeister HTTPS pages.

On the host's Pixel emulator, Google Play Services and GSF were disabled. The
existing QSO Vision session continued to return 200 filtered TG91 records and
AudioFlinger reported a started app-owned media track. This confirms that the
absence of Google services alone does not prevent monitoring.

Added `BrowserSupport`: WebView initialization failures are caught and reported
instead of crashing or silently waiting. The connection-status text opens device
guidance explaining that a usable system WebView is required. The app does not
silently install a WebView APK, change the provider, bypass TLS validation, or
open an external audio player.

Android chooses the WebView provider supplied by the device ROM. AOSP/non-GMS
phones can work if they ship an enabled, sufficiently current `com.android.webview`
or another supported provider. On Android 7, the factory Chromium 69 engine
cannot run the current BrandMeister JavaScript even after the official CA-root
compatibility fix; it needs the newest provider supported by that device.

The bundled GeckoView approach was not added: current Mozilla documentation says
GeckoView/Fenix requires Android 8+, so it cannot solve API24/25 devices without
introducing a separate legacy engine with significant security and APK-size
tradeoffs. Android 8+ devices without GMS may be candidates for a later bundled
engine evaluation.

Build: version 1.3.2 / code 6. The WebView fallback remains intentionally
deferred; no claim is made that an unupdated Android 7 browser can parse
BrandMeister.
