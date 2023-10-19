
import com.tinkerforge.BrickletOutdoorWeather
import com.tinkerforge.BrickletOutdoorWeather.StationData
import com.tinkerforge.IPConnection
import de.hasenburg.geobroker.client.main.SimpleClient
import de.hasenburg.geobroker.commons.communication.ZMQProcessManager
import de.hasenburg.geobroker.commons.model.message.Payload
import de.hasenburg.geobroker.commons.model.message.ReasonCode
import de.hasenburg.geobroker.commons.model.message.Topic
import de.hasenburg.geobroker.commons.model.spatial.Geofence
import de.hasenburg.geobroker.commons.model.spatial.Location
import de.hasenburg.geobroker.commons.randomDouble
import de.hasenburg.geobroker.commons.sleep
import org.apache.logging.log4j.LogManager
import org.json.JSONObject
import kotlin.random.Random
import kotlin.system.exitProcess

private const val HOST = "localhost"
private const val PORT = 4223
private const val UID_OUTDOORWEATHER = "ZPd"
private const val STATION_ID = 240

private val logger = LogManager.getLogger()

//TODO: enable listener(current doesn't work)
fun getStationData(): StationData {
    // Create IP connection--> Create device object--> Connect to brick daemon
    val ipcon = IPConnection()
    val outdoorWeather = BrickletOutdoorWeather(UID_OUTDOORWEATHER, ipcon)
    ipcon.connect(HOST, PORT)

    // Get station data--> [temperature = 216, humidity = 44, windSpeed = 0, gustSpeed = 0, rain = 48, windDirection = 2, batteryLow = false, lastChange = 41]
    val stationData = outdoorWeather.getStationData(STATION_ID)

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

    return stationData
}

fun main() {
    val publishTopic = Topic("info")
    var locations = Location(0.0, 0.0)

    logger.info("the input subscription's topic is: {}", publishTopic)

    val processManager = ZMQProcessManager()

    val client = SimpleClient("localhost", 5559)
    client.send(Payload.CONNECTPayload(locations))
    logger.info("Received server answer: {}", client.receive())

    var i = 0
    repeat(20) {
        val outdoorWeatherBrickletStationData = getStationData()

        val temperature = outdoorWeatherBrickletStationData.temperature
        val humidity= outdoorWeatherBrickletStationData.humidity
        val windSpeed = outdoorWeatherBrickletStationData.windSpeed
        val windDirection = outdoorWeatherBrickletStationData.windDirection
//
//        val gustSpeed = outdoorWeatherBrickletStationData.gustSpeed
//        val rain = outdoorWeatherBrickletStationData.rain
//        val batteryLow = outdoorWeatherBrickletStationData.batteryLow
//        val lastChange = outdoorWeatherBrickletStationData.lastChange
//
        //todo: the location should be just the sensor location
        locations = Location(Random.nextDouble(0.0, 2.0), Random.nextDouble(0.0, 2.0))

        val newElem = JSONObject().apply {
            put("publisherID", client.identity)
            put("timeSent", System.nanoTime())

            put("temperature", temperature)
            put("humidity", humidity)
            //todo: this is the sensor value
            //put("windVelocity", windSpeed)
            //TODO: delete later, here for crosswind function checking
            put("windVelocity", randomDouble(110.0, 140.0))
            //TODO: modify function or modify the data function here
            put("windDirection", "NW")
            put("windD", windDirection)

//            put("temperature", randomDouble(0.0, 60.0))
//            put("humidity", randomDouble(0.0, 60.0))
        }

        client.send(
            Payload.PUBLISHPayload(
                publishTopic,
                Geofence.circle(locations, 2.0),
                newElem.toString()
            )
        )

        logger.info("Publishing at {} topic {}", locations, publishTopic)
        logger.debug("PubAck: {}", client!!.receive())
        sleep(100, 0)
        logger.info("Sent message ${++i}")

    }

    //sleep(10000, 0)

    client!!.send(Payload.DISCONNECTPayload(ReasonCode.NormalDisconnection))
    client!!.tearDownClient()

    processManager.tearDown(3000)
    exitProcess(0)

}



