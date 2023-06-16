
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

fun Application.applyRouting(){
    routing {
        //static page for localhost:8081/index.html
        static("/") {
            resources("web")
        }

/*        // apply subscription
        options("/clientInput") {
            call.respond(HttpStatusCode.OK)
        }
        post("/clientInput") {
            try {
                val inputEvent = call.receive<InputEvent>()
                println("Received inputEvent: $inputEvent")

                call.respond("post test successfully")

            } catch (e: Exception) {
                val errorMessage = e.message ?: "Unknown error"
                call.respond(status = HttpStatusCode.BadRequest, ErrorResponseEvent(errorMessage))
            }
        }*/
        // show warnings
        options("/show") {
            call.respond(HttpStatusCode.OK)
        }
        post("/show") {
            try {
                val inputEvent = call.receive<InputEvent>()
                println("Received inputEvent: $inputEvent")
                call.respond("post show successfully")

                GlobalScope.launch {
                    runRuleSubscriber(geoBrokerPara(inputEvent))
                }
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Unknown error"
                call.respond(status = HttpStatusCode.BadRequest, ErrorResponseEvent(errorMessage))
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
        options("/saveRules") {
            call.respond(HttpStatusCode.OK)
        }
        post("/saveRules") {
            try {
                //receive the forwarded rule set
                val rules: List<InputRule> = call.receive()
                val json = Json.encodeToString(rules)

                //val file = File("./GeoBroker-Client/src/main/kotlin/de/hasenburg/geoverdemo/multiRule/subscriber/ruleJson/saverule.json")
                val file = File("./GeoBroker-Client/src/main/kotlin/de/hasenburg/geoverdemo/crossWind/subscriber/ruleJson/saverule.json")

                // clear file
                file.writeText("")
                file.writeText("$json\n")

            } catch (e: Exception) {
                val errorMessage = e.message ?: "Unknown error"
                call.respond(status = HttpStatusCode.BadRequest, ErrorResponseRule(errorMessage))
            }
        }
        //get the events from 'warnings' topic
        get("/warningMessage"){
            call.respond(warningArray.toString())
        }

        options("/warningMessage/{timeSent}") {
            call.respond(HttpStatusCode.OK)
        }
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
                    val oriTimeSent = message.optLong("timeSent")

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
            val responseMessage = "Remaining: $remainingCount warnings"

            call.respond(responseMessage)
        }

        get("/infoMessage"){
            call.respond(infoArray.toString())
        }

    }
}