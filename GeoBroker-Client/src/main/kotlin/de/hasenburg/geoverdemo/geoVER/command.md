mkdir geover
scp /Users/minghe/geobroker/GeoBroker-Client/out/publisher.jar raspi-alpha:~/geover

ADDRESS="192.168.0.172" STATION_ID=143 TINKERFORGE_HOST="192.168.0.172" PUBLISH_GEOFENCE="Schoenhagen Airport" java -jar publisher.jar


ADDRESS="192.168.0.172" STATION_ID=143 TINKERFORGE_HOST="192.168.0.172" PUBLISH_GEOFENCE="Berlin Airport" java -jar publisher.jar

TINYFAAS_PATH="/home/pi/Documents/tinyFaaS/" FUNCTION_FILE_PATH="/home/pi/Documents/tinyFaaS/test/fns/ruleJson/" java -jar weather.jar

TINYFAAS_PATH="/Users/minghe/geobroker/tinyFaaS/" FUNCTION_FILE_PATH="/Users/minghe/geobroker/GeoBroker-Client/src/main/kotlin/de/hasenburg/geoverdemo/crossWind/subscriber/ruleJson/" java -jar weather.jar
