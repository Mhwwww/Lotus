

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.json.JSONObject
import java.io.File

var RULE_JSON = ""
var INPUT_FUNCTION_PATH = ""

const val CROSSWIND_PATH = "/Users/minghe/geobroker/GeoBroker-Client/src/main/kotlin/de/hasenburg/geoverdemo/crossWind/subscriber/ruleJson/"
const val TEMPERATURE_PATH = "/Users/minghe/geobroker/GeoBroker-Client/src/main/kotlin/de/hasenburg/geoverdemo/multiRule/subscriber/ruleJson/"

fun Application.applyRouting(){
    routing {
        //static page for localhost:8081/index.html
        static("/") {
            resources("web")
        }

        post("/subscriptionInput") {
            try {
                val inputEvent = call.receive<InputEvent>()
                println("Received inputEvent: $inputEvent")
                call.respond("post show successfully")

                //todo: write rules into file
                when(inputEvent.functionName){
                    "Crosswind"->{
                        INPUT_FUNCTION_PATH = CROSSWIND_PATH
                    }
                    "Snow Clearing"->{
                        INPUT_FUNCTION_PATH = TEMPERATURE_PATH
                    }
                }
                val file = File(INPUT_FUNCTION_PATH+"/saverule.json/")

                // clear file
                file.writeText("")
                file.writeText("$RULE_JSON\n")

                GlobalScope.launch {
                    runRuleSubscriber(geoBrokerPara(inputEvent))
                }
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Unknown error"
                call.respond(status = HttpStatusCode.BadRequest, ErrorResponseEvent(errorMessage))
            }
        }

        post("/saveRules") {
            try {
                //receive the frontend input rule set
                val rules: List<InputRule> = call.receive()
                val json = Json.encodeToString(rules)
                RULE_JSON = json

                // the file to save use input rules
//                val file = File("./GeoBroker-Client/src/main/kotlin/de/hasenburg/geoverdemo/multiRule/subscriber/ruleJson/saverule.json")
                //var SAVE_RULES_JSON_PATH = FUNCTION_FILE_PATH+"/saverule.json/"


//                val file = File(INPUT_FUNCTION_PATH+"/saverule.json/")
//                println(json)
//
//                // clear file
//                file.writeText("")
//                file.writeText("$json\n")

            } catch (e: Exception) {
                val errorMessage = e.message ?: "Unknown error"
                call.respond(status = HttpStatusCode.InternalServerError, ErrorResponseRule(errorMessage))
            }
        }

        // messages in 'info' topic
        get("/infoMessage"){
            call.respond(infoArray.toString())

        }

        //get the events from 'warnings' topic
        get("/warningMessage"){
            call.respond(warningArray.toString())
        }

        // change priority of the warning messages
        post("/warningMessage/{timeSent}") {
            val timeSent = call.parameters["timeSent"]?.toLongOrNull()
            if (timeSent == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid timeSent value")
            }

            val arrayLength = warningArray.length()
            for (i in 0 until arrayLength) {
                val obj = warningArray[i] as JSONObject

                if (obj.has("message")) {
                    val message = obj.getJSONObject("message")
                    val oriTimeSent = message.optLong("Time Sent")

                    if (oriTimeSent == timeSent) {
                        //add changed info message to 'warningArray'
                        infoArray.put(warningArray[i])
                        //println(infoPassedRuleArray.length())

                        //delete this message from 'warningArray'
                        warningArray.remove(i)
                        break
                    }
                }
            }
            // number of remaining warnings
            val remainingCount = warningArray.length()
            println("warning Length ${remainingCount}")

            val responseMessage = "Remaining: $remainingCount warnings"

            call.respond(responseMessage)
        }


    }
}

/*        // show warnings
        options("/showInfo") {
            call.respond(HttpStatusCode.OK)
        }
        post("/showInfo") {
            call.respond("testing")
        }*/

// save the added rules to 'saverule.json' in 'multiRule' folder

// get user input(topic, repubTopic, lat, lon, rad)
//        options("/subscriptionInput") {
//            call.respond(HttpStatusCode.OK)
//        }




//        options("/warningMessage/{timeSent}") {
//            call.respond(HttpStatusCode.OK)
//        }