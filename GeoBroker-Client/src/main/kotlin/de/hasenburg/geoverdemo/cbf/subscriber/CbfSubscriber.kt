package de.hasenburg.geoverdemo.cbf.subscriber

import de.hasenburg.geobroker.client.main.SimpleClient
import de.hasenburg.geobroker.commons.communication.ZMQProcessManager
import de.hasenburg.geobroker.commons.model.message.Payload
import de.hasenburg.geobroker.commons.model.message.ReasonCode
import de.hasenburg.geobroker.commons.model.message.Topic
import de.hasenburg.geobroker.commons.model.spatial.Geofence
import de.hasenburg.geobroker.commons.model.spatial.Location
import de.hasenburg.geobroker.commons.setLogLevel
import de.hasenburg.geobroker.commons.sleep
import de.hasenburg.geover.BridgeManager
import de.hasenburg.geover.UserSpecifiedRule
import de.hasenburg.geoverdemo.cbf.common.*
import kotlinx.coroutines.*
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import java.io.File
import kotlin.random.Random
import kotlin.system.exitProcess

private val logger = LogManager.getLogger()
class CbfSubscriber(private val loc: Location, private val topic: Topic) {
    private val logger = LogManager.getLogger()
    var interruptMe = false
    fun run() {
        logger.debug("Subscribing to {} at {}", topic, loc)

        val processManager = ZMQProcessManager()
        val client = SimpleClient("localhost", 5559, identity = " CbfSub_${topic.topic}_${loc.point}_${Random.nextInt()}")

        logger.debug("sending connect with client id ${client.identity}")

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
suspend fun main() = runBlocking {
    setLogLevel(logger, Level.DEBUG)
    // Geofence.circle(Location(0.0,0.0), 350.0)
    val newRule = UserSpecifiedRule(locations.map { Geofence.circle(it, 2.0) }, publishTopic, File("GeoBroker-Client/src/main/kotlin/de/hasenburg/geoverdemo/cbf/subscriber/readJSON/"), "nodejs", matchingTopic)

    val bridgeManager = BridgeManager()
    bridgeManager.createNewRule(newRule)

    val subscribers = mutableListOf<CbfSubscriber>()
    locations.forEach{
        launch {
            val newS = CbfSubscriber(it, matchingTopic)
            subscribers.add(newS)
            newS.run()
            sleep(100, 0)
        }
    }

    logger.info("Press enter to finish subscribers")
    readLine()

    subscribers.forEach{it.interruptMe = true}
    bridgeManager.deleteRule(newRule)
}