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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.json.JSONObject
import java.io.File
import kotlin.concurrent.thread
import kotlin.random.Random
import kotlin.system.exitProcess

private val logger = LogManager.getLogger()
class CbfSubscriber(private val loc: Location, private val topic: Topic, private val name: String) {
    private val logger = LogManager.getLogger()
    private var cancel = false
    private lateinit var client: SimpleClient
    private lateinit var processManager: ZMQProcessManager
    fun prepare() {
//        setLogLevel(this.logger, Level.DEBUG)

        logger.debug("{}: Subscribing to {} at {}", name, topic, loc)

        this.processManager = ZMQProcessManager()
        this.client = SimpleClient("localhost", 5559, identity = " CbfSub_${name}")

        logger.debug("{}: sending connect with client id {}", name,  client.identity)

        client.send(Payload.CONNECTPayload(loc))
        logger.debug("{}: ConnAck: {}", name, client.receive())

        client.send(Payload.SUBSCRIBEPayload(topic, Geofence.circle(loc, 2.0)))
        logger.debug("{}: SubAck: {}", name, client.receive())

        logger.info("{}: Subscribed to {} at {}", name, topic, loc)
    }
    fun run() {
        logger.info("{}: Running subscriber for {} at {}", name, topic, loc)
        // some code smells here:
        // receive is blocking, so if we set cancel to true, it won't check until it has received a message in the meantime
        // presumably this still works because the DISCONNECT message will incur an ACK
        while (!this.cancel) {
            // receive one message
            logger.debug("{}: Waiting for message", name)
            val message = this.client.receive()
            val timeReceived = System.nanoTime()
            logger.debug("{}: Relevant Message: {}", name, message)
            if (message is Payload.PUBLISHPayload) {
                val timeSent = JSONObject(message.content).getLong("timeSent")
                logger.info("{}: Time for topic {} difference: {}", name, message.topic, timeReceived - timeSent)
            }
        }
    }

    fun stop() {
        this.cancel = true
        // disconnect
        this.client.send(Payload.DISCONNECTPayload(ReasonCode.NormalDisconnection))
        this.client.tearDownClient()
        this.processManager.tearDown(3000)
        exitProcess(0)
    }
}
fun main() = runBlocking {
//    setLogLevel(logger, Level.DEBUG)
    // Geofence.circle(Location(0.0,0.0), 350.0)
    val newRule = UserSpecifiedRule(locations.map { Geofence.circle(it, 2.0) }, publishTopic, File("GeoBroker-Client/src/main/kotlin/de/hasenburg/geoverdemo/cbf/subscriber/filter/"), "nodejs", matchingTopic)

    val bridgeManager = BridgeManager()
    bridgeManager.createNewRule(newRule)

    val subscribers = mutableListOf<CbfSubscriber>()
    var i = 0

    // prepare subscribers
    locations.forEach {
        val newS = CbfSubscriber(it, publishTopic, "unfiltered_${i}")
        subscribers.add(newS)
        newS.prepare()

        // also prepare a subscriber for the matching topic
        val newS2 = CbfSubscriber(it, matchingTopic, "filtered_${i}")
        subscribers.add(newS2)
        newS2.prepare()

        i++
    }

    subscribers.forEach {
        thread { it.run() }
    }

    logger.info("Press enter to finish subscribers")
    readLine()

    subscribers.forEach{
        it.stop()
    }


    bridgeManager.deleteRule(newRule)
}