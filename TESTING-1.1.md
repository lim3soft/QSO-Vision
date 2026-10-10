# QSO Vision 1.1

Single talkgroup selection cancels the previous metadata subscription and filters displayed history by the current talkgroup. Normal mode has no connected live metadata provider. Previously the app generated fake callsigns in its default mode; this version disables that behavior. Explicit demo mode provides labeled start/end events for visual testing only.

The dashboard covers the attached Hoseline WebView. Player controls can be revealed when the service requires user interaction. This does not scrape Hoseline or derive PTT from its audio. Mute destroys the player and releases its page resources; unmute recreates it and reloads the selected subscription. Reconnection can take time and Hoseline may require a user gesture. This is stop/resume mute, not seamless gain control. System media volume is never changed.

The animation is a PTT activity indicator, not an audio amplitude measurement. Normal mode shows the current TG and local clock/date with seconds, explicitly noting live PTT is unavailable. History contains only selected-group demo transmissions when demo is enabled. This release does not supply real Last Heard metadata.

The player is stopped when the app leaves the foreground. Rotation preserves ViewModel data but reloads audio. Screen-off/background audio and end-to-end hidden WebView playback have not been verified on a device.

References:
- https://help.brandmeister.network/last-heard-dash/hoseline/
- https://developer.android.com/reference/android/webkit/WebView
