package de.hasenburg.geoverdemo.geoVER.kotlin.publisher

import de.hasenburg.geobroker.commons.model.spatial.Geofence
import de.hasenburg.geobroker.commons.model.spatial.Location
import de.hasenburg.geoverdemo.multiRule.publisher.PublishingClient
import org.apache.logging.log4j.LogManager

val PUBLISHER_ID = "Publisher ID"
val TIME_SENT = "Time Sent"
val TEMPERATURE = "Temperature"
val HUMIDITY = "Humidity"
val WIND_SPEED = "Wind Speed"
val WIND_DIRECTION = "Wind Direction"
val WIND_VELOCITY = "Wind Velocity"


private val logger = LogManager.getLogger()

//todo: set the locations later


//broker
var PORT = 5559
var ADDRESS = "localhost"
var REPEAT_TIME = 20


//tinkerforge
var TINKERFORGE_HOST= "localhost"
var TINKERFORGE_PORT= 4223

var UID_OUTDOORWEATHER = "ZPd"

var STATION_ID = 143

//publisher
var PUB_TOPIC = "info"
var PUB_RADIUS = 2.0
var PUB_INTERVAL: Long = 300 //ms
// locations for different publishers
val SCHOENHAGEN_AIRPORT = Geofence.circle(Location(0.0, 0.0), PUB_RADIUS)
val BER_AIRPORT = Geofence.circle(Location(10.0,20.0), PUB_RADIUS)
val FRANKFURT_AIRPORT = Geofence.circle(Location(30.0, 50.0), PUB_RADIUS)


var PUBLISH_GEOFENCE = SCHOENHAGEN_AIRPORT

class PubConfiguration{
    init {
        logger.info("Hello from the PUBLISHER Configuration init")

        val stationID  = System.getenv("STATION_ID")

        if (stationID != null) {
            logger.info("STATION_ID: $stationID")
            STATION_ID = stationID.toInt()
        } else {
            logger.info("DEFAULT STATION_ID is {}", STATION_ID)
        }

        val tHost  = System.getenv("TINKERFORGE_HOST")

        if (tHost != null) {
            logger.info("TINKERFORGE_HOST: $tHost")
            TINKERFORGE_HOST = tHost
        } else {
            logger.info("DEFAULT TINKERFORGE_HOST is {}", TINKERFORGE_HOST)
        }


        val pubGeofence  = System.getenv("PUBLISH_GEOFENCE")

        if (pubGeofence != null) {
            logger.info("PUBLISH_GEOFENCE: $pubGeofence")

            when (pubGeofence) {
                "Berlin Airport" -> {
                    PUBLISH_GEOFENCE = BER_AIRPORT
                }
                "Schoenhagen Airport", "Schönhagen Airport" -> {
                    PUBLISH_GEOFENCE = SCHOENHAGEN_AIRPORT
                }
                "Frankfurt Airport" -> {
                    PUBLISH_GEOFENCE = FRANKFURT_AIRPORT
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

        val addr = System.getenv("ADDRESS")

        if (addr != null) {
            logger.info("ADDRESS: $addr")
            ADDRESS = addr
        } else {
            logger.info("DEFAULT ADDRESS is: {}", ADDRESS)
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


fun main(){
    PubConfiguration()
    PublishingClient().startPublisherClient()

    //get data from station 1
//    OutdoorWeatherBrickletPublishingClient().startOutdoorBrickletPublisher(STATION_ID)

}
