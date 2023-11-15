
import de.hasenburg.geobroker.commons.model.message.Topic
import de.hasenburg.geobroker.commons.model.spatial.Geofence
import de.hasenburg.geobroker.commons.model.spatial.Location
import de.hasenburg.geover.UserSpecifiedRule
import de.hasenburg.geoverdemo.geoVER.kotlin.FUNCTION_FILE_PATH
import de.hasenburg.geoverdemo.geoVER.kotlin.publisher.BER_AIRPORT
import de.hasenburg.geoverdemo.geoVER.kotlin.publisher.FRANKFURT_AIRPORT
import de.hasenburg.geoverdemo.geoVER.kotlin.publisher.SCHOENHAGEN_AIRPORT
import de.hasenburg.geoverdemo.geoVER.kotlin.publisher.WEATHER_STATION
import kotlinx.serialization.Serializable
import org.apache.logging.log4j.LogManager
import java.io.File

private val logger = LogManager.getLogger()

var locations = Location(0.0,0.0)
var publishTopic = Topic("")
var matchingTopic = Topic("")
var radius = 0.0

var constraints = 0.0

@Serializable
//data class InputEvent(val topic: String, val repubTopic: String, val lat: String, val lon: String, val rad: String)
data class InputEvent(val topic: String, val repubTopic: String, val locationName: String, val rad: String)


@Serializable
data class ErrorResponseEvent(val message: String)

@Serializable
data class InputRule(val topic: String, val operator: String, val constraints: String, val link: String)


@Serializable
data class ErrorResponseRule(val message: String)

fun geoBrokerPara(inputEvent: InputEvent) : UserSpecifiedRule {
        var lat = 0.0
        var lon = 0.0

        publishTopic = Topic(inputEvent.topic)
        matchingTopic = Topic(inputEvent.repubTopic)

        when(inputEvent.locationName){
                "Berlin Airport" -> {
                        lat = BER_AIRPORT.center.lat
                        lon = BER_AIRPORT.center.lon
                }
                "Schönhagen Airport" -> {
                        lat = SCHOENHAGEN_AIRPORT.center.lat
                        lon = SCHOENHAGEN_AIRPORT.center.lon
                }
                "Frankfurt Airport" -> {
                        lat = FRANKFURT_AIRPORT.center.lat
                        lon = FRANKFURT_AIRPORT.center.lon
                }
                "Weather Station" -> {
                        lat = WEATHER_STATION.center.lat
                        lon = WEATHER_STATION.center.lon
                }
        }

        locations = Location(lat,lon)

        radius = inputEvent.rad.toDouble()

        logger.info(publishTopic)
        logger.info(matchingTopic)
        logger.info(locations)
        logger.info(radius)

        logger.info("location name: {}", inputEvent.locationName)


    //Using the rule-based filtering function
//    return UserSpecifiedRule(Geofence.circle(locations, inputEvent.rad.toDouble()), publishTopic, File("GeoBroker-Client/src/main/kotlin/de/hasenburg/geoverdemo/multiRule/subscriber/ruleJson/"), "nodejs", matchingTopic)
    return UserSpecifiedRule(Geofence.circle(locations, radius), publishTopic, File(FUNCTION_FILE_PATH), "nodejs", matchingTopic)
}
