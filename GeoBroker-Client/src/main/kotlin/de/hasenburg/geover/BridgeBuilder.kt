package de.hasenburg.geobroker.client.main

import de.hasenburg.geobroker.commons.model.message.Payload
import de.hasenburg.geobroker.commons.model.message.Topic
import de.hasenburg.geobroker.commons.model.spatial.Geofence
import de.hasenburg.geobroker.commons.model.spatial.Location
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.logging.log4j.LogManager
import org.json.JSONObject
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL


const val GEOBROKER_HOST = "localhost"
const val GEOBROKER_PORT = 5559
const val TINYFAAS_BASE_URL = "http://localhost:80/"

private val logger = LogManager.getLogger()
public suspend fun buildBridgeBetweenTopicAndFunction(topic: Topic, geofence: Geofence, functionName: String, newTopicIfMatch: Topic) {
    //identity: TinyFaaSClient_test_/*/drone/*_(0.0,0.0,2.0)_nanotime
    //val client = SimpleClient(GEOBROKER_HOST, GEOBROKER_PORT, socketHWM = 1000, identity = " TinyFaaSClient_"+ functionName+"_" +topic.topic+"_"+geofence+"_"+System.nanoTime())
    var eventNum = 0
    val newTopicSet = ArrayList<String>()


    val client = SimpleClient(
        GEOBROKER_HOST,
        GEOBROKER_PORT,
        socketHWM = 1000,
        identity = "TinyFaaSClient_" + functionName + "_" + System.nanoTime()
    )
    client.send(Payload.CONNECTPayload(Location(0.0, 0.0)))
    logger.debug("Connect Payload Answer: {}", client.receive())


    client.send(Payload.SUBSCRIBEPayload(topic, geofence))
    logger.debug("Subscribe Payload Answer: {}", client.receive())

    // Every time client.recieve gets something it should be a PublishPayload()
    // Send out a request to the function everytime this happens
    while (true) {
        logger.debug(
            "ZMQ Client Topic={} Geofence={} is waiting for a message to send to FunctionName={}",
            topic,
            geofence,
            functionName
        )
        val message = client.receive()
        Runtime.getRuntime().addShutdownHook(Thread {
            client.tearDownClient()
        })
        if (message !is Payload.PUBLISHPayload) {
            logger.debug("Message {} is not a PublishPayload, but it should be! Its probably a PubAckPayload from pusblishing something to a mathing topic", message)
            continue
        }
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
        logger.debug(
            "ZMQ Client{} received message and will forward json {} to function {}",
            client.identity,
            jsonToSendToTinyFaaS.toString(),
            functionName
        )
        val resultFromTinyFaaS = sendReqToTinyFaaS(functionName, jsonToSendToTinyFaaS.toString())
        val tinyFaaSJson = JSONObject(resultFromTinyFaaS)
        logger.debug("Processed Event. Asnwer from TinyFaaS is={}", tinyFaaSJson)

        //TODO: set new event topics
        //TODO: client re-subscribe to the new topic
        //TODO: re-publish the event with new topics


        if (tinyFaaSJson.keySet().size > 0) {
            // response messages from tinyfaas are the processed event topics
            // new topic: original + client id (including funcitonName--original topic)
            logger.debug("We have found a match! Publishing answer from tinyFaaS to {}", newTopicIfMatch)
            republish(client, newTopicIfMatch, message.geofence, tinyFaaSJson.toString())
            eventNum += 1

            logger.info("\nNumber of processed events: $eventNum")

        } else {
            logger.debug("TinyFaaS returned an empty object, so we don't need to forward it to any other topic")
        }


    }
}

suspend fun resubscribe(client: SimpleClient, topic: Topic, geofence: Geofence) {
    client.send(Payload.CONNECTPayload(Location(geofence.center.lat, geofence.center.lon)))
    logger.info("Connect Payload answer: {}", client.receive())

    client.send(Payload.SUBSCRIBEPayload(topic, geofence))
    logger.info("The Direct Subscribe Payload Answer: {}", client.receive())

}


suspend fun republish(client: SimpleClient, topic: Topic, geofence: Geofence, message: String) {
    client.send(Payload.PUBLISHPayload(topic, geofence, message))
}

suspend fun sendReqToTinyFaaS(
    functionName: String,
    payload: String,
    async: Boolean = true,
    contentType: String = "application/json"
): String {
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


/*
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

                    val clientDirectory = ClientDirectory()
                    clientDirectory.addClient(client.identity, location, false)
                    clientDirectory.updateSubscription(client.identity, topic, Geofence.circle(location, radius))

                    logger.error(
                        "the client has {} subscription",
                        clientDirectory.getCurrentClientSubscriptions(client.identity)
                    )
                    logger.error("the subscription is {}", clientDirectory.getSubscription(client.identity, topic))

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
}*/
