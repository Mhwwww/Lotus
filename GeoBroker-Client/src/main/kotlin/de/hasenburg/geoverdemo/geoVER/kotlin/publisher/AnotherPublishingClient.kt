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

    logger.info("the input subscription's topic is: {}", publishTopic)

    val processManager = ZMQProcessManager()

    val client = SimpleClient("localhost", 5559)
    client.send(Payload.CONNECTPayload(locations))
    logger.info("Received server answer: {}", client.receive())

    var i = 0
    repeat(20) {
        locations = Location(Random.nextDouble(0.0, 2.0), Random.nextDouble(0.0, 2.0))
        val newElem = JSONObject().apply {
            put("timeSent", System.nanoTime())
            put("publisherID", client.identity)

            put("temperature", randomDouble(0.0, 60.0))
            put("humidity", randomDouble(0.0, 60.0))

            //put("wind", randomDouble(0.0, 60.0))
            put("windVelocity", randomDouble(110.0, 140.0))
            put("windDirection", "NW")
        }
        //newElem.put("timeSent", System.nanoTime())
        //newElem.put("publisher ID", client.identity)

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


