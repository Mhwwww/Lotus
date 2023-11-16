
import com.tinkerforge.BrickletOutdoorWeather
import com.tinkerforge.BrickletOutdoorWeather.StationData
import com.tinkerforge.IPConnection
import de.hasenburg.geobroker.client.main.SimpleClient
import de.hasenburg.geobroker.commons.communication.ZMQProcessManager
import de.hasenburg.geobroker.commons.model.message.Payload
import de.hasenburg.geobroker.commons.model.message.ReasonCode
import de.hasenburg.geobroker.commons.model.message.Topic
import de.hasenburg.geobroker.commons.sleep
import de.hasenburg.geoverdemo.geoVER.kotlin.publisher.*
import org.apache.logging.log4j.LogManager
import org.json.JSONObject
import kotlin.system.exitProcess

private val logger = LogManager.getLogger()
//TODO: enable listener(current doesn't work)
class OutdoorWeatherBrickletPublishingClient(){
    fun getStationData(stationID:Int): StationData {
        // Create IP connection--> Create device object--> Connect to brick daemon
        val ipcon = IPConnection()

        val outdoorWeather = BrickletOutdoorWeather(UID_OUTDOORWEATHER, ipcon)

        ipcon.connect(TINKERFORGE_HOST, TINKERFORGE_PORT)


        println(outdoorWeather.readUID())
        val stationData = outdoorWeather.getStationData(stationID)

        logger.info(stationData)

        // Get station data--> [temperature = 216, humidity = 44, windSpeed = 0, gustSpeed = 0, rain = 48, windDirection = 2, batteryLow = false, lastChange = 41]

        return stationData
    }

    fun startOutdoorBrickletPublisher(stationID: Int, address: String) {
        val publishTopic = Topic(PUB_TOPIC)
//        var locations = Location(0.0, 0.0)
        var locations = PUBLISH_GEOFENCE.center

        logger.info("the input subscription's topic is: {}", publishTopic)

        val processManager = ZMQProcessManager()

        val client = SimpleClient(address, PORT)
        client.send(Payload.CONNECTPayload(locations))

        logger.info("Received server answer: {}", client.receive())

        var i = 0

        repeat(REPEAT_TIME) {
            val outdoorWeatherBrickletPublishingClient = OutdoorWeatherBrickletPublishingClient()
            val outdoorWeatherBrickletStationData = outdoorWeatherBrickletPublishingClient.getStationData(stationID)

            //    BrickletOutdoorWeather.WIND_DIRECTION_N = 0
            //    BrickletOutdoorWeather.WIND_DIRECTION_NNE = 1
            //    BrickletOutdoorWeather.WIND_DIRECTION_NE = 2
            //    BrickletOutdoorWeather.WIND_DIRECTION_ENE = 3
            //    BrickletOutdoorWeather.WIND_DIRECTION_E = 4
            //    BrickletOutdoorWeather.WIND_DIRECTION_ESE = 5
            //    BrickletOutdoorWeather.WIND_DIRECTION_SE = 6
            //    BrickletOutdoorWeather.WIND_DIRECTION_SSE = 7
            //    BrickletOutdoorWeather.WIND_DIRECTION_S = 8
            //    BrickletOutdoorWeather.WIND_DIRECTION_SSW = 9
            //    BrickletOutdoorWeather.WIND_DIRECTION_SW = 10
            //    BrickletOutdoorWeather.WIND_DIRECTION_WSW = 11
            //    BrickletOutdoorWeather.WIND_DIRECTION_W = 12
            //    BrickletOutdoorWeather.WIND_DIRECTION_WNW = 13
            //    BrickletOutdoorWeather.WIND_DIRECTION_NW = 14
            //    BrickletOutdoorWeather.WIND_DIRECTION_NNW = 15
            //    BrickletOutdoorWeather.WIND_DIRECTION_ERROR = 255

            val temperature = outdoorWeatherBrickletStationData.temperature
            val humidity= outdoorWeatherBrickletStationData.humidity
//            val windSpeed = outdoorWeatherBrickletStationData.windSpeed //mps
            val windSpeed = outdoorWeatherBrickletStationData.gustSpeed

            val windDirection = outdoorWeatherBrickletStationData.windDirection

            //val windDirection = outdoorWeatherBrickletStationData.windDirection
//        val gustSpeed = outdoorWeatherBrickletStationData.gustSpeed
//        val rain = outdoorWeatherBrickletStationData.rain
//        val batteryLow = outdoorWeatherBrickletStationData.batteryLow
//        val lastChange = outdoorWeatherBrickletStationData.lastChange
//
            //todo: the location should be just the sensor location
//            locations = PUBLISHER_LOCATION
//            locations = PUBLISH_GEOFENCE.center

            val newElem = JSONObject().apply {
                //put("Publisher ID", client.identity)
                put(TIME_SENT, System.nanoTime())
                put(TEMPERATURE, temperature/10.0)
                put(HUMIDITY, humidity/1.0)

                // todo: need to expand values of wind speed
                put(WIND_VELOCITY, windSpeed * 1.94)//sensor value --> 1 meter per second = 1.94384449 knot
                put(WIND_DIRECTION, windDirection)

//                put(WIND_VELOCITY, randomDouble(0.0, 64.0))
            }

            client.send(
                Payload.PUBLISHPayload(
                    publishTopic,
//                    Geofence.circle(locations, 1.0),
                    PUBLISH_GEOFENCE,
                    newElem.toString()
                )
            )

            logger.info("Publishing at {} topic {}", locations, publishTopic)
            logger.debug("PubAck: {}", client!!.receive())
            sleep(PUB_INTERVAL, 0)
            logger.info("Sent message ${++i} to ${address}: ${newElem.toString()} from ${stationID}")

        }

        sleep(2000, 0)

        client!!.send(Payload.DISCONNECTPayload(ReasonCode.NormalDisconnection))
        client!!.tearDownClient()

        processManager.tearDown(3000)
        exitProcess(0)

    }
}




fun main(){


}



