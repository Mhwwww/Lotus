package de.hasenburg.geoverdemo.multiRule.publisher

import de.hasenburg.geobroker.client.main.SimpleClient
import de.hasenburg.geobroker.commons.communication.ZMQProcessManager
import de.hasenburg.geobroker.commons.model.message.Payload
import de.hasenburg.geobroker.commons.model.message.ReasonCode
import de.hasenburg.geobroker.commons.model.message.Topic
import de.hasenburg.geobroker.commons.randomDouble
import de.hasenburg.geobroker.commons.setLogLevel
import de.hasenburg.geobroker.commons.sleep
import de.hasenburg.geoverdemo.geoVER.kotlin.publisher.*
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.json.JSONObject
import kotlin.system.exitProcess

private val logger = LogManager.getLogger()

class PublishingClient(){
    fun startPublisherClient() {
        setLogLevel(logger, Level.DEBUG)

        val publishTopic = Topic(PUB_TOPIC)
//        var locations = Location(0.0, 0.0)
        val locations = PUBLISH_GEOFENCE.center


        logger.info("the input subscription's topic is: {}", publishTopic)

        val processManager = ZMQProcessManager()

        val client = SimpleClient(ADDRESS, PORT)
        client.send(Payload.CONNECTPayload(locations))
        logger.info("Received server answer: {}", client.receive())

        var i = 0
        repeat(20) {
            //locations = Location(Random.nextDouble(0.0, 2.0), Random.nextDouble(0.0, 2.0))
            //locations = PUBLISHER_LOCATION

            val newElem = JSONObject().apply {
                put(TIME_SENT, System.nanoTime())
//                put(PUBLISHER_ID, client.identity)

                put(TEMPERATURE, randomDouble(0.0, 60.0))
                put(HUMIDITY, randomDouble(0.0, 60.0))

                put(WIND_VELOCITY, randomDouble(0.0, 64.0))
                put(WIND_DIRECTION, 14)
            }

            client.send(
                Payload.PUBLISHPayload(
                    publishTopic,
//                    Geofence.circle(locations, PUB_RADIUS),
                    PUBLISH_GEOFENCE,
                    newElem.toString()
                )
            )

            logger.info("Publishing at {} topic {}", locations, publishTopic)
            logger.debug("PubAck: {}", client!!.receive())

            sleep(PUB_INTERVAL, 0)
            logger.info("Sent message ${++i}: ${newElem.toString()} ")
        }

        client!!.send(Payload.DISCONNECTPayload(ReasonCode.NormalDisconnection))
        client!!.tearDownClient()

        processManager.tearDown(3000)
        exitProcess(0)
    }
}

fun main(){
    val publishClient = PublishingClient()
    publishClient.startPublisherClient()
}



