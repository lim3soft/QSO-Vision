# BrandMeister metadata integration verification

The official docs page https://api.brandmeister.network/docs/ links to
https://api.brandmeister.network/api-docs?api-docs.json .
The retrieved OpenAPI document identifies itself as Halligan API 2.1.0.

Documented paths include /talkgroup, /talkgroup/{id}, /registry/{id},
/registry/byCall/{call}, /master, /device and /selfcare paths.
No Last Heard, historical transmission list, or live PTT subscription is
documented in this specification. This is not evidence that no private
or separately authorized feed exists.

Audio and metadata can and should be independent. Hoseline supplies the
working audio player. A separate approved event feed must supply TG ID,
station ID or callsign, timestamps and start/end state. Registry lookups
can enrich events but cannot discover current transmissions. Recent
history can be recorded from received events; history before connecting
requires a separately available historical source.

The native app currently has no live metadata connection. Version 1.2
removes the demo toggle, Player controls button and receive-only subtitle,
adds the smaller by 9W3SPY attribution, and sets active callsign styling
to #FF6A00. No live metadata capability is claimed by this UI update.
Monitor applies the same validated ID to native state and the audio reload,
including when retrying the same ID. Existing history remains TG-filtered.

With player controls removed, a service login or browser-required gesture
cannot be completed through the native dashboard. Autoplay remains dependent
on the service and device WebView. Mute/unmute still stops/reloads the player.
