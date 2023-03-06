package de.hasenburg.geoverdemo.condensing.subscriber

import de.hasenburg.geobroker.client.main.SimpleClient
import de.hasenburg.geobroker.commons.communication.ZMQProcessManager
import de.hasenburg.geobroker.commons.model.message.Payload
import de.hasenburg.geobroker.commons.model.message.ReasonCode
import de.hasenburg.geobroker.commons.model.message.Topic
import de.hasenburg.geobroker.commons.model.spatial.Geofence
import de.hasenburg.geobroker.commons.model.spatial.Location
import de.hasenburg.geobroker.commons.setLogLevel
import de.hasenburg.geover.BridgeManager
import de.hasenburg.geover.UserSpecifiedRule
import de.hasenburg.geoverdemo.condensing.common.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import java.io.File
import kotlin.random.Random
import kotlin.system.exitProcess

private val logger = LogManager.getLogger()
class CondensingSubscriber(private val loc: Location, private val topic: Topic) {
    private val logger = LogManager.getLogger()
    var interruptMe = false
    fun run() {
        logger.debug("Subscribing to {} at {}", topic, loc)

        val processManager = ZMQProcessManager()
        val client = SimpleClient("localhost", 5559, identity = " CondenseSub_${topic.topic}_${loc.point}_${Random.nextInt()}")

        client.send(Payload.CONNECTPayload(loc))
        logger.debug("ConnAck: {}", client.receive())

        client.send(Payload.SUBSCRIBEPayload(topic, Geofence.circle(loc, 2.0)))
        logger.debug("SubAck: {}", client.receive())

        while (!interruptMe) {
            // receive one message
            val message = client.receive()
            logger.info("Relevant Message: {}", message)
        }

        // disconnect
        client.send(Payload.DISCONNECTPayload(ReasonCode.NormalDisconnection))
        client.tearDownClient()
        processManager.tearDown(3000)
        exitProcess(0)
    }
}

@OptIn(DelicateCoroutinesApi::class)
suspend fun main() {
    setLogLevel(logger, Level.DEBUG)
    // Geofence.circle(Location(0.0,0.0), 350.0)
    val newRule = UserSpecifiedRule(locations.map { Geofence.circle(it, 2.0) }, publishTopic, File("GeoBroker-Client/src/main/kotlin/de/hasenburg/geoverdemo/condensing/subscriber/condense/"), "nodejs", subscriberTopic)

    val bridgeManager = BridgeManager()
    bridgeManager.createNewRule(newRule)


    val subscribers = mutableListOf<CondensingSubscriber>()
    locations.forEach{location ->
        GlobalScope.launch {
            repeat(clientsPerLocation) {
                val newS = CondensingSubscriber(location, subscriberTopic)
                subscribers.add(newS)
                newS.run()
            }
        }
    }

    logger.info("Press enter to finish subscribers")
    readLine()

    subscribers.forEach{it.interruptMe = true}
    bridgeManager.deleteRule(newRule)
}