# QSO Vision 1.3.1

Scope: top-row controls only. Google-framework/WebView investigation explicitly deferred.

Order: numeric TG input, Monitor (play triangle), Mute/Unmute (speaker with waves or cross), Dim (sun), Exit (door/arrow).
Buttons use local vector icons, 48dp touch targets, screen-reader descriptions and long-press tooltips. Mute and dim menus retain existing behavior. Browser diagnostics remain available by tapping the connection-status text; its separate button was removed to keep controls in one row.

Build, lint and all nine JVM tests passed. No changes to audio service, metadata extraction or certificate configuration. Version code 5, version name 1.3.1.
