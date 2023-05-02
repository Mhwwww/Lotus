package de.hasenburg.geover


import de.hasenburg.geobroker.commons.setLogLevel
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager

private val logger = LogManager.getLogger()


class WarningGenerator {

//TODO: returen boolean
    suspend fun generateWarnings(functionName: String, payload: String, async: Boolean = true, contentType: String = "application/json") :String{
        //sendReqToTinyFaaS(functionName: String, payload: String, async: Boolean = true, contentType: String = "application/json")
        val priority  = sendReqToTinyFaaS(functionName, payload, async, contentType)

        return priority
    }
}



suspend fun main() {
    setLogLevel(logger, Level.DEBUG)

    val warningGenerator = WarningGenerator()
    //TODO: real testing function
    warningGenerator.generateWarnings("topN", "abc", true, "json")

}
