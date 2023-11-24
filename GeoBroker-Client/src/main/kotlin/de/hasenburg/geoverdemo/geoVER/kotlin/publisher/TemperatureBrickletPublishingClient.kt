
import de.hasenburg.geobroker.client.main.SimpleClient
import de.hasenburg.geobroker.commons.communication.ZMQProcessManager
import de.hasenburg.geobroker.commons.model.message.Payload
import de.hasenburg.geobroker.commons.model.message.ReasonCode
import de.hasenburg.geobroker.commons.model.message.Topic
import de.hasenburg.geobroker.commons.randomDouble
import de.hasenburg.geobroker.commons.sleep
import de.hasenburg.geoverdemo.geoVER.kotlin.BROKER_HOST
import de.hasenburg.geoverdemo.geoVER.kotlin.publisher.*
import org.apache.logging.log4j.LogManager
import org.json.JSONObject
import kotlin.system.exitProcess


private val logger = LogManager.getLogger()

    fun temperaturePublisher(address: String){
        val publishTopic = Topic(TEMPERATURE_PUB_TOPIC)
        var locations = PUBLISH_GEOFENCE.center

        logger.info("the input subscription's topic is: {}", publishTopic)

        val processManager = ZMQProcessManager()

        val client = SimpleClient(address, PORT)
        client.send(Payload.CONNECTPayload(locations))

        logger.info("Received server answer: {}", client.receive())

        var i = 0

        repeat(REPEAT_TIME) {

//            val ipcon = IPConnection()
//            val temperatureBricklet = BrickletTemperature("EKx", ipcon)
//
//            ipcon.connect(TINKERFORGE_HOST, TINKERFORGE_PORT)
//
//            val currentTemperature = temperatureBricklet.temperature/100.0
//            println("Current Temperature is $currentTemperature")

            val newElem = JSONObject().apply {
                //put("Publisher ID", client.identity)
                put(TIME_SENT, System.nanoTime())
//                put(TEMPERATURE, currentTemperature)
                put(TEMPERATURE, randomDouble(0.0, 30.0))
            }

            client.send(
                Payload.PUBLISHPayload(
                    publishTopic,
                    PUBLISH_GEOFENCE,
                    newElem.toString()
                )
            )

            logger.info("Publishing at {} topic {}", locations, publishTopic)
            logger.debug("PubAck: {}", client!!.receive())
            sleep(PUB_INTERVAL, 0)
            logger.info("Sent message ${++i} to ${address}: ${newElem.toString()}")

        }

//        sleep(2000, 0)

        client!!.send(Payload.DISCONNECTPayload(ReasonCode.NormalDisconnection))
        client!!.tearDownClient()

        processManager.tearDown(3000)
        exitProcess(0)

    }


fun main(){
    temperaturePublisher(BROKER_HOST)

}


