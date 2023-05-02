package de.hasenburg.geoverdemo.multiRule.publisher

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
fun main() {
    setLogLevel(logger, Level.DEBUG)
    val publishTopic = Topic("info")
    var locations = Location(0.0, 0.0)

    logger.error(publishTopic)

    val processManager = ZMQProcessManager()

    val client = SimpleClient("localhost", 5559)
    client.send(Payload.CONNECTPayload(locations))
    logger.info("Received server answer: {}", client.receive())

    var i = 0
    repeat (30){
       //TODO: sent different content events in same location
        locations = Location(Random.nextDouble(0.0,2.5),Random.nextDouble(0.0,2.5))
        val newElem = JSONObject().apply {
            put("temperature", randomDouble(0.0, 60.0))
            put("speed", randomDouble(0.0, 60.0))
            put("wind", randomDouble(0.0, 60.0))
            put("wet", randomDouble(0.0, 60.0))
        }
       logger.error("Publishing at {} topic {}", locations, publishTopic)
       newElem.put("timeSent", System.nanoTime())
       client.send(Payload.PUBLISHPayload(
           publishTopic, Geofence.circle(locations,2.0), newElem.toString()))

       logger.debug("PubAck: {}", client!!.receive())
       sleep(100, 0)

       logger.info("Sent message ${++i}")

   }


    client!!.send(Payload.DISCONNECTPayload(ReasonCode.NormalDisconnection))
    client!!.tearDownClient()

    processManager.tearDown(3000)
    exitProcess(0)
}
