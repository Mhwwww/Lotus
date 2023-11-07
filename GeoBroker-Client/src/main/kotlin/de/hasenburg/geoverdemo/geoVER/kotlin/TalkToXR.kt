package de.hasenburg.geoverdemo.geoVER.kotlin

import applyRouting
import configureHTTP
import configureMonitoring
import de.hasenburg.geover.startTinyFaaS
import de.hasenburg.geoverdemo.geoVER.kotlin.plugin.configureSerialization
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.util.reflect.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.logging.log4j.LogManager
import org.json.JSONObject
import java.util.*

private val logger = LogManager.getLogger()

// DT Websocket URLs
const val DT_ALL_URL= "urn:tlabs:geover:ws:receiver:all" //Forward message to all connected clients (including myself). This is also default if no target property is set
const val DT_INFO_URL = "urn:tlabs:geover:ws:info" //Special target. Server sends a message back with some info (e.g. own sender ID, connected clients, etc.).
const val DT_SELF_URL = "urn:tlabs:geover:ws:receiver:self" //Forward message only back to me.
const val DT_ALL_EXCEPT_SELF_URL = "urn:tlabs:geover:ws:receiver:others" //Forward message to all connected clients (excluding myself).
const val DT_SINGLE_URL = "urn:tlabs:geover:ws:receiver:single" //Forward message to a single client. The receiver property must be set and should contain a sender ID.
const val DT_MUTI_URL = "urn:tlabs:geover:ws:receiver:multiple" //Forward message to multiple clients. The receivers property must be set and should contain an array of sender IDs.

/**
 * - send messages to "all" --> there is only one receiver...
 * - we don't care about answers, we just want to print them to terminal.
 */

class TalkToXR {
    @Serializable
    data class WindData(
        //TODO: use integer late
        val direction: String,
        val directionType: String,
        val speed: Double,
        val speedType: String
    )
    @Serializable
    data class SensorData(
        val sensor: String,
        val wind: WindData
    )
    @Serializable
    data class WebSocketMessage(
        val target: String? = null,
        val request: String? = null,
        val receiver: String? = null,
        val receivers: List<String>? = null,
        val data: SensorData? = null
    )

    private val websocketClient = HttpClient {
        install(WebSockets)
    }

    private var isClientRunning = false
    private var isClientStartSuccesful = false

    val channel = Channel<String>(10_00)

    @OptIn(DelicateCoroutinesApi::class)
    private suspend fun startClient() {
        isClientRunning = true
        synchronized(channel) {
            if (isClientStartSuccesful) {
                return
            } else {
                isClientStartSuccesful = true
            }
            GlobalScope.launch {
                websocketClient.webSocket(
                    method = HttpMethod.Get,
                    host = "master.tv.labs-exit.de",
                    port = 80,
                    path = "/geover-ws/api"
                ) {
                    //isClientRunning = true
                    val job1 = launch {
                        while (true) {
                            logger.info("Waiting for incoming messages")
                            val incoming = incoming.receive() as? Frame.Text ?: continue
                            val asString = incoming.readText()
                            logger.info("Received message from WebSockets: {}", asString)
                        }
                    }
                    val job2 = launch {
                        logger.info("Waiting for Messages to send to WebSocket")
                        while (true) {
                            val msg = channel.receive()
                            logger.info("Will send message to webSocket: {}", msg)
                            send(Frame.Text(msg))
                            logger.info("Sent message")

                        }
                    }
                    job1.join()
                    job2.join()
                }
            }
        }
    }

    suspend fun sendWarning(warning: String) {
        if (!isClientRunning) {
            startClient()
        }
        val jsonObject = JSONObject(warning)

        //val sensor = jsonObject.get("publisher ID").toString()
        val sensor = "live_demo_1"
        val direction = jsonObject.get("windDirection").toString()
        var speed = jsonObject.get("windVelocity") as Double
        speed = String.format("%.2f", speed).toDouble()

        val windData = WindData(
            direction = direction,
            directionType = "id",
            speed = speed,
            speedType = "ms"
        )
        val sensorData = SensorData(
            sensor = sensor,
            wind = windData
        )
        val json = WebSocketMessage(
            target = DT_ALL_EXCEPT_SELF_URL,
            data = sensorData
        )

        val msg = Json.encodeToString(json)
        channel.send(msg)
    }

    public suspend fun sendRawString(message: String) {
        if (!isClientRunning) {
            startClient()
        }
        channel.send(message)
    }
}

fun main(args: Array<String>) {
    //val ruleArray= JSONArray()
    startTinyFaaS()
    Configuration()

    val server = embeddedServer(Netty, port = PORT) {
        applyRouting()
        configureSerialization()
        configureHTTP()
        configureMonitoring()
    }
    server.start(wait = true)
}
