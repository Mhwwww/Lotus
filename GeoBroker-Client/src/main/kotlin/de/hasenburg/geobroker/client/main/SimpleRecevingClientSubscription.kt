package de.hasenburg.geobroker.client.main

import de.hasenburg.geobroker.commons.communication.ZMQProcessManager
import de.hasenburg.geobroker.commons.model.message.Payload
import de.hasenburg.geobroker.commons.model.message.ReasonCode
import de.hasenburg.geobroker.commons.model.message.Topic
import de.hasenburg.geobroker.commons.model.spatial.Geofence
import de.hasenburg.geobroker.commons.model.spatial.Location
import de.hasenburg.geobroker.commons.sleepNoLog
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
import io.ktor.util.*
import io.ktor.util.Identity.decode
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import net.pwall.json.asJSONValue
import org.apache.logging.log4j.LogManager
import org.json.JSONObject
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.system.exitProcess

import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.serialization.json.jsonObject

private val logger = LogManager.getLogger()
val client = SimpleClient("localhost", 5559)

fun main() {
    runBlocking {
        // connect, location = null
        client.send(Payload.CONNECTPayload(Location(0.0,0.0)))
        // receive one message
        logger.info("Received server answer (Hopefully Connack): {}", client.receive())

        val httpJob = launch {
            val server = embeddedServer(Netty, 9001) {
                install(ContentNegotiation) {
                    // JSON support
                    json()
                    jackson()
                }

                routing {
                    post("/sub") {
                        GlobalScope.launch {
                            zmqJob()
                            call.respondText("Zeromq job started")
                        }

                        val rawString = call.receiveText()   //{"topic": "test" }
                        val json = JSONObject(rawString) //Construct a JSONObject from a source JSON text string.

                        //subscription topic
                        val topic = Topic(json["topic"] as String)

                        //geofence: location + radius (circle)
                        var lat = json.getJSONObject("location").getDouble("lat")
                        var lon = json.getJSONObject("location").getDouble("lon")
                        var radius = json.getJSONObject("location").getDouble("radius")
                        val location = Location(lat, lon)

                        client.send(Payload.SUBSCRIBEPayload(topic, Geofence.circle(location, radius)))
                        call.respond(HttpStatusCode.OK, "Subsribed to Topic: ${topic.topic}")
                    }
                }
            }
            server.start(wait = false)
        }

        httpJob.join()
    }

}

suspend fun zmqJob() {
    val processManager = ZMQProcessManager()

    var shouldContinue = true
    while (shouldContinue){
        logger.info("Waiting for a new message to arrive")
        val message = client.receive()
        logger.info("Received server answer (This is the main while true loop): {}", message)
        //Received server answer: PUBLISHPayload(topic=Topic(topic=test), geofence=BUFFER (POINT (0 0), 2), content={"key":"value"})
        if (message !is Payload.PUBLISHPayload){
            logger.info("Ignoring Message since its not a PublishPayload")
        } else if (message.content.equals("pleaseshutdownnow")) {
            // Do nothing here
        } else{
            val fnName = message.topic.topic
            val json = message.content

            val url = URL("http://localhost:80/$fnName")
            val connection = url.openConnection() as HttpURLConnection

            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")

            val outputStream = DataOutputStream(connection.outputStream)
            withContext(Dispatchers.IO) {
                outputStream.writeBytes(json)
                outputStream.flush()
                outputStream.close()
            }
            val responseCode = connection.responseCode
            logger.info("Response Code: $responseCode")

            val inputStream = connection.inputStream
            val response = inputStream.bufferedReader().use { it.readText() }
            logger.info("Response: $response") }
    }

    // wait 5 seconds
    sleepNoLog(5000, 0)

    // disconnect
    client.send(Payload.DISCONNECTPayload(ReasonCode.NormalDisconnection))

    client.tearDownClient()
    if (processManager.tearDown(3000)) {
        logger.info("SimpleClient shut down properly.")
    } else {
        logger.fatal("ProcessManager reported that processes are still running: {}",
            processManager.incompleteZMQProcesses)
    }
    exitProcess(0)
}




