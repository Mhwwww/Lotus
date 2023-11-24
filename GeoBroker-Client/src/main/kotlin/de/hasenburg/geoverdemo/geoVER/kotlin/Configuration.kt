package de.hasenburg.geoverdemo.geoVER.kotlin

import InputEvent
import de.hasenburg.geobroker.commons.setLogLevel
import geoBrokerPara
import kotlinx.serialization.Serializable
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import runRuleSubscriber1
import java.io.File

//configuration on raspis
//var TINYFASS_PATH ="/home/pi/Documents/tinyFaaS/"
//var FUNCTION_FILE_PATH="/home/pi/geover/Lotus/GeoBroker-Client/src/main/kotlin/de/hasenburg/geoverdemo/crossWind/subscriber/ruleJson/"
//Broker host & port configuration
var BROKER_HOST = "localhost"

//tinyFaas
var TINYFASS_PATH = "/Users/minghe/geobroker/tinyFaaS/"

//var FUNCTION_FILE_PATH = INPUT_FUNCTION_PATH
//var SAVE_RULES_JSON_PATH = FUNCTION_FILE_PATH+"/saverule.json/"

//influx_db

val URL = "http://"+BROKER_HOST+":8086"
val ORGANIZATION = "geover"
val WARNING_BUCKET = "warning"
val INFO_BUCKET = "info"

var TOKEN =
    "cDcQwBEUylxWSIYO6t5R4Wx9Id2kbLw-Vs87Wozn649_6QTYcuQCnS5Hu0UBhCBWpmdzoAUH1B7h9ZDN2SxjKw==".toCharArray()

//ktor
var KTOR_PORT = 8082
var KTOR_ADDRESS = "localhost"

var INFO_URL = "http://"+ KTOR_ADDRESS+":"+ KTOR_PORT+"/infoMessage"
var WARNING_URL = "http://"+ KTOR_ADDRESS+":"+ KTOR_PORT+"/warningMessage"

var SUBSCRIPTION_FRONTEND_INPUT_URL = "http://"+ KTOR_ADDRESS+":"+ KTOR_PORT+"/subscriptionInput"
var RULES_FRONTEND_INPUT_URL = "http://"+ KTOR_ADDRESS+":"+ KTOR_PORT+"/saveRules"

private val logger = LogManager.getLogger()

@Serializable
data class ConfigData(
    val infoUrl: String? = null,
    val warningUrl: String? = null,
    val subscriptionFrontEndInputUrl: String? = null,
    val ruleFrontendInputUrl: String? = null,
)

class Configuration {
    init {
        setLogLevel(logger, Level.ERROR)

        logger.info("Hello from the Configuration init")
        val brokerHost = System.getenv("BROKER_HOST")
        if (brokerHost!=null){
            logger.info("BROKER_HOST $brokerHost")
            BROKER_HOST = brokerHost
        }else{
            logger.info("DEFAULT BROKER_HOST is {}",BROKER_HOST)
    }

        val influxdbToken = System.getenv("TOKEN")
        if (influxdbToken!=null){
            logger.info("TOKEN: $influxdbToken")
            TOKEN = influxdbToken.toCharArray()
        }else {
            logger.info("DEFAULT InfluxDB Token is {}",TOKEN)
        }

        val tinyFaasPath = System.getenv("TINYFAAS_PATH")
        if (tinyFaasPath != null) {
            logger.info("TINYFAAS_PATH: $tinyFaasPath")
            TINYFASS_PATH = tinyFaasPath
        } else {
            logger.info("DEFAULT TINYFAAS_PATH {}",TINYFASS_PATH)
        }

//        val functionPath = System.getenv("FUNCTION_FILE_PATH")
//        if (functionPath != null) {
//            logger.info("FUNCTION_FILE_PATH: $functionPath")
//            FUNCTION_FILE_PATH = functionPath
//        } else {
//            logger.info("DEFAULT FUNCTION_FILE_PATH {}",FUNCTION_FILE_PATH)
//        }
////


        val port = System.getenv("KTOR_PORT")
        if (port != null) {
            logger.info("KTOR_PORT: $port")
            KTOR_PORT = port.toIntOrNull()!!

        } else {
            logger.info("DEFAULT KTOR_PORT {}", KTOR_PORT)
        }

        val address = System.getenv("KTOR_ADDRESS")
        if (address != null) {
            logger.info("KTOR_ADDRESS: $address")
            KTOR_ADDRESS = address

        } else {
            logger.info("DEFAULT KTOR_ADDRESS {}", KTOR_ADDRESS)
        }

        //saveConfig()
    }
}


fun saveConfig(){
    val configJsonFile = File("/Users/minghe/geobroker/GeoBroker-Client/src/main/resources/web/js/config.js")
//    val configJsonFile = File("/home/pi/geover/Lotus/GeoBroker-Client/src/main/resources/web/js/config.js")

    INFO_URL = "http://"+ KTOR_ADDRESS+":"+ KTOR_PORT+"/infoMessage"
     WARNING_URL = "http://"+ KTOR_ADDRESS+":"+ KTOR_PORT+"/warningMessage"

     SUBSCRIPTION_FRONTEND_INPUT_URL = "http://"+ KTOR_ADDRESS+":"+ KTOR_PORT+"/subscriptionInput"
     RULES_FRONTEND_INPUT_URL = "http://"+ KTOR_ADDRESS+":"+ KTOR_PORT+"/saveRules"

    val content = """
        let warningMsgUrl ='$WARNING_URL';
        let subscriptionInputUrl = '$SUBSCRIPTION_FRONTEND_INPUT_URL';
        let saveRuleUrl = '$RULES_FRONTEND_INPUT_URL';
        let infoMsgUrl = '$INFO_URL';
    """.trimIndent()

    println(content)
   // configJsonFile.writeText(content)
}

fun main(){
    Configuration()

    val rule = geoBrokerPara(InputEvent(topic= "crosswind", repubTopic = "weather", locationName = "Weather Station", rad = "80", functionName = "Crosswind"))
//    val rule =  UserSpecifiedRule(Geofence.circle(WEATHER_STATION.center, 80.0), Topic(WEATHER_INFO_TOPIC), File(CROSSWIND_PATH), "nodejs", Topic(WEATHER_WARNING_TOPIC))

    runRuleSubscriber1(rule)
}


