package de.hasenburg.geoverdemo.cbf.publisher

import de.hasenburg.geobroker.client.main.SimpleClient
import de.hasenburg.geobroker.commons.communication.ZMQProcessManager
import de.hasenburg.geobroker.commons.model.message.Payload
import de.hasenburg.geobroker.commons.model.message.ReasonCode
import de.hasenburg.geobroker.commons.model.spatial.Geofence
import de.hasenburg.geobroker.commons.randomDouble
import de.hasenburg.geobroker.commons.setLogLevel
import de.hasenburg.geobroker.commons.sleep
import de.hasenburg.geoverdemo.cbf.common.*
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import kotlin.random.Random
import kotlin.system.exitProcess

private val logger = LogManager.getLogger()
fun main() {
    setLogLevel(logger, Level.DEBUG)
    val processManager = ZMQProcessManager()
    val client = SimpleClient("localhost", 5559, identity = "CbfPublisher_${System.currentTimeMillis()}_${Random.nextInt()}")

    // connect
    locations.forEach {
        client.send(Payload.CONNECTPayload(it))
        logger.debug("ConnAck: {}", client.receive())
        sleep(100,0)
    }

    repeat(numberOfRepeats) {
        locations.forEach{
            val temperature = 35 //randomDouble(0.0, 60.0)
            val speed = 15 //randomDouble(0.0, 60.0)
            val wind = randomDouble(0.0, 60.0)
            val wet = randomDouble(0.0, 60.0)
            val location = Geofence.circle(it,2.0)
            logger.debug("Publishing at {} topic {}", location, publishTopic)
            client.send(
                Payload.PUBLISHPayload(
                    publishTopic, location,"{\n" +
                            "    \"temperature\":${temperature},\n" +
                            "    \"speed\":${speed},\n" +
                            "    \"wind\":${wind},\n" +
                            "    \"wet\": ${wet}\n" +
                            "  }"))
            sleep(100, 0)
            logger.debug("PubAck: {}", client.receive())
        }
    }

    client.send(Payload.DISCONNECTPayload(ReasonCode.NormalDisconnection))

    client.tearDownClient()
    processManager.tearDown(3000)
    exitProcess(0)
}