package de.hasenburg.geover

import de.hasenburg.geobroker.commons.model.message.Topic
import de.hasenburg.geobroker.commons.model.spatial.Geofence
import org.apache.logging.log4j.LogManager
import java.io.File
import java.math.BigInteger
import java.security.MessageDigest

//jsFile--rule.json

private val logger = LogManager.getLogger()
private val RADIUS = 2.0//if geofence is circle

data class UserSpecifiedRule(val geofence: Geofence, val topic: Topic, val jsFile: File, val env: String, val matchingTopic: Topic, val createdAt: Long = System.currentTimeMillis()){

    fun getFunctionName(): String {
        val hasher = MessageDigest.getInstance("MD5")
        return BigInteger(hasher.digest(this.toString().toByteArray())).abs().toString(16).padStart(32, '0')
    }

}
