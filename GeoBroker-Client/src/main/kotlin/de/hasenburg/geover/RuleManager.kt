@file:OptIn(DelicateCoroutinesApi::class)

package de.hasenburg.geover

import cmdDeleteFromtinyFaaS
import cmdUploadTotinyFaaS
import de.hasenburg.geobroker.client.main.SimpleClient
import de.hasenburg.geobroker.client.main.buildBridgeBetweenTopicAndFunction
import de.hasenburg.geobroker.commons.model.message.Payload
import de.hasenburg.geobroker.commons.model.message.Topic
import de.hasenburg.geobroker.commons.model.spatial.Geofence
import de.hasenburg.geobroker.commons.model.spatial.Location
import de.hasenburg.geobroker.commons.setLogLevel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import java.io.File

private val logger = LogManager.getLogger()
val numThread = 1

class RuleManager {

    private val rulesList = ArrayList<UserSpecifiedRule>()

    suspend fun createNewRule(rule: UserSpecifiedRule) {
        //function name should be a string without spaces and special symbols
        var functionName = rule.getFunctionName()

        uploadFileToTinyFaaS(rule.jsFile, rule.env, functionName)
        //send subscription and get new topic set to be subscribed to
        GlobalScope.launch {
                buildBridgeBetweenTopicAndFunction(rule.topic, rule.geofence, functionName, rule.matchingTopic)
        }
        rulesList.add(rule)
    }

    suspend fun deleteRule(rule: UserSpecifiedRule) {

        if(!rulesList.contains(rule)){
            println("there is no such function")
        }

        deleteTinyFaaSFunction(rule.getFunctionName())
        rulesList.remove(rule)
    }

    private fun uploadFileToTinyFaaS(file: File, env: String, functionName: String) {
        // TODO: better way to upload function
        cmdUploadTotinyFaaS(file.absolutePath, functionName, env, numThread)
    }
    private fun deleteTinyFaaSFunction(functionName: String) {
        cmdDeleteFromtinyFaaS(functionName)
    }

}

suspend fun main() {
    setLogLevel(logger, Level.DEBUG)

/*
    // Test out the RuleManager
    println("File Path:")
    val fileName = readLine()!!
    println("Function Environment:(nodejs, python3, binary)")
    val env = readLine()!!
    println("Topic:")
    val topic = Topic(readLine()!!)
    println("lat in Double")
    val lat = readLine()!!.toDouble()
    println("lon in Double")
    val lon = readLine()!!.toDouble()
    println("radius")
    val radius = readLine()!!.toDouble()
* */

    val topic = Topic("/read/1/berlin")
    val matchesTopic = Topic("/read/1/berlin/randomHashValueTBD")
    val geofence = Geofence.circle(Location(0.0, 0.0),2.0)
    val newRule = UserSpecifiedRule(geofence, topic , File("/Users/minghe/tinyFaaS/test/fns/readJSON/"), "nodejs", matchesTopic)

    val ruleManager = RuleManager()
    ruleManager.createNewRule(newRule)

    GlobalScope.launch {
        val client = SimpleClient("localhost", 5559)
        client.send(Payload.CONNECTPayload(geofence.center))
        client.send(Payload.SUBSCRIBEPayload(matchesTopic, geofence))
        client.receive()

        logger.info("I am a client and I just uploaded my functions and now i am waiting for mathes on {}", matchesTopic)
        while (true) {
            val msg = client.receive()
            logger.info("Client topic={} recd={}", topic, msg)
        }

    }

     logger.info("Publish something to Topic $topic")
     logger.info("Press Enter to Stop Program:")
     readLine()
}