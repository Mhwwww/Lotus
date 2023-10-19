package de.hasenburg.geoverdemo.geoVER.kotlin.plugin

import io.ktor.serialization.gson.*
import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.websocket.*
import kotlinx.serialization.json.Json

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json()
        gson()
}
    install(WebSockets){
        contentConverter = KotlinxWebsocketSerializationConverter(Json)
//        pingPeriod = Duration.ofSeconds(15)
//        timeout = Duration.ofSeconds(15)
//        maxFrameSize = Long.MAX_VALUE
//        masking = false
    }
}
