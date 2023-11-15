# GeoVER Frontend Demo

## Pre-request:
- Docker installed
- InfluxDB and Grafana installed


## Configuration for Subscriber:
- Configure related parameters in the file [`Configuration.kt`](../geoVER/kotlin/Configuration.kt).
  - For tinyFaaS
    - change line15 to your tinyFaaS path.
    - change line16 to your function path.
  - To use Ktor frontend, you need to make sure the port is not in use.
  - If you want to use your own InfluxDB, change lines 30-35 to your own InfluxDB information.

## Configuration for Publisher:
- Configure related parameters in the file [`PubConfiguration.kt`](../geoVER/kotlin/publisher/PubConfiguration.kt).
  - If you would like to publish Tinkerforge sensor data, use line149 instead of line146 in the main function and change line 23-27 to your Tinkerforge Configuration.


## Try the Demo
### Backend
- start tinyfaas.
- run Server.kt.
- run the TalkToXR.kt.

### Frontend
- input topics and geofence for subscription.
- input rule and press 'Add Rule'.
- press 'Save Rules' to save inputted rules.
- press 'Show Warnings', waiting for the matching events.

### Start Publisher
- run [`PubConfiguration.kt`](../geoVER/kotlin/publisher/PubConfiguration.kt).(default topic is 'info')
- after publishing events, press 'Show Warnings/Show Information' button on the right top of the frontend page to see the 'warnings/information(geo-fence matching events)' 

### Change Message Priority
- The default priority of warnings is _TRUE_, information is _FALSE_.
- Warning could be changed to information by clicking the _Warning_ on the webpage.




## Stop Subscriber
If you press enter twice in the subscriber, it will clean up after itself and destroy the tinyfaas function.
that we only perform the communication of condensing the message once (maybe this is deduplication?)