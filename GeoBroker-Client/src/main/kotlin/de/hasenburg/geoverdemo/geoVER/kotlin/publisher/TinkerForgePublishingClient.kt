
import com.tinkerforge.*
import de.hasenburg.geobroker.client.main.SimpleClient
import de.hasenburg.geobroker.commons.communication.ZMQProcessManager
import de.hasenburg.geobroker.commons.model.message.Payload
import de.hasenburg.geobroker.commons.model.message.ReasonCode
import de.hasenburg.geobroker.commons.model.message.Topic
import de.hasenburg.geobroker.commons.model.spatial.Geofence
import de.hasenburg.geobroker.commons.model.spatial.Location
import de.hasenburg.geobroker.commons.randomDouble
import de.hasenburg.geobroker.commons.setLogLevel
import de.hasenburg.geobroker.commons.sleep
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.json.JSONObject
import kotlin.random.Random
import kotlin.system.exitProcess


private val logger = LogManager.getLogger()

private const val HOST = "localhost"
private const val PORT = 4223
private const val UID_TEMPERATURE = "EKx"
private const val UID_HUMIDITY = "HF1"
private const val UID_OUTDOORWEATHER = "***"

fun main() {
    setLogLevel(logger, Level.DEBUG)
    // Create IP connection
    val ipcon = IPConnection()
    // Create device object
    val temp = BrickletTemperature(UID_TEMPERATURE, ipcon)
    val humi = BrickletHumidityV2(UID_HUMIDITY, ipcon)
    //TODO: test wind use case related bricklet

     val outdoorWeather = BrickletOutdoorWeather(UID_OUTDOORWEATHER, ipcon)
    // Connect to brick daemon
    ipcon.connect(HOST, PORT)

    //Publisher
    val publishTopic = Topic("info")
    var locations = Location(0.0, 0.0)

    logger.info("the input subscription's topic is: {}", publishTopic)

    val processManager = ZMQProcessManager()

    val client = SimpleClient("localhost", 5559)
    client.send(Payload.CONNECTPayload(locations))
    logger.info("Received server answer: {}", client.receive())

    var i = 0
    repeat(3) {
        locations = Location(Random.nextDouble(0.0, 2.0), Random.nextDouble(0.0, 2.0))
        var temperature:Double
        var humidity:Double
        try {
            temperature = temp.temperature/100.0

            //TODO: test sensor data from outdoor weather bricklet
            //could also get temperature, humidity from outdoor weather bricklet

/*
                //var tempOutdoorWeather = outdoorWeather.SensorData().temperature
                //var humiOutdoorWeather = outdoorWeather.SensorData().humidity
                outdoorWeather.addStationDataListener(StationDataListener { identifier, temperature, humidity, windSpeed, gustSpeed, rain, windDirection, batteryLow ->
                println("Identifier (Station): $identifier")
                println("Humidity (Station): $humidity %RH")

                var tempOutdoorWeatherStation = temperature/10.0
                println("Temperature (Station): " +tempOutdoorWeatherStation+ " °C")
                var windSpeedOutdoorWeatherStation = windSpeed/10.0
                println("Wind Speed (Station): " +windSpeedOutdoorWeatherStation+ " m/s")
                var gustSpeedOutdoorWeatherStation = windSpeed/10.0
                println("Gust Speed (Station): " +gustSpeedOutdoorWeatherStation+ " m/s")
                var rainSpeedOutdoorWeatherStation = rain/10.0
                println("Rain (Station):"+ rainSpeedOutdoorWeatherStation+ " mm")

                if (windDirection == BrickletOutdoorWeather.WIND_DIRECTION_N) {
                    println("Wind Direction (Station): N")
                } else if (windDirection == BrickletOutdoorWeather.WIND_DIRECTION_NNE) {
                    println("Wind Direction (Station): NNE")
                } else if (windDirection == BrickletOutdoorWeather.WIND_DIRECTION_NE) {
                    println("Wind Direction (Station): NE")
                } else if (windDirection == BrickletOutdoorWeather.WIND_DIRECTION_ENE) {
                    println("Wind Direction (Station): ENE")
                } else if (windDirection == BrickletOutdoorWeather.WIND_DIRECTION_E) {
                    println("Wind Direction (Station): E")
                } else if (windDirection == BrickletOutdoorWeather.WIND_DIRECTION_ESE) {
                    println("Wind Direction (Station): ESE")
                } else if (windDirection == BrickletOutdoorWeather.WIND_DIRECTION_SE) {
                    println("Wind Direction (Station): SE")
                } else if (windDirection == BrickletOutdoorWeather.WIND_DIRECTION_SSE) {
                    println("Wind Direction (Station): SSE")
                } else if (windDirection == BrickletOutdoorWeather.WIND_DIRECTION_S) {
                    println("Wind Direction (Station): S")
                } else if (windDirection == BrickletOutdoorWeather.WIND_DIRECTION_SSW) {
                    println("Wind Direction (Station): SSW")
                } else if (windDirection == BrickletOutdoorWeather.WIND_DIRECTION_SW) {
                    println("Wind Direction (Station): SW")
                } else if (windDirection == BrickletOutdoorWeather.WIND_DIRECTION_WSW) {
                    println("Wind Direction (Station): WSW")
                } else if (windDirection == BrickletOutdoorWeather.WIND_DIRECTION_W) {
                    println("Wind Direction (Station): W")
                } else if (windDirection == BrickletOutdoorWeather.WIND_DIRECTION_WNW) {
                    println("Wind Direction (Station): WNW")
                } else if (windDirection == BrickletOutdoorWeather.WIND_DIRECTION_NW) {
                    println("Wind Direction (Station): NW")
                } else if (windDirection == BrickletOutdoorWeather.WIND_DIRECTION_NNW) {
                    println("Wind Direction (Station): NNW")
                } else if (windDirection == BrickletOutdoorWeather.WIND_DIRECTION_ERROR) {
                    println("Wind Direction (Station): Error")
                }
                println("Battery Low (Station): $batteryLow")
                println("")
            })*/



        }catch (e: TimeoutException){
            temperature = randomDouble(0.0, 60.0)
        }

        try {
            humidity = humi.humidity/100.0
        }catch (e: TimeoutException){
            humidity = randomDouble(0.0, 60.0)
        }


        //val temperature = temp.temperature/100.0
        //val humidity = humi.humidity/100.0

        //logger.error("current temp is:{}",temperature)

        val newElem = JSONObject().apply {
            //put("temperature", randomDouble(0.0, 60.0))
            //put("wet", randomDouble(0.0, 60.0))
            put("temperature", temperature)
            put("wet", humidity)
            put("speed", randomDouble(0.0, 60.0))
            put("wind", randomDouble(0.0, 60.0))

            put("timeSent", System.nanoTime())
            put("publisher ID", client.identity)
        }
        logger.info("Publishing at {} topic {}", locations, publishTopic)

        //newElem.put("timeSent", System.nanoTime())
        //newElem.put("publisher ID", client.identity)

        logger.debug("The Current Temperature is: {} and Humidity is: {}",temperature,humidity)

        client.send(
            Payload.PUBLISHPayload(
                publishTopic,
                Geofence.circle(locations, 2.0),
                newElem.toString()
            )
        )

        logger.debug("PubAck: {}", client!!.receive())
        sleep(100, 0)


        logger.info("Sent message ${++i}")

    }

    //keep publisher stay alive for a longer time
    //sleep(100, 0)

    client!!.send(Payload.DISCONNECTPayload(ReasonCode.NormalDisconnection))
    client!!.tearDownClient()

    processManager.tearDown(3000)
    exitProcess(0)
}



