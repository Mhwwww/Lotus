package de.hasenburg.geoverdemo.condensing.publisher

import de.hasenburg.geobroker.client.main.SimpleClient
import de.hasenburg.geobroker.commons.communication.ZMQProcessManager
import de.hasenburg.geobroker.commons.model.message.Payload
import de.hasenburg.geobroker.commons.model.message.ReasonCode
import de.hasenburg.geobroker.commons.model.spatial.Geofence
import de.hasenburg.geobroker.commons.randomInt
import de.hasenburg.geobroker.commons.setLogLevel
import de.hasenburg.geobroker.commons.sleep
import de.hasenburg.geoverdemo.condensing.common.*
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import kotlin.system.exitProcess
import org.json.JSONObject
import kotlin.random.Random

private val logger = LogManager.getLogger()

val alphabet: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
fun main() {
    setLogLevel(logger, Level.DEBUG)
    val processManager = ZMQProcessManager()
    val client = SimpleClient("localhost", 5559, identity = "CondensePublisher_${System.currentTimeMillis()}_${Random.nextInt()}")

    // connect
    locations.forEach {
        client.send(Payload.CONNECTPayload(it))
        logger.debug("ConnAck: {}", client.receive())
        sleep(100,0)
    }

    repeat(numberOfRepeats) {
        locations.forEach{ currLocation ->
            val jsonObject = JSONObject()
            jsonObject.put("importantKey", randomInt(10000))
            repeat(numberOfUnnecessaryJson) {
                jsonObject.put(List(12) { alphabet.random() }.joinToString(""), randomInt(10000))
            }

            client.send(
                Payload.PUBLISHPayload(
                    publishTopic, Geofence.circle(currLocation,2.0),jsonObject.toString()))
            logger.debug("PubAck: {}", client.receive())
            sleep(100, 0)
        }
    }

    client.send(Payload.DISCONNECTPayload(ReasonCode.NormalDisconnection))

    client.tearDownClient()
    processManager.tearDown(3000)
    exitProcess(0)
}