

import de.hasenburg.geobroker.commons.model.message.Topic
import de.hasenburg.geobroker.commons.model.spatial.Geofence
import de.hasenburg.geobroker.commons.model.spatial.Location
import de.hasenburg.geover.UserSpecifiedRule
import de.hasenburg.geoverdemo.geoVER.kotlin.runRuleSubscriber
import de.hasenburg.geoverdemo.geoVER.kotlin.warningArray
import io.ktor.http.*
import io.ktor.serialization.gson.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.logging.log4j.LogManager
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

private val logger = LogManager.getLogger()

var locations = Location(0.0,0.0)
var publishTopic = Topic("")
val matchingTopic = Topic("warnings")

var constraints = 0.0

@Serializable
data class InputEvent(val topic: String, val geofence: String)
@Serializable
data class ErrorResponseEvent(val message: String)
@Serializable
data class OutputEvent(val output: String)

@Serializable
data class InputRule(val topic: String, val operator: String, val constraints: String)
@Serializable
data class ErrorResponseRule(val message: String)
@Serializable
data class OutputRule(val output: String)

@Serializable
data class Rule(val topic: String, val operator: String, val constraints: String)

fun main() {
    val ruleArray= JSONArray()
    val server = embeddedServer(Netty, port = 8081) {

        install(ContentNegotiation) {
            json()
            gson()
        }
        install(CallLogging){
        }
        install(CORS) {
            allowMethod(HttpMethod.Options)
            allowMethod(HttpMethod.Post)
            allowMethod(HttpMethod.Get)
            allowHeader(HttpHeaders.AccessControlAllowOrigin)
            allowHeader(HttpHeaders.ContentType)

            anyHost()
        }
        //TODO
        //install(WebSockets)
        routing {
            //static page for localhost:8081/index.html
            static("/") {
                resources("web")
            }
            // apply subscription
            options("/test") {
                call.respond(HttpStatusCode.OK)
            }
            post("/test") {
                try {
                    val inputEvent = call.receive<InputEvent>()
                    println("Received inputEvent: $inputEvent")
                    val output = processInput(inputEvent)

                    call.respond(OutputEvent(output))

                } catch (e: Exception) {
                    val errorMessage = e.message ?: "Unknown error"
                    call.respond(status = HttpStatusCode.BadRequest, ErrorResponseEvent(errorMessage))
                }
            }
            // show warnings
            options("/show") {
                call.respond(HttpStatusCode.OK)
            }
            post("/show") {
                try {
                    val inputEvent = call.receive<InputEvent>()
                    println("Received inputEvent: $inputEvent")
                    val output = processInput(inputEvent)

                    call.respond(OutputEvent(output))

                    GlobalScope.launch {
                        runRuleSubscriber(geoBrokerPara(inputEvent))
                    }

                } catch (e: Exception) {
                    val errorMessage = e.message ?: "Unknown error"
                    call.respond(status = HttpStatusCode.BadRequest, ErrorResponseEvent(errorMessage))
                }
            }

            //add rule
            options("/addRule") {
                call.respond(HttpStatusCode.OK)
            }
            post("/addRule") {
                try {
                    val inputRule = call.receive<InputRule>()
                    val rule = Rule(inputRule.topic, inputRule.operator, inputRule.constraints)
                    val json = Json.encodeToString(rule)
//                    logger.info(json)
                   val ruleJson = JSONObject(json)
//                    logger.info(ruleJson)
                    ruleArray.put(ruleJson)

                    val output = processInputRule(inputRule)
                    call.respond(OutputRule(output))

                } catch (e: Exception) {
                    val errorMessage = e.message ?: "Unknown error"
                    call.respond(status = HttpStatusCode.BadRequest, ErrorResponseRule(errorMessage))
                }
            }
            // save the added rules to 'saverule.json' in 'multiRule' folder
            options("/saveRules") {
                call.respond(HttpStatusCode.OK)
            }
            post("/saveRules") {
                try {
                    val file = File("./GeoBroker-Client/src/main/kotlin/de/hasenburg/geoverdemo/multiRule/subscriber/ruleJson/saverule.json")
                    // clear file
                    file.writeText("")
                    file.appendText("$ruleArray\n")
                    //call.respond(HttpStatusCode.OK)
                   // logger.error(ruleArray.toString())

                    call.respond(ruleArray.toString())
                    // clear array
                    while (ruleArray.length() > 0) {
                        ruleArray.remove(0)
                    }

                } catch (e: Exception) {
                    val errorMessage = e.message ?: "Unknown error"
                    call.respond(status = HttpStatusCode.BadRequest, ErrorResponseRule(errorMessage))
                }

            }
            //get the events from 'warnings' topic
            get("/warningMessage"){
                call.respond(warningArray.toString())
            }

        }
    }
    server.start(wait = true)
}

fun processInput(inputEvent: InputEvent): String {
    return try {
        "Received Subscription is: Topic: ${inputEvent.topic}, Geofence: ${inputEvent.geofence}"
    } catch (e: Exception) {
        "Error occurred on server"
    }
}

fun processInputRule(inputRule: InputRule): String {
    return try {
        "Received Rule is: Topic: ${inputRule.topic}, Operator: ${inputRule.operator}, Constraints: ${inputRule.constraints}"
    } catch (e: Exception) {
        "Error occurred on server"
    }
}

fun geoBrokerPara(inputEvent: InputEvent) : UserSpecifiedRule {
    publishTopic = Topic(inputEvent.topic)
    locations = Location(inputEvent.geofence.toDouble(), inputEvent.geofence.toDouble())
    logger.info(publishTopic)
    logger.info(locations)
    //Using the rule-based filtering function
    return UserSpecifiedRule(Geofence.circle(locations,2.0), publishTopic, File("GeoBroker-Client/src/main/kotlin/de/hasenburg/geoverdemo/multiRule/subscriber/ruleJson/"), "nodejs", matchingTopic)
}
