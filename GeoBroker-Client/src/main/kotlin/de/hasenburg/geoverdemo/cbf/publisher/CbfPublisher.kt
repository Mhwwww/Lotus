package de.hasenburg.geoverdemo.cbf.publisher

import de.hasenburg.geobroker.client.main.SimpleClient
import de.hasenburg.geobroker.commons.communication.ZMQProcessManager
import de.hasenburg.geobroker.commons.model.message.Payload
import de.hasenburg.geobroker.commons.model.message.ReasonCode
import de.hasenburg.geobroker.commons.model.spatial.Geofence
import de.hasenburg.geobroker.commons.model.spatial.Location
import de.hasenburg.geobroker.commons.randomDouble
import de.hasenburg.geobroker.commons.setLogLevel
import de.hasenburg.geobroker.commons.sleep
import de.hasenburg.geobroker.server.storage.client.Client
import de.hasenburg.geoverdemo.cbf.common.*
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.json.JSONObject
import kotlin.random.Random
import kotlin.system.exitProcess

private val logger = LogManager.getLogger()
fun main() {
    setLogLevel(logger, Level.DEBUG)
    val processManager = ZMQProcessManager()

    // make a map of locations to clients
    val client = SimpleClient("localhost", 5559, identity = "CbfPublisher_${System.currentTimeMillis()}_${Random.nextInt()}")

    // connect
    locations.forEach {
        client.send(Payload.CONNECTPayload(it))
        logger.debug("ConnAck: {}", client.receive())
        sleep(100,0)
    }

    repeat(numberOfRepeats) {
        locations.forEach{ currLocation ->
//            val temperature = 35 //randomDouble(0.0, 60.0)
//            val speed = 15 //randomDouble(0.0, 60.0)
//            val wind = 0 //randomDouble(0.0, 60.0)
//            val wet = 60 // randomDouble(0.0, 60.0)

            val newElem = JSONObject().apply {
                put("temperature", 35)
                put("speed", 15)
                put("wind", 0)
                put("wet", 60)
            }

            val location = Geofence.circle(currLocation,2.0)
            logger.debug("Publishing at {} topic {}", location, publishTopic)
            client.send(
                Payload.PUBLISHPayload(
                    publishTopic, location, newElem.toString()))
            logger.debug("PubAck: {}", client.receive())
            sleep(100, 0)

        }
    }

    client.send(Payload.DISCONNECTPayload(ReasonCode.NormalDisconnection))

    client.tearDownClient()

    processManager.tearDown(3000)
    exitProcess(0)
}