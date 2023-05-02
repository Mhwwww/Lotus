package de.hasenburg.geoverdemo.condensing.publisher

import de.hasenburg.geobroker.client.main.SimpleClient
import de.hasenburg.geobroker.commons.communication.ZMQProcessManager
import de.hasenburg.geobroker.commons.model.message.Payload
import de.hasenburg.geobroker.commons.model.message.ReasonCode
import de.hasenburg.geobroker.commons.model.spatial.Geofence
import de.hasenburg.geobroker.commons.model.spatial.Location
import de.hasenburg.geobroker.commons.randomInt
import de.hasenburg.geobroker.commons.setLogLevel
import de.hasenburg.geobroker.commons.sleep
import de.hasenburg.geoverdemo.condensing.common.locations
import de.hasenburg.geoverdemo.condensing.common.numberOfRepeats
import de.hasenburg.geoverdemo.condensing.common.numberOfUnnecessaryJson
import de.hasenburg.geoverdemo.condensing.common.publishTopic
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.json.JSONObject
import kotlin.random.Random
import kotlin.system.exitProcess

private val logger = LogManager.getLogger()

val alphabet: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
fun main() {
    setLogLevel(logger, Level.DEBUG)
    val processManager = ZMQProcessManager()

    // make a map of locations to clients
    val clients = mutableMapOf<Location, SimpleClient>()

    // connect
    locations.forEach {
        clients[it] = SimpleClient("localhost", 5559, identity = "CondensePublisher_${System.currentTimeMillis()}_${Random.nextInt()}")

        clients[it]!!.send(Payload.CONNECTPayload(it))
        logger.debug("ConnAck: {}", clients[it]!!.receive())
        sleep(100,0)
    }

    var i = 0
    repeat(numberOfRepeats) {
        locations.forEach{ currLocation ->
            val jsonObject = JSONObject()
            jsonObject.put("importantKey", randomInt(10000))
            repeat(numberOfUnnecessaryJson) {
                jsonObject.put(List(12) { alphabet.random() }.joinToString(""), randomInt(10000))
            }

            jsonObject.put("timeSent", System.nanoTime())

            val location = Geofence.circle(currLocation,2.0)
            clients[currLocation]!!.send(
                Payload.PUBLISHPayload(
                    publishTopic, location, jsonObject.toString()))
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