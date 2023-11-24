package de.hasenburg.geoverdemo.geoVER.kotlin.publisher

import de.hasenburg.geobroker.commons.model.spatial.Geofence
import de.hasenburg.geobroker.commons.model.spatial.Location
import de.hasenburg.geoverdemo.geoVER.kotlin.BROKER_HOST
import de.hasenburg.geoverdemo.multiRule.publisher.PublishingClient
import kotlinx.coroutines.*
import org.apache.logging.log4j.LogManager
import temperaturePublisher

val PUBLISHER_ID = "Publisher ID"
val TIME_SENT = "Time Sent"
val TEMPERATURE = "Temperature"
val HUMIDITY = "Humidity"
val WIND_DIRECTION = "Wind Direction"
val WIND_VELOCITY = "Wind Velocity"

private val logger = LogManager.getLogger()
//broker
var PORT = 5559
var ADDRESS = BROKER_HOST
var REPEAT_TIME = 10000000

//tinkerforge
var TINKERFORGE_HOST = BROKER_HOST
var TINKERFORGE_PORT = 4223

// outdoor weather bricklet
var UID_OUTDOORWEATHER = "ZPd"
var STATION_ID = 143

// lcd bricklet todo: environment parameter
var UID_LCD = "24Qa"
var WIDTH: Short = 128
var HEIGHT: Short = 64

// segment bricklet
var UID_SEGMENT = "TvZ"

//publisher
var PUB_TOPIC = "crosswind"
var PUB_RADIUS = 2.0
var PUB_INTERVAL: Long = 1000 //ms

var TEMPERATURE_PUB_TOPIC = "snow"

// locations for different publishers
val SCHOENHAGEN_AIRPORT = Geofence.circle(Location(1.0, 1.0), PUB_RADIUS)
val HAMBURG_AIRPORT = Geofence.circle(Location(10.0, 20.0), PUB_RADIUS)
val DRESDEN_AIRPORT = Geofence.circle(Location(30.0, 50.0), PUB_RADIUS)

val WEATHER_STATION = Geofence.circle(Location(0.0, 0.0), PUB_RADIUS)

var PUBLISH_GEOFENCE = SCHOENHAGEN_AIRPORT

class PubConfiguration {
    init {
        logger.info("Hello from the PUBLISHER Configuration init")

        val crosswindTopic = System.getenv("TOPIC")
        if (crosswindTopic !=null){
            PUB_TOPIC = crosswindTopic
            logger.info("PUB_TOPIC is: $crosswindTopic")
        }else{
            logger.info("DEFAULT PUB_TOPIC is {}", PUB_TOPIC)
        }

        val temperaturePubTopic = System.getenv("TEMPERATURE_TOPIC")
        if (temperaturePubTopic != null){
            logger.info("TEMPERATURE_PUB_TOPIC: $temperaturePubTopic")
            TEMPERATURE_PUB_TOPIC = temperaturePubTopic
        }else{
            logger.info("DEFAULT TEMPERATURE_PUB_TOPIC is {}", TEMPERATURE_PUB_TOPIC)
        }


        val stationID = System.getenv("STATION_ID")

        if (stationID != null) {
            logger.info("STATION_ID: $stationID")
            STATION_ID = stationID.toInt()
        } else {
            logger.info("DEFAULT STATION_ID is {}", STATION_ID)
        }

        val tHost = System.getenv("TINKERFORGE_HOST")

        if (tHost != null) {
            logger.info("TINKERFORGE_HOST: $tHost")
            TINKERFORGE_HOST = tHost
        } else {
            logger.info("DEFAULT TINKERFORGE_HOST is {}", TINKERFORGE_HOST)
        }

        val pubGeofence = System.getenv("PUBLISH_GEOFENCE")

        if (pubGeofence != null) {
            logger.info("PUBLISH_GEOFENCE: $pubGeofence")

            when (pubGeofence) {
                "Hamburg" -> {
                    PUBLISH_GEOFENCE = HAMBURG_AIRPORT
                }

                "Schoenhagen", "Schönhagen Airport" -> {
                    PUBLISH_GEOFENCE = SCHOENHAGEN_AIRPORT
                }

                "Dresden" -> {
                    PUBLISH_GEOFENCE = DRESDEN_AIRPORT
                }
            }

        } else {
            logger.info("DEFAULT PUBLISH_GEOFENCE is {}", PUBLISH_GEOFENCE)
        }

        val pubInterval = System.getenv("PUB_INTERVAL")

        if (pubInterval != null) {
            logger.info("PUB_INTERVAL: $pubInterval")
            PUB_INTERVAL = pubInterval.toLong()
        } else {
            logger.info("DEFAULT PUB_INTERVAL is {}", PUB_INTERVAL)
        }

        val pubRadius = System.getenv("PUB_RADIUS")

        if (pubRadius != null) {
            logger.info("PUB_RADIUS: $pubRadius")
            PUB_RADIUS = pubRadius.toDouble()
        } else {
            logger.info("DEFAULT PUB_RADIUS {}", PUB_RADIUS)
        }

        val addrAirport = System.getenv("BROKER_ADDRESS")

        if (addrAirport != null) {
            logger.info("BROKER_ADDRESS: $addrAirport")
            ADDRESS = addrAirport
        } else {
            logger.info("DEFAULT BROKER_ADDRESS is: {}", ADDRESS)
        }

        val port = System.getenv("PORT")

        if (port != null) {
            logger.info("PORT: $port")
            PORT = port.toInt()
        } else {
            logger.info("DEFAULT PORT is {}", PORT)
        }

        val repeat = System.getenv("REPEAT_TIME")

        if (repeat != null) {
            logger.info("REPEAT_TIME: $repeat")
            REPEAT_TIME = repeat.toInt()
        } else {
            logger.info("DEFAULT REPEAT_TIME is {}", REPEAT_TIME)
        }

    }
}

@OptIn(DelicateCoroutinesApi::class)
fun main() = runBlocking{
//fun main(){
    PubConfiguration()
    //PublishingClient().startPublisherClient(ADDRESS)// normal publisher

//    Tinkerforge Publisher
//    val job1 = GlobalScope.async {
//        //OutdoorWeatherBrickletPublishingClient().startOutdoorBrickletPublisher(STATION_ID, ADDRESS)//tinkerforge
//        //realTimeWindSpeed()
//    }
//    // random data DT publisher
//    val job2 = GlobalScope.async {
//        repeat(REPEAT_TIME) {
//            sendFakeData()
//            sleep(2000, 0)
//        }
//    }
//
    val job3 = GlobalScope.async {
        temperaturePublisher(BROKER_HOST)
    }

    val job4 = GlobalScope.async {
        PublishingClient().startPublisherClient(ADDRESS)// normal publisher
    }

//    job1.await()
//    job2.await()
    job3.await()
    job4.await()

}


