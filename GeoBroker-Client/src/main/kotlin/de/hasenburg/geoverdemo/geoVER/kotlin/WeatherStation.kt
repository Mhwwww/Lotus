
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
import de.hasenburg.geoverdemo.geoVER.kotlin.publisher.BER_AIRPORT
import de.hasenburg.geoverdemo.geoVER.kotlin.publisher.FRANKFURT_AIRPORT
import de.hasenburg.geoverdemo.geoVER.kotlin.publisher.SCHOENHAGEN_AIRPORT
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.json.JSONArray
import org.json.JSONObject
import kotlin.concurrent.thread
import kotlin.system.exitProcess

private val logger = LogManager.getLogger()

var weatherWarningArray = JSONArray()
var weatherInfoArray = JSONArray()

const val WEATHER_STATION_HOST = "localhost"
const val WEATHER_STATION_PORT = 5559

const val WEATHER_INFO_TOPIC = "info"
const val WEATHER_WARNING_TOPIC = "weather"

const val WEATHER_INFO_BUCKET = "allInfo"
const val WEATHER_WARNING_BUCKET = "allWarning"

class WeatherStation(private val loc: Location, private val topic: Topic, private val name: String) {
    private val logger = LogManager.getLogger()

    private var cancel = false
    private lateinit var client: SimpleClient
    private lateinit var processManager: ZMQProcessManager

    fun prepare() {
        setLogLevel(this.logger, Level.DEBUG)
        logger.debug("{}: Subscribing to {} at {}", name, topic, loc)

        this.processManager = ZMQProcessManager()
        this.client = SimpleClient(WEATHER_STATION_HOST, WEATHER_STATION_PORT, identity = "Weather_Station_Sub_${name}_${System.currentTimeMillis()}")

        logger.debug("{}: sending connect with client id {}", name, client.identity)

        client.send(Payload.CONNECTPayload(loc))
        logger.debug("{}: ConnAck: {}", name, client.receive())

//        client.send(Payload.SUBSCRIBEPayload(topic, Geofence.circle(loc, 2.0)))
        client.send(Payload.SUBSCRIBEPayload(topic, Geofence.circle(loc, radius)))

        logger.debug("{}: SubAck: {}", name, client.receive())
        logger.info("{}: Subscribed to {} at {} with raduis {}", name, topic, loc, radius)
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
                // add priority to message content, and set it to Boolean
                // if it is "info", set to be 'false', if it is warning, then true
                if (processMessage1(message)) { // warning messages

                    reformatEvents1(message, weatherWarningArray)
                    //store warnings in Bucket_warning
                    influxdb.writeMsgToInfluxDB(message, WEATHER_WARNING_BUCKET)
//                    influxdb.writeToInfluxDB(reformatMsg, WARNING_BUCKET)

                } else {
                    val info = reformatEvents1(message, weatherInfoArray)

                    //store info in Bucket_info
                    influxdb.writeMsgToInfluxDB(message, WEATHER_INFO_BUCKET)
//                    influxdb.writeToInfluxDB(info, INFO_BUCKET)
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

fun reformatEvents1(message: Payload.PUBLISHPayload, array: JSONArray): String {
    val locJson = JSONObject()
    val sentJson = JSONObject()
    val msgLocation = message.geofence
    var locationName = ""

    // if the location is the known location, then send the 'name', else send the concreate location.
    if (msgLocation == SCHOENHAGEN_AIRPORT){
        locationName = "Schönhagen Airport"
        sentJson.put("location", locationName)

    } else if (msgLocation == BER_AIRPORT){
        locationName = "Berlin Airport"
        sentJson.put("location", locationName)

    } else if (msgLocation == FRANKFURT_AIRPORT){
        locationName = "Frankfurt Airport"
        sentJson.put("location", locationName)
    }  else{
        locJson.put("lat", msgLocation.center.lat)
        locJson.put("lon", msgLocation.center.lon)
    }

    sentJson.put("topic", message.topic.topic)
//    sentJson.put("location", locJson)
    sentJson.put("message", JSONObject(message.content))

    array.put(sentJson)
    logger.debug("The Number of {} Event is: {}", message.topic.topic, array.length())

    return array.toString()

//    return sentJson.toString()
}

fun addPriority1(message: Payload.PUBLISHPayload, priority: Boolean): String {
    val msgContent = message.content
    val contentWithPriority = JSONObject(msgContent).put("Priority", priority)
    message.content = contentWithPriority.toString()
    logger.debug("Add Priority Successfully, and the current message is {}", message.content)
    return message.content
}

fun processMessage1(message: Payload.PUBLISHPayload): Boolean {
    if (message.topic.topic==WEATHER_WARNING_TOPIC){
        addPriority1(message, true)
        logger.error(JSONObject(message.content).get("Priority") is Boolean)
        return true
    } else if (message.topic.topic == WEATHER_INFO_TOPIC){
        addPriority1(message, false)
        return false
    }

    logger.error("Fail to add priority! Do not find the matching topic for either info or warning")
    return false
}

fun runRuleSubscriber1(rule: UserSpecifiedRule) = runBlocking {
    setLogLevel(logger, Level.DEBUG)

    val bridgeManager = BridgeManager()
    bridgeManager.createNewRule(rule)

    val subscribers = mutableListOf<WeatherStation>()
    val location = Location(rule.geofences[0].center.lat, rule.geofences[0].center.lon)

    // prepare subscribers
    val newS = WeatherStation(location, rule.topic, rule.topic.topic)
    subscribers.add(newS)

    newS.prepare()

    // subscriber for the matching topic
    val newS2 = WeatherStation(location, rule.matchingTopic, rule.matchingTopic.topic)
    subscribers.add(newS2)
    newS2.prepare()

    subscribers.forEach {
        thread {
            runBlocking {
                it.run()
            }
        }
    }

    logger.info("Press enter to finish subscribers")
    readLine()

    subscribers.forEach {
        it.stop()
    }

    bridgeManager.deleteRule(rule)
}


fun main () = runBlocking {
    val rule = geoBrokerPara(InputEvent(topic= "info", repubTopic = "weather", lat = "0", lon = "0", rad = "80"))

    runRuleSubscriber1(rule)

}