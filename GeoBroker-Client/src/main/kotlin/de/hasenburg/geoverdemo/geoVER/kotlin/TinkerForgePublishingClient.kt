

import com.tinkerforge.BrickletHumidityV2
import com.tinkerforge.BrickletTemperature
import com.tinkerforge.IPConnection
import com.tinkerforge.TimeoutException
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


fun main() {
    setLogLevel(logger, Level.DEBUG)
    // Create IP connection
    val ipcon = IPConnection()
    // Create device object
    val temp = BrickletTemperature(UID_TEMPERATURE, ipcon)
    val humi = BrickletHumidityV2(UID_HUMIDITY, ipcon)
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



