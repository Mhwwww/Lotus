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

class RuleManager {

    private val rulesList = ArrayList<UserSpecifiedRule>()

    suspend fun createNewRule(rule: UserSpecifiedRule) {
        //function name should be a string without spaces and special symbols
        var functionName = ""
        for (i in 0..rule.topic.numberOfLevels-1){
                functionName += rule.topic.levelSpecifiers[i]
        }

        uploadFileToTinyFaaS(rule.jsFile, functionName)
        buildBridgeBetweenTopicAndFunction(rule.topic, rule.geofence, functionName)
        rulesList.add(rule)
        logger.info(rulesList)
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
    private fun uploadFileToTinyFaaS(file: File, functionName: String) {
        // - look at the ./scripts/upload.sh script in TinyFaaS
        // - do the same in Kotlin (Maybe ask chatGPT for some input?)
        cmdUploadTotinyFaaS(path = file.absolutePath, functionName,1)
    }
    private fun deleteTinyFaaSFunction(functionName: String) {
        cmdDeleteFromtinyFaaS(functionName)
    }
}

suspend fun main() {
    // Test out the RuleManager
    println("File Path:")
    val fileName = readLine()!!
    println("Topic:")
    val topic = Topic(readLine()!!)
    println("lat in Double")
    val lat = readLine()!!.toDouble()
    println("lon in Double")
    val lon = readLine()!!.toDouble()
    println("radius")
    val radius = readLine()!!.toDouble()

    val geofence = Geofence.circle(Location(lat, lon),radius)
    val newRule = UserSpecifiedRule(geofence, topic, File(fileName))

    val ruleManager = RuleManager()
    ruleManager.createNewRule(newRule)
    //ruleManager.deleteRule(newRule)

    println("Publish something to Topic $topic")
    println("Press Enter to Stop Program:")
    readLine()
}