package de.hasenburg.geover

import cmdDeleteFromtinyFaaS
import cmdUploadTotinyFaaS
import de.hasenburg.geobroker.client.main.buildBridgeBetweenTopicAndFunction
import de.hasenburg.geobroker.commons.model.message.Topic
import de.hasenburg.geobroker.commons.model.spatial.Geofence
import de.hasenburg.geobroker.commons.model.spatial.Location
import org.apache.logging.log4j.LogManager
import java.io.File

private val logger = LogManager.getLogger()
val numThread = 1

class RuleManager {

    private val rulesList = ArrayList<UserSpecifiedRule>()

    suspend fun createNewRule(rule: UserSpecifiedRule) {
        //function name should be a string without spaces and special symbols
        var functionName = ""
        for (i in 0..rule.topic.numberOfLevels-1){
                functionName += rule.topic.levelSpecifiers[i]
        }

        uploadFileToTinyFaaS(rule.jsFile, rule.env, functionName)
        //send subscription and get new topic set to be subscribed to
        val topicSet = buildBridgeBetweenTopicAndFunction(rule.topic, rule.geofence, functionName)
        rulesList.add(rule)
    }

    suspend fun deleteRule(rule: UserSpecifiedRule) {
        var functionName = ""
        for (i in 0..rule.topic.numberOfLevels-1){
            functionName += rule.topic.levelSpecifiers[i]
        }

        if(!rulesList.contains(rule)){
            println("there is no such function")
        }

        deleteTinyFaaSFunction(functionName)
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
    val geofence = Geofence.circle(Location(0.0, 0.0),2.0)
    val newRule = UserSpecifiedRule(geofence, topic , File("/Users/minghe/tinyFaaS/test/fns/readJSON/"), "nodejs")

    val ruleManager = RuleManager()
    ruleManager.createNewRule(newRule)

     println("Publish something to Topic $topic")
     println("Press Enter to Stop Program:")
     readLine()
}