package de.hasenburg.geoverdemo.transform.publisher

import de.hasenburg.geobroker.client.main.SimpleClient
import de.hasenburg.geobroker.commons.communication.ZMQProcessManager
import de.hasenburg.geobroker.commons.model.message.Payload
import de.hasenburg.geobroker.commons.model.message.ReasonCode
import de.hasenburg.geobroker.commons.model.spatial.Geofence
import de.hasenburg.geobroker.commons.model.spatial.Location
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
//    setLogLevel(logger, Level.DEBUG)
    val processManager = ZMQProcessManager()

    val clients = mutableMapOf<Location, SimpleClient>()

    // connect
    locations.forEach {
        clients[it] = SimpleClient("localhost", 5559, identity = "TransformPublisher_${System.currentTimeMillis()}_${Random.nextInt()}")
        clients[it]!!.send(Payload.CONNECTPayload(it))
        logger.debug("ConnAck: {}", clients[it]!!.receive())
        sleep(100,0)
    }

    var i = 0
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

//            val location = Geofence.world()
            val location = Geofence.circle(currLocation,2.0)
            logger.debug("Publishing at {} topic {}", location, publishTopic)

            jsonObject.put("timeSent", System.nanoTime())
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