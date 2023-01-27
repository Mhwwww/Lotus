package de.hasenburg.geover

import de.hasenburg.geobroker.commons.model.message.Topic
import de.hasenburg.geobroker.commons.model.spatial.Geofence
import de.hasenburg.geobroker.commons.model.spatial.toJson
import org.apache.logging.log4j.LogManager
import org.json.JSONObject
import java.io.File
//jsFile--rule.json

private val logger = LogManager.getLogger()
private val RADIUS = 2.0//if geofence is circle

data class UserSpecifiedRule(val geofence: Geofence, val topic: Topic, val jsFile: File, val env:String){

    fun getUserSubscription():JSONObject{
        val locJson = JSONObject()
        locJson.put("lat", geofence.center.lat)
        locJson.put("lon", geofence.center.lon)
        locJson.put("radius", RADIUS)

        logger.info("geofence is {}",geofence.toJson())

        val userSpecifiedRule = JSONObject()
        userSpecifiedRule.put("topic",topic.topic)
        userSpecifiedRule.put("functionName", jsFile.name)
        userSpecifiedRule.put("location",  locJson)

        return userSpecifiedRule
    }

}
