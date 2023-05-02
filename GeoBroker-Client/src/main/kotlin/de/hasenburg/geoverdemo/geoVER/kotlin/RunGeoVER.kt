package de.hasenburg.geoverdemo.geoVER.kotlin

import de.hasenburg.geobroker.client.main.SimpleClient
import de.hasenburg.geobroker.commons.communication.ZMQProcessManager
import de.hasenburg.geobroker.commons.model.message.Payload
import de.hasenburg.geobroker.commons.model.message.ReasonCode
import de.hasenburg.geobroker.commons.model.message.Topic
import de.hasenburg.geobroker.commons.model.spatial.Geofence
import de.hasenburg.geobroker.commons.model.spatial.Location
import de.hasenburg.geobroker.commons.setLogLevel
import de.hasenburg.geover.BridgeManager
import de.hasenburg.geover.UserSpecifiedRule
import kotlinx.coroutines.runBlocking
import locations
import matchingTopic
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread
import kotlin.system.exitProcess
/*

private val logger = LogManager.getLogger()
class RunGeoVER(private val loc: Location, private val topic: Topic, private val name: String) {
    private val logger = LogManager.getLogger()
    private var cancel = false
    private lateinit var client: SimpleClient
    private lateinit var processManager: ZMQProcessManager
    fun prepare() {
        setLogLevel(this.logger, Level.DEBUG)

        logger.debug("{}: Subscribing to {} at {}", name, topic, loc)

        this.processManager = ZMQProcessManager()
        this.client = SimpleClient("localhost", 5559, identity = " RGSub_${name}")

        logger.debug("{}: sending connect with client id {}", name, client.identity)

        client.send(Payload.CONNECTPayload(loc))
        logger.debug("{}: ConnAck: {}", name, client.receive())

        client.send(Payload.SUBSCRIBEPayload(topic, Geofence.circle(loc, 2.0)))
        logger.debug("{}: SubAck: {}", name, client.receive())

        logger.info("{}: Subscribed to {} at {}", name, topic, loc)
    }

    fun run() {
        logger.info("{}: Running subscriber for {} at {}", name, topic, loc)
        // some code smells here:
        // receive is blocking, so if we set cancel to true, it won't check until it has received a message in the meantime
        // presumably this still works because the DISCONNECT message will incur an ACK
        while (!this.cancel) {
            // receive one message
            logger.debug("{}: Waiting for message", name)
            val message = this.client.receive()
            val timeReceived = System.nanoTime()
            logger.debug("{}: Relevant Message: {}", name, message)
            if (message is Payload.PUBLISHPayload) {
                val timeSent = JSONObject(message.content).getLong("timeSent")
                logger.info("{}: Time for topic {} difference: {}", name, message.topic, timeReceived - timeSent)

                if (message.topic.topic == "warnings"){
                    logger.error("The content is{}", message)
                }

            }
        }
    }
    fun display():String {
        logger.info("{}: Running subscriber for {} at {}", name, topic, loc)
        // some code smells here:
        // receive is blocking, so if we set cancel to true, it won't check until it has received a message in the meantime
        // presumably this still works because the DISCONNECT message will incur an ACK
        while (!this.cancel) {
            // receive one message
            logger.debug("{}: Waiting for message", name)
            val message = this.client.receive()
            val timeReceived = System.nanoTime()
            logger.debug("{}: Relevant Message: {}", name, message)
            if (message is Payload.PUBLISHPayload) {
                //TODO: could get timeSent but the following log don't display
                val timeSent = JSONObject(message.content).getLong("timeSent")
                logger.info("{}: Time for topic {} difference: {}", name, message.topic, timeReceived - timeSent)

                return message.toString()
            }
        }
        return ""
    }

    fun stop() {
        this.cancel = true
        // disconnect
        this.client.send(Payload.DISCONNECTPayload(ReasonCode.NormalDisconnection))
        this.client.tearDownClient()
        this.processManager.tearDown(3000)
        exitProcess(0)
    }
}

fun runRuleSubscriber(rule: UserSpecifiedRule) = runBlocking {
    setLogLevel(logger, Level.INFO)

    val bridgeManager = BridgeManager()
    bridgeManager.createNewRule(rule)

// TODO: Start tinyFaas
// TODO: Start Server.kt

    // prepare subscribers
    val newS = RunGeoVER(locations, publishTopic, rule.topic.topic)
    logger.error(rule.topic.topic)
    newS.prepare()
    val newS2 = RunGeoVER(locations, matchingTopic, rule.matchingTopic.topic)
    logger.error(rule.matchingTopic.topic)
    newS2.prepare()
    thread {
        newS.run()
        newS2.run()

    }

    logger.info("Press enter to finish subscriers")
    readLine()

    newS.stop()

    bridgeManager.deleteRule(rule)
*/

