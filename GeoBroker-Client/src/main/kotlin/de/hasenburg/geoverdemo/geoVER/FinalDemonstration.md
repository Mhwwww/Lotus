# GeoVER End Event Demonstration

## Devices
- 3 raspis: publishers on three airport. Schoenhagen running 'MultithreadPublisher.jar'(including weather station data, fake data to DT, temperature data from Temperature Bricklet), the other two running 'OutdoorWeatherPublisher.jar'
- 3 laptop (1 in Schoenhagen Airport(another 1 laptop from DT); 1 for Weather Station, 1 to host Zoom meeting).
- 3 fans

## Crosswind Setup
- All jar files are in the '/GeoBroker-Client/out' folder

- Start Influx DB
- Start Docker and tinyFaaS
- Start Server.kt

[//]: # (### raspi-alpha)

[//]: # (- start tinyFaaS )

[//]: # (- start weather station by run)

[//]: # (```shell)

[//]: # (TINYFAAS_PATH="/home/pi/Documents/tinyFaaS/" FUNCTION_FILE_PATH="/home/pi/Documents/tinyFaaS/test/fns/ruleJson/" java -jar weather.jar)

[//]: # (```)

### the three raspis(Station ID should be settled at the last try)
- start publisher on each raspi by run one of the following commands. 

```shell
BROKER_ADDRESS="192.168.0.172" TINKERFORGE_HOST="192.168.0.172"  PUBLISH_GEOFENCE="Dresden" STATION_ID=143 java -jar ./geover/OutdoorWeatherPublisher.jar
```
```shell
BROKER_ADDRESS="192.168.0.172" TINKERFORGE_HOST="192.168.0.172" PUBLISH_GEOFENCE="Hamburg" STATION_ID=143 java -jar ./geover/OutdoorWeatherPublisher.jar      
```
```shell
BROKER_ADDRESS="192.168.0.172" TINKERFORGE_HOST="192.168.0.172" PUBLISH_GEOFENCE="Schoenhagen" STATION_ID=143 java -jar ./geover/MultiThreadPublisher.jar
```

### Schönhagen Laptop
- start GeoVER.jar/ just run code from IntelliJ
- open frontent with url 'http://localhost:8082/index.html' (synchronize the url with the Ktor configuration in Configuration.kt)
- input subscription(topic is 'crosswind') & rule ('aircraft' & 'runway') & function (choose crosswind)
- click 'Show Warning' and wait for the messages.

## Temperature Setup
- choose function to be 'snow clearing' on the frontend page.
- topic is 'snow' and self-defined target topic.
- input rules like 'temperature < 2'.
- click 'Show Warning' and wait for the messages.

