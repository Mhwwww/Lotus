package de.hasenburg.geobroker.client.main

import de.hasenburg.geobroker.commons.communication.ZMQProcessManager
import de.hasenburg.geobroker.commons.model.message.Payload
import de.hasenburg.geobroker.commons.model.message.ReasonCode
import de.hasenburg.geobroker.commons.model.message.Topic
import de.hasenburg.geobroker.commons.model.spatial.Geofence
import de.hasenburg.geobroker.commons.model.spatial.Location
import de.hasenburg.geobroker.commons.sleepNoLog
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

    client.send(Payload.PUBLISHPayload(Topic("warnings"),Geofence.circle(Location(0.0,0.0),2.0),"{\n" +
            "    \"temperature\":35.0,\n" +
            "    \"speed\":20.0,\n" +
            "    \"wind\":20.0,\n" +
            "    \"wet\": 40.0\n" +
            "  }"))

    logger.info("test topic: {}", client.receive())

    client.send(Payload.PUBLISHPayload(Topic("/read/1/berlin"),Geofence.circle(location,2.0),"{\n" +
            "    \"temperature\":35.0,\n" +
            "    \"speed\":20.0,\n" +
            "    \"wind\":20.0,\n" +
            "    \"wet\": 40.0\n" +
            "  }"))

    logger.info("!!!!!!!!111111111111111: {}", client.receive())

    client.send(Payload.PUBLISHPayload(Topic("/read/1/berlin"),Geofence.circle(location,2.0),"{\n" +
            "    \"temperature\":35.0,\n" +
            "    \"speed\":20.0,\n" +
            "    \"wind\":20.0,\n" +
            "    \"wet\": 60.0\n" +
            "  }"))

    logger.info("!!!!!!!!999999999999991: {}", client.receive())


    client.send(Payload.PUBLISHPayload(Topic("read"),Geofence.circle(location,2.0),"{" +
            "    \"temperature\" : 35.0," +
            "    \"speed\" : 25.0," +
            "    \"wind\":20.0,\n" +
            "  }"))
    logger.info("!2222222222222222: {}", client.receive())

    client.send(Payload.PUBLISHPayload(Topic("sieve"),Geofence.circle(location,2.0),"{" +
            "    \"temperature\" : 35.0," +
            "    \"speed\" : 25.0," +
            "    \"wind\":20.0,\n" +
            "  }"))
    logger.info("!444444444444444444: {}", client.receive())

    val topics = listOf("/read/1/berlin")

    for (topic in topics){
        // publish
        client.send(Payload.PUBLISHPayload(Topic(topic),Geofence.circle(location,2.0),"{\n" +
                "    \"temperature\":35.0,\n" +
                "    \"speed\":20.0,\n" +
                "    \"wind\":20.0,\n" +
                "    \"wet\": 50.0\n" +
                "  }"))

        // receive one message
        logger.info("3333333333: {}", client.receive())

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

