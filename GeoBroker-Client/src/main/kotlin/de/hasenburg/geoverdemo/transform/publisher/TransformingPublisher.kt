package de.hasenburg.geoverdemo.transform.publisher

import de.hasenburg.geobroker.client.main.SimpleClient
import de.hasenburg.geobroker.commons.communication.ZMQProcessManager
import de.hasenburg.geobroker.commons.model.message.Payload
import de.hasenburg.geobroker.commons.model.message.ReasonCode
import de.hasenburg.geobroker.commons.model.spatial.Geofence
import de.hasenburg.geobroker.commons.randomDouble
import de.hasenburg.geobroker.commons.setLogLevel
import de.hasenburg.geobroker.commons.sleep
import de.hasenburg.geoverdemo.transform.common.*
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.json.JSONArray
import kotlin.system.exitProcess
import org.json.JSONObject
import kotlin.random.Random

private val logger = LogManager.getLogger()
fun main() {
    setLogLevel(logger, Level.DEBUG)
    val processManager = ZMQProcessManager()
    val client = SimpleClient("localhost", 5559, identity = "TransformPublisher_${System.currentTimeMillis()}_${Random.nextInt()}")

    // connect
    locations.forEach {
        client.send(Payload.CONNECTPayload(it))
        logger.debug("ConnAck: {}", client.receive())
        sleep(100,0)
    }

    repeat(numberOfRepeats) {
        locations.forEach{ currLocation ->
            val jsonObject = JSONObject()
            jsonObject.put("list", JSONArray())
            val jsonList = jsonObject.getJSONArray("list")
            repeat(10) {
                val newElem = JSONObject().apply {
                    put("temperature", 35)
                    put("speed", 15)
                    put("wind", randomDouble(0.0, 60.0))
                    put("lat", currLocation.lat)
                    put("lon", currLocation.lon)
                }
                jsonList.put(newElem)
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