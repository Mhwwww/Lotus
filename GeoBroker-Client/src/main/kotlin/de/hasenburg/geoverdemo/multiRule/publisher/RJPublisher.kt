package de.hasenburg.geoverdemo.crossWind.publisher

import de.hasenburg.geobroker.client.main.SimpleClient
import de.hasenburg.geobroker.commons.communication.ZMQProcessManager
import de.hasenburg.geobroker.commons.model.message.Payload
import de.hasenburg.geobroker.commons.model.message.ReasonCode
import de.hasenburg.geobroker.commons.model.spatial.Geofence
import de.hasenburg.geobroker.commons.model.spatial.Location
import de.hasenburg.geobroker.commons.randomDouble
import de.hasenburg.geobroker.commons.setLogLevel
import de.hasenburg.geobroker.commons.sleep
import de.hasenburg.geoverdemo.crossWind.common.locations
import de.hasenburg.geoverdemo.crossWind.common.numberOfRepeats
import de.hasenburg.geoverdemo.crossWind.common.publishTopic
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
    val clients = mutableMapOf<Location, SimpleClient>()
    // connect
    locations.forEach {
        clients[it] = SimpleClient("localhost", 5559, identity = "RJPublisher_${System.currentTimeMillis()}_${Random.nextInt()}")
        clients[it]!!.send(Payload.CONNECTPayload(it))
        logger.debug("ConnAck: {}", clients[it]!!.receive())
        sleep(100,0)
    }

    var i = 0
    repeat(numberOfRepeats) {
        locations.forEach{ currLocation ->
//            val temperature = 35 //randomDouble(0.0, 60.0)
//            val speed = 15 //randomDouble(0.0, 60.0)
//            val wind = 0 //randomDouble(0.0, 60.0)
//            val wet = 60 // randomDouble(0.0, 60.0)

            val newElem = JSONObject().apply {
                put("temperature", randomDouble(0.0, 60.0))
                put("speed", randomDouble(0.0, 60.0))
                put("wind", randomDouble(0.0, 60.0))
                put("wet", randomDouble(0.0, 60.0))
            }

            val location = Geofence.circle(currLocation,2.0)
//            val location = Geofence.world()
            logger.debug("Publishing at {} topic {}", location, publishTopic)
            newElem.put("timeSent", System.nanoTime())
            clients[currLocation]!!.send(
                Payload.PUBLISHPayload(
                    publishTopic, location, newElem.toString()))
            logger.debug("PubAck: {}", clients[currLocation]!!.receive())
            sleep(100, 0)
            logger.info("Sent message ${++i}")

        }
    }

    locations.forEach {
        clients[it]!!.send(Payload.DISCONNECTPayload(ReasonCode.NormalDisconnection))

        clients[it]!!.tearDownClient()
    }

    processManager.tearDown(3000)
    exitProcess(0)
}