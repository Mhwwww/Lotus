# GeoVER End Event Demonstration

## Devices
- 4 raspi: rasp-alpha is for _Weather Station_(run weather.jar), the others three for 3 airports(run publisher.jar).
- 1 laptop (in Schoenhagen Airport, another 1 laptop from DT).
- 3 fans

## Crosswind Setup
- All jar files are in the '/GeoBroker-Client/out' folder
- Start Server.kt

### raspi-alpha
- start tinyFaaS 
- start weather station by run
```shell
TINYFAAS_PATH="/home/pi/Documents/tinyFaaS/" FUNCTION_FILE_PATH="/home/pi/Documents/tinyFaaS/test/fns/ruleJson/" java -jar weather.jar
```
### the other raspis
- start publisher on each raspi by run one of the following commands. 

```shell
ADDRESS_AIRPORT="192.168.0.172" TINKERFORGE_HOST="192.168.0.172" PUBLISH_GEOFENCE="Frankfurt" STATION_ID=2 java -jar ./geover/publisher.jar
```
```shell
ADDRESS_AIRPORT="192.168.0.172" TINKERFORGE_HOST="192.168.0.172" PUBLISH_GEOFENCE="Berlin" STATION_ID=60 java -jar ./geover/publisher.jar      
```
```shell
ADDRESS_AIRPORT="192.168.0.172" TINKERFORGE_HOST="192.168.0.172" PUBLISH_GEOFENCE="Schoenhagen" STATION_ID=143 java -jar ./geover/publisher.jar
```

### Schönhagen Laptop
- start GeoVER.jar
- open frontent with url 'http://localhost:8082/index.html' (synchronize the url with the Ktor configuration in Configuration.kt)
- input subscription & rule
- click 'Show Warning' and wait for the messages.

## Temperature Setup
- need test the function as I upgraded the Nodejs version.