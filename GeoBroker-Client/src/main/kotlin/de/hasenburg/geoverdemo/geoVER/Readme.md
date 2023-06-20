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
- start _PublishingClient.kt_ in "/geoVER/publisher" folder.(default topic is 'info')
- press 'Show Warnings' after publishing events to see the 'warnings' 
- press 'Show Information' after publishing events to see the geo-fence matching events.
- 
### Event Priority
- The default priority of warnings is TRUE, information is FALSE.
- Warning priority could be changed by clicking, and this warning will be removed from current warning list to info list.

## Stop Subscriber
If you press enter twice in the subscriber, it will clean up after itself and destroy the tinyfaas function.
that we only perform the comuitation of condensing the message once (maybe this is deduplication?)