package de.hasenburg.geobroker.client.main

import de.hasenburg.geobroker.commons.communication.ZMQProcessManager
import de.hasenburg.geobroker.commons.model.message.Payload
import de.hasenburg.geobroker.commons.model.message.ReasonCode
import de.hasenburg.geobroker.commons.model.message.Topic
import de.hasenburg.geobroker.commons.model.spatial.Geofence
import de.hasenburg.geobroker.commons.model.spatial.Location
import de.hasenburg.geobroker.commons.sleepNoLog
import kotlinx.serialization.SerialName
import org.apache.logging.log4j.LogManager
import kotlin.system.exitProcess

private val logger = LogManager.getLogger()
private val location = Location(0.0,0.0)

fun main() {

    val processManager = ZMQProcessManager()
    val client = SimpleClient("localhost", 5559)

    // connect
    client.send(Payload.CONNECTPayload(location))

    // receive one message
    logger.info("Received server answer: {}", client.receive())


    val topics = listOf("test","read","sieve")

    for (topic in topics){
        // publish
        client.send(Payload.PUBLISHPayload(Topic(topic),Geofence.circle(location,2.0),"{" +
                "    \"temperature\" : 5.0," +
                "    \"speed\" : 25.0," +
                "    \"wind\": 3.0" +
                "  }"))

        // receive one message
        logger.info("Received server answer: {}", client.receive())
        // wait 5 seconds
        sleepNoLog(5000, 0)
    }

    // disconnect
    client.send(Payload.DISCONNECTPayload(ReasonCode.NormalDisconnection))

    client.tearDownClient()
    if (processManager.tearDown(3000)) {
        logger.info("SimpleClient shut down properly.")
    } else {
        logger.fatal("ProcessManager reported that processes are still running: {}",
            processManager.incompleteZMQProcesses)
    }
    exitProcess(0)

}

