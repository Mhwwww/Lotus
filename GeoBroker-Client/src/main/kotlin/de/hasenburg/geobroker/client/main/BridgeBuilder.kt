package de.hasenburg.geobroker.client.main

import de.hasenburg.geobroker.commons.communication.ZMQProcessManager
import de.hasenburg.geobroker.commons.model.message.Payload
import de.hasenburg.geobroker.commons.model.message.Topic
import de.hasenburg.geobroker.commons.model.spatial.Geofence
import de.hasenburg.geobroker.commons.model.spatial.Location
import de.hasenburg.geobroker.commons.setLogLevel
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.*
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.json.JSONObject
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

const val GEOBROKER_HOST = "localhost"
const val GEOBROKER_PORT = 5559
const val TINYFAAS_BASE_URL = "http://localhost:80/"

private val logger = LogManager.getLogger()

suspend fun main() {
    setLogLevel(logger, Level.DEBUG)
    val processManager = ZMQProcessManager()
    Runtime.getRuntime().addShutdownHook(Thread {
        processManager.tearDown(3000)
    })

    runBlocking {
        startHttpServer() // This blocks forever
    }

}

suspend fun buildBridgeBetweenTopicAndFunction(topic: Topic, geofence: Geofence, functionName: String) {
    val client = SimpleClient(GEOBROKER_HOST, GEOBROKER_PORT)
    client.send(Payload.CONNECTPayload(Location(0.0,0.0)))
    logger.debug("Connect Payload Answer: {}", client.receive())
    client.send(Payload.SUBSCRIBEPayload(topic, geofence))
    logger.debug("Subscribe Payload Answer: {}", client.receive())

    // Every time client.recieve gets something it should be a PublishPayload()
    // Send out a request to the function everytime this happens

    Runtime.getRuntime().addShutdownHook(Thread {
        client.tearDownClient()
    })

    while (true) {
        logger.debug("ZMQ Client Topic={} Geofence={} is waiting for a message to send to FunctionName={}", topic, geofence, functionName)
        val message = client.receive()
        if (message !is Payload.PUBLISHPayload){
            logger.error("Message {} is not a PublishPayload, but it should be!", message)
        } else{
            val locJson = JSONObject()
            locJson.put("lat", message.geofence.center.lat)
            locJson.put("lon", message.geofence.center.lon)
            val jsonToSendToTinyFaaS = JSONObject()
            jsonToSendToTinyFaaS.put("topic", message.topic.topic)
            jsonToSendToTinyFaaS.put("location", locJson)
            jsonToSendToTinyFaaS.put("message", JSONObject(message.content))

            /**
             * message: "Whatever from Clients, we don't care, probably JSON"
             * topic: drones
             * location: {lat: 123, lon: 456}
             */
            logger.debug("ZMQ Client received message and will forward json {} to function {}", jsonToSendToTinyFaaS.toString(), functionName)
            sendReqToTinyFaaS(functionName, jsonToSendToTinyFaaS.toString())
        }

    }

}

suspend fun sendReqToTinyFaaS(functionName: String, payload: String, async: Boolean = true, contentType: String = "application/json"): String {
    val url = URL("$TINYFAAS_BASE_URL$functionName")
    val connection = url.openConnection() as HttpURLConnection

    connection.requestMethod = "POST"
    connection.doOutput = true
    connection.setRequestProperty("Content-Type", contentType)

    val outputStream = DataOutputStream(connection.outputStream)
    withContext(Dispatchers.IO) {
        outputStream.writeBytes(payload)
        outputStream.flush()
        outputStream.close()
    }
    val responseCode = connection.responseCode

    val inputStream = connection.inputStream
    val response = inputStream.bufferedReader().use { it.readText() }

    if (responseCode != 200) {
        logger.error("Response code is not 200! It's {}. Response: {}", responseCode, response)
    }

    return response
}

suspend fun startHttpServer() {
    runBlocking {
        val server = embeddedServer(Netty, 9001) {
            install(ContentNegotiation) {
                // JSON support
                json()
                jackson()
            }

            routing {
                post("/sub") {
                    val rawString = call.receiveText()   //{"topic": "test" }
                    val json = JSONObject(rawString) //Construct a JSONObject from a source JSON text string.

                    //subscription topic
                    val topic = Topic(json["topic"] as String)

                    //function to call
                    val functionName = json["function"] as String

                    //geofence: location + radius (circle)
                    val lat = json.getJSONObject("location").getDouble("lat")
                    val lon = json.getJSONObject("location").getDouble("lon")
                    val radius = json.getJSONObject("location").getDouble("radius")
                    val location = Location(lat, lon)

                    logger.debug("Received message to connect {} and {}", topic, functionName)

                    thread {
                        logger.debug("Connecting Topic {} and functionName {}", topic, functionName)
                        runBlocking {
                            buildBridgeBetweenTopicAndFunction(topic, Geofence.circle(location, radius), functionName)
                        }
                    }

                    call.respond(HttpStatusCode.OK, "Subsribed to Topic: ${topic.topic}")
                }
            }
        }
        server.start(wait = false)
    }
}