private val logger = LogManager.getLogger()
val warningArray= JSONArray()

class RunGeoVER(private val loc: Location, private val topic: Topic, private val name: String) {
    private val logger = LogManager.getLogger()
    private var cancel = false
    private lateinit var client: SimpleClient
    private lateinit var processManager: ZMQProcessManager
    fun prepare() {
        setLogLevel(this.logger, Level.DEBUG)

        logger.debug("{}: Subscribing to {} at {}", name, topic, loc)

        this.processManager = ZMQProcessManager()
        this.client = SimpleClient("localhost", 5559, identity = " RuleJsonSub_${name}")

        logger.debug("{}: sending connect with client id {}", name,  client.identity)

        client.send(Payload.CONNECTPayload(loc))
        logger.debug("{}: ConnAck: {}", name, client.receive())

        client.send(Payload.SUBSCRIBEPayload(topic, Geofence.circle(loc, 2.0)))
        logger.debug("{}: SubAck: {}", name, client.receive())

        logger.info("{}: Subscribed to {} at {}", name, topic, loc)
    }
    fun run() {
        logger.info("{}: Running subscriber for {} at {}", name, topic, loc)
        // some code smells here:
        // receive is blocking, so if we set cancel to true, it won't check until it has received a message in the meantime
        // presumably this still works because the DISCONNECT message will incur an ACK
        while (!this.cancel) {
            // receive one message
            logger.debug("{}: Waiting for message", name)
            val message = this.client.receive()
            val timeReceived = System.nanoTime()
            logger.debug("{}: Relevant Message: {}", name, message)
            if (message is Payload.PUBLISHPayload) {
                val timeSent = JSONObject(message.content).getLong("timeSent")
                logger.info("{}: Time for topic {} difference: {}", name, message.topic, timeReceived - timeSent)

                if (message.topic.topic == "warnings"){
                    logger.error("The content is {}", message)
                    postWarnings(display(message))
                }
            }
        }
    }

    fun stop() {
        this.cancel = true
        // disconnect
        this.client.send(Payload.DISCONNECTPayload(ReasonCode.NormalDisconnection))
        this.client.tearDownClient()
        this.processManager.tearDown(3000)
        exitProcess(0)
    }
}

fun runRuleSubscriber(rule: UserSpecifiedRule) = runBlocking {
    setLogLevel(logger, Level.DEBUG)
    // Geofence.circle(Location(0.0,0.0), 350.0)
    val newRule = rule

    val bridgeManager = BridgeManager()
    bridgeManager.createNewRule(newRule)

    val subscribers = mutableListOf<RunGeoVER>()

    logger.error(locations)


    // prepare subscribers
        val newS = RunGeoVER(locations, rule.topic, rule.topic.topic)
        subscribers.add(newS)
        newS.prepare()

        // also prepare a subscriber for the matching topic
        val newS2 = RunGeoVER(locations, matchingTopic, matchingTopic.topic)
        subscribers.add(newS2)
        newS2.prepare()



    subscribers.forEach {
        thread { it.run() }
    }

    logger.info("Press enter to finish subscribers")
    readLine()

    subscribers.forEach{
        it.stop()
    }


    bridgeManager.deleteRule(newRule)

}

fun display(message: Payload.PUBLISHPayload):String{
    val locJson = JSONObject()
    locJson.put("lat", message.geofence.center.lat)
    locJson.put("lon", message.geofence.center.lon)
    val jsonToSendToTinyFaaS = JSONObject()
    jsonToSendToTinyFaaS.put("topic", message.topic.topic)
    jsonToSendToTinyFaaS.put("location", locJson)
    jsonToSendToTinyFaaS.put("message", JSONObject(message.content))

    warningArray.put(jsonToSendToTinyFaaS)
    //logger.error(warningArray)
    logger.error(warningArray.length())

    return warningArray.toString()
}


fun postWarnings(warningArrayJson: String) {
    val url = URL("http://localhost:8081/warningMessage")
    val connection = url.openConnection() as HttpURLConnection
    connection.requestMethod = "POST"
    connection.setRequestProperty("Content-Type", "application/json")
    connection.doOutput = true
    val output = connection.outputStream
    output.write(warningArrayJson.toByteArray(Charsets.UTF_8))
    output.flush()
    output.close()
    connection.connect()
    connection.disconnect()
}

