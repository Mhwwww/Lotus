package de.hasenburg.geoverdemo.geoVER.kotlin.publisher

import de.hasenburg.geobroker.commons.randomDouble
import de.hasenburg.geobroker.commons.randomInt
import de.hasenburg.geoverdemo.geoVER.kotlin.TalkToXR
import org.json.JSONObject

val talkToXR = TalkToXR()


suspend fun sendFakeData(){
    val newElem = JSONObject().apply {
        put(WIND_VELOCITY, randomDouble(0.0, 64.0))
        put(WIND_DIRECTION, randomInt( 15))
        put(TEMPERATURE, randomDouble(0.0, 45.0))
        put(HUMIDITY, randomDouble(0.0, 60.0))
        put(TIME_SENT, System.nanoTime())
    }
    talkToXR.sendWarning(newElem.toString(),"fake_data")

}

