package de.hasenburg.geover

import de.hasenburg.geobroker.commons.model.message.Topic
import de.hasenburg.geobroker.commons.model.spatial.Geofence
import org.apache.logging.log4j.LogManager
import java.io.File
import java.math.BigInteger
import java.security.MessageDigest

//jsFile--aircraftType.json

private val logger = LogManager.getLogger()
private val RADIUS = 2.0//if geofence is circle

data class UserSpecifiedRule(
    val geofences: List<Geofence>,
    val topic: Topic,
    val jsFile: File,
    val env: String,
    val matchingTopic: Topic,
    val createdAt: Long = System.currentTimeMillis()
){
    constructor(//work for single geofence--return list of the input
        geofence: Geofence,
        topic: Topic,
        jsFile: File,
        env: String,
        matchingTopic: Topic,
        createdAt: Long = System.currentTimeMillis()
    ): this(listOf(geofence), topic, jsFile, env, matchingTopic, createdAt)
    fun getFunctionName(): String {//generate name for function
        val hasher = MessageDigest.getInstance("MD5")
        return BigInteger(hasher.digest(this.toString().toByteArray())).abs().toString(16).padStart(32, '0')
    }

}
