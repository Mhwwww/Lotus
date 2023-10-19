package de.hasenburg.geoverdemo.geoVER.kotlin

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import io.ktor.server.application.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.logging.log4j.LogManager
import org.json.JSONObject

// DT Websocket URLs
//const val DT_ALL_URL= "urn:tlabs:geover:ws:receiver:all" //Forward message to all connected clients (including myself). This is also default if no target property is set
//const val DT_INFO_URL = "urn:tlabs:geover:ws:info" //Special target. Server sends a message back with some info (e.g. own sender ID, connected clients, etc.).
//const val DT_SELF_URL = "urn:tlabs:geover:ws:receiver:self" //Forward message only back to me.
//const val DT_ALL_EXCEPT_SELF_URL = "urn:tlabs:geover:ws:receiver:others" //Forward message to all connected clients (excluding myself).
//const val DT_SINGLE_URL = "urn:tlabs:geover:ws:receiver:single" //Forward message to a single client. The receiver property must be set and should contain a sender ID.
//const val DT_MUTI_URL = "urn:tlabs:geover:ws:receiver:multiple" //Forward message to multiple clients. The receivers property must be set and should contain an array of sender IDs.
private val logger = LogManager.getLogger()

class Websocket {
    //TODO: data format
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

    @OptIn(ExperimentalSerializationApi::class)
    val websocketClient = HttpClient {
        install(WebSockets) {
            contentConverter = KotlinxWebsocketSerializationConverter(Json)
        }
    }

    suspend fun info(): String? {//{"version":"1.0.2","sender":"b2","secure":false,"clients":1}
        val result = sendMsg(WebSocketMessage(target = "urn:tlabs:geover:ws:info"))
        logger.info(result)
        //val id = info()?.substringAfter("""sender":""")?.substringBefore(""","secure""")
        return result
    }

    suspend fun all(warning: String) {
        val jsonObject = JSONObject(warning)

        val sensor = jsonObject.get("publisher ID").toString()
        val direction = jsonObject.get("windDirection").toString()
        val speed = jsonObject.get("windVelocity")

        val windData = WindData(
            direction = direction,
            directionType = "deg",
            speed = speed as Double,
            speedType = "kn"
        )
        val sensorData = SensorData(
            sensor = sensor,
            wind = windData
        )

        val received = sendMsg(
            WebSocketMessage(
                target = "urn:tlabs:geover:ws:receiver:all",
                data = sensorData
            )
        )
        logger.info(received)
    }

    suspend fun single(clientID: String, warning: String) {
        //{"target":"urn:tlabs:geover:ws:receiver:single","receiver":"e0","data":{"sensor":"hello"}}

        //{"publisher ID":"SimpleClient-11817164990583","wet":26.26850465951804,"timeSent":11819346317041,"temperature":49.34658103110901,"windDirection":"NW","priority":true,"windVelocity":125.8824536500926,"wind":20.822750151172073}
        //val msgToSend = Json.encodeToString(websocketMsg)

        val jsonObject = JSONObject(warning)

        val sensor = jsonObject.get("publisher ID").toString()
        val direction = jsonObject.get("windDirection").toString()
        val speed = jsonObject.get("windVelocity")

        val windData = WindData(
            direction = direction,
            directionType = "deg",
            speed = speed as Double,
            speedType = "kn"
        )
        val sensorData = SensorData(
            sensor = sensor,
            wind = windData
        )

        sendMsgNoReply(
            WebSocketMessage(
                target = "urn:tlabs:geover:ws:receiver:single",
                receiver = clientID,
                data = sensorData
            )
        )

        logger.info("Sent to Single Receiver: {}", clientID)
        logger.info("The Sent message is: {}", sensorData)
    }

    //TODO: self() and exceptSelf()

    private suspend fun sendMsg(websocketMsg: WebSocketMessage): String? {
        var receivedJson: String? = null
        runBlocking {
            websocketClient.webSocket(
                method = HttpMethod.Get,
                host = "master.tv.labs-exit.de",
                port = 80,
                path = "/geover-ws/api"
            ) {
                val msgToSend = Json.encodeToString(websocketMsg)
                send(Frame.Text(msgToSend))

                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        receivedJson = frame.readText()
                        logger.debug("Received JSON: $receivedJson")
                        return@webSocket
                    }
                }
            }
        }
        return receivedJson
    }


    private suspend fun sendMsgNoReply(websocketMsg: WebSocketMessage) {
        websocketClient.webSocket(
            method = HttpMethod.Get,
            host = "master.tv.labs-exit.de",
            port = 80,
            path = "/geover-ws/api"
        ) {
            val msgToSend = Json.encodeToString(websocketMsg)
            send(Frame.Text(msgToSend))
        }
    }


    suspend fun createWebSocketSession(): Pair<WebSocketSession, String> {
        var receivedJson = ""
        lateinit var webSocketSession: WebSocketSession

        try {
            websocketClient.webSocket(
                method = HttpMethod.Get,
                host = "master.tv.labs-exit.de",
                port = 80,
                path = "/geover-ws/api"
            ) {
                webSocketSession = this  // Store the WebSocketSession in the outer variable

                val websocketMsg = WebSocketMessage(target = "urn:tlabs:geover:ws:info")
                val msgToSend = Json.encodeToString(websocketMsg)

                send(Frame.Text(msgToSend))

                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        receivedJson = frame.readText()
                        logger.debug("WebSocket Info: $receivedJson")
                        println(receivedJson)
                        return@webSocket
                    }
                }
            }
        }catch (e: CancellationException) {
            // Handle cancellation, if needed
            logger.debug("WebSocket session creation was canceled.")
        }
        return Pair(webSocketSession, receivedJson)
    }


     suspend fun sendMsgWithSession(ws: WebSocketSession, msg: WebSocketMessage) {
        val msgToSend = Json.encodeToString(msg)
        ws.send(Frame.Text(msgToSend))

    }
}

//fun main() = runBlocking {
//    val websocketInstance = Websocket()
//    val websocket = websocketInstance.createWebSocketSession()
//    logger.info("WebSocket Session is: {}", websocket.first)
//
//    val info = websocket.second
//    logger.info("Session Info is: {}", info)
//
//    val ws = websocket.first
//
//    // Send message using the existing WebSocket session
//    val windData = Websocket.WindData(
//        direction = "direction",
//        directionType = "deg",
//        speed = 10.0,
//        speedType = "kn"
//    )
//    val sensorData = Websocket.SensorData(
//        sensor = "sensor",
//        wind = windData
//    )
//
//    val msg = Websocket.WebSocketMessage(
//        target = "urn:tlabs:geover:ws:receiver:all",
//        receiver = info.substringAfter("""sender":""").substringBefore(""","secure"""),
//        data = sensorData
//    )
//
//    websocketInstance.sendMsgWithSession(ws, msg)
//}