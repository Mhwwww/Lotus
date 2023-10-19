
import de.hasenburg.geobroker.commons.model.message.Topic
import de.hasenburg.geobroker.commons.model.spatial.Geofence
import de.hasenburg.geobroker.commons.model.spatial.Location
import de.hasenburg.geover.UserSpecifiedRule
import de.hasenburg.geoverdemo.geoVER.kotlin.plugin.configureSerialization
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.serialization.Serializable
import org.apache.logging.log4j.LogManager
import java.io.File

private val logger = LogManager.getLogger()

var locations = Location(0.0,0.0)
var publishTopic = Topic("")
var matchingTopic = Topic("")

var constraints = 0.0

@Serializable
data class InputEvent(val topic: String, val repubTopic: String, val lat: String, val lon: String, val rad: String)
@Serializable
data class ErrorResponseEvent(val message: String)

@Serializable
data class InputRule(val topic: String, val operator: String, val constraints: String, val link: String)
@Serializable
data class ErrorResponseRule(val message: String)

fun main() {
    //val ruleArray= JSONArray()
    val server = embeddedServer(Netty, port = 8081) {
        applyRouting()
        configureSerialization()
        configureHTTP()
        configureMonitoring()
    }
    server.start(wait = true)
}

suspend fun geoBrokerPara(inputEvent: InputEvent) : UserSpecifiedRule {
    publishTopic = Topic(inputEvent.topic)
    matchingTopic = Topic(inputEvent.repubTopic)
    locations = Location(inputEvent.lat.toDouble(), inputEvent.lon.toDouble())
    logger.info(publishTopic)
    logger.info(locations)

    //Using the rule-based filtering function
//    return UserSpecifiedRule(Geofence.circle(locations, inputEvent.rad.toDouble()), publishTopic, File("GeoBroker-Client/src/main/kotlin/de/hasenburg/geoverdemo/multiRule/subscriber/ruleJson/"), "nodejs", matchingTopic)
    return UserSpecifiedRule(Geofence.circle(locations, inputEvent.rad.toDouble()), publishTopic, File("GeoBroker-Client/src/main/kotlin/de/hasenburg/geoverdemo/crossWind/subscriber/ruleJson/"), "nodejs", matchingTopic)
}