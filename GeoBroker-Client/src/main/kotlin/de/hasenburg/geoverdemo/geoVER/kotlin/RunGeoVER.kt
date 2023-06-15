
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
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread
import kotlin.system.exitProcess

private val logger = LogManager.getLogger()
var warningArray = JSONArray()
var infoArray = JSONArray()

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

        logger.debug("{}: sending connect with client id {}", name, client.identity)

        client.send(Payload.CONNECTPayload(loc))
        logger.debug("{}: ConnAck: {}", name, client.receive())

        client.send(Payload.SUBSCRIBEPayload(topic, Geofence.circle(loc, 2.0)))
        logger.debug("{}: SubAck: {}", name, client.receive())

        logger.info("{}: Subscribed to {} at {}", name, topic, loc)
    }

    fun run() {
        logger.info("{}: Running subscriber for {} at {}", name, topic, loc)

        while (!this.cancel) {
            // receive one message
            logger.debug("{}: Waiting for message", name)
            val message = this.client.receive()
            val timeReceived = System.nanoTime()
            logger.debug("{}: Relevant Message: {}", name, message)
            if (message is Payload.PUBLISHPayload) {
                //logger.error("The content is {}", message)
                val timeSent = JSONObject(message.content).getLong("timeSent")
                logger.info("{}: Time for topic {} difference: {}", name, message.topic, timeReceived - timeSent)


                // add priority to message content, and set it to Boolean
                // if it is "info", set to be 'false', if it is warning, then true

                if (processMessage(message)) {
                    val warningUrl = URL("http://localhost:8081/warningMessage")
                    postEvents(warningUrl, displayEvents(message, warningArray))
                } else {
                    val infoUrl = URL("http://localhost:8081/infoMessage")
                    postEvents(infoUrl, displayEvents(message, infoArray))
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

    val bridgeManager = BridgeManager()
    bridgeManager.createNewRule(rule)

    val subscribers = mutableListOf<RunGeoVER>()

    logger.debug(locations)

    // prepare subscribers
    val newS = RunGeoVER(locations, rule.topic, rule.topic.topic)
    subscribers.add(newS)
    newS.prepare()

    // subscriber for the matching topic
    val newS2 = RunGeoVER(locations, matchingTopic, matchingTopic.topic)
    subscribers.add(newS2)
    newS2.prepare()

    subscribers.forEach {
        thread { it.run() }
    }

    logger.info("Press enter to finish subscribers")
    readLine()

    subscribers.forEach {
        it.stop()
    }

    bridgeManager.deleteRule(rule)
}

fun displayEvents(message: Payload.PUBLISHPayload, array: JSONArray): String {
    val locJson = JSONObject()
    locJson.put("lat", message.geofence.center.lat)
    locJson.put("lon", message.geofence.center.lon)
    val jsonToSendToTinyFaaS = JSONObject()
    jsonToSendToTinyFaaS.put("topic", message.topic.topic)
    jsonToSendToTinyFaaS.put("location", locJson)
    jsonToSendToTinyFaaS.put("message", JSONObject(message.content))

    array.put(jsonToSendToTinyFaaS)

    logger.debug("The Number of {} Event is: {}", message.topic.topic, array.length())

    return array.toString()
}

fun postEvents(url: URL, inputJsonArray: String) {
    val connection = url.openConnection() as HttpURLConnection
    connection.requestMethod = "POST"
    connection.setRequestProperty("Content-Type", "application/json")
    connection.doOutput = true

    val output = connection.outputStream
    output.write(inputJsonArray.toByteArray(Charsets.UTF_8))
    output.flush()
    output.close()

    connection.connect()
    connection.disconnect()
}

fun addPriority(message: Payload.PUBLISHPayload, priority: Boolean): String {
    val msgContent = message.content
    var contentWithPriority = JSONObject(msgContent).put("priority", priority)
    message.content = contentWithPriority.toString()
    logger.debug("Add Priority Successfully, and the current message is {}", message.content)

    return message.content
}

fun processMessage(message: Payload.PUBLISHPayload): Boolean {
    when (message.topic.topic) {
        matchingTopic.topic -> {
            addPriority(message, true)
            logger.error(JSONObject(message.content).get("priority") is Boolean)
            return true
        }

        publishTopic.topic -> {
            addPriority(message, false)
            return false
        }
    }
    return false
}
