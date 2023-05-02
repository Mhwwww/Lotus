# GeoVER Frontend Demo

## Pre-request:
- Docker installed
- In the file [`CmdToTinyFaaS`](../geover/CmdToTinyFaaS.kt), change line 6 to the location of your tinyfaas.

## Try the Demo

### Backend
- start tinyfaas
- start Server.kt
- start the Application.kt

### Frontend
- input topic and geofence for subscription
- input rule and press 'Add Rule'
- after inputing all the rules, press 'Save Rules'
- press 'Show Warnings' to waiting for the matching events

### Matching Events
- start PublishingClient.kt(default topic is 'info')
- press 'Reload' after publishing events to see the 'warnings'

## Stop Subscriber
If you press enter twice in the subscriber, it will clean up after itself and destroy the tinyfaas function.
that we only perform the comuitation of condensing the message once (maybe this is deduplication?)