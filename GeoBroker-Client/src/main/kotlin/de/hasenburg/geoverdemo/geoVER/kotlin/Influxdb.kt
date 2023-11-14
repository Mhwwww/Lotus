package de.hasenburg.geoverdemo.geoVER.kotlin

import com.influxdb.annotations.Column
import com.influxdb.annotations.Measurement
import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.kotlin.InfluxDBClientKotlinFactory
import de.hasenburg.geobroker.commons.model.message.Payload
import de.hasenburg.geoverdemo.geoVER.kotlin.publisher.TEMPERATURE
import de.hasenburg.geoverdemo.geoVER.kotlin.publisher.WIND_DIRECTION
import de.hasenburg.geoverdemo.geoVER.kotlin.publisher.WIND_VELOCITY
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import java.time.Instant


@Measurement(name = "temperature")
data class Temperature(
//    @Column(tag = true) val location: String,
    @Column val value: Double,
    @Column(timestamp = true) val time: Instant
)

@Measurement(name = "wind")
data class Wind(
    //    @Column val location: String,
    @Column val windDirection: Int,
    @Column val value: Double,//wind speed
    @Column(timestamp = true) val time: Instant,

)

class InfluxDB{
    fun writeToInfluxDB(msg: String, bucket: String) {
        runBlocking {
            //publisher data
            val jsonObject = JSONObject(msg)
//            {
        //            "topic":"warning",
        //            "location":"Frankfurt Airport",
        //            "message":{
        //                  "Time Sent":9205281982041,
        //                  "Temperature":24.451258330866057,
        //                  "Wind Velocity":60.1580425198826,
        //                  "Priority":true,
        //                  "Humidity":59.22916413142805,
        //                  "Wind Direction":14
        //                  }
        //     }
//            val location = jsonObject.get("location").toString()

            val message = JSONObject(jsonObject.get("message").toString())

            var windSpeed = message.get("Wind Velocity") as Double
            windSpeed = String.format("%.2f", windSpeed).toDouble()
            val windDirection = message.get("Wind Direction") as Int
            val temperature = message.get("Temperature") as Double


            val client = InfluxDBClientKotlinFactory.create( url = URL, token = TOKEN, org = ORGANIZATION, bucket = bucket)

            client.use {
                val writeApi = client.getWriteKotlinApi()
                val tempe = Temperature(temperature, Instant.now())// Write by DataClass

                writeApi.writeMeasurement(tempe, WritePrecision.MS)//nano-second

                val wind = Wind(windDirection , windSpeed, Instant.now())// Write by DataClass
                writeApi.writeMeasurement(wind, WritePrecision.MS)//nano-second

                /* Query results */
                //TODO: leave here for testing, delete later.
                val fluxQuery_wind =
                    """
                    from(bucket: "$INFO_BUCKET")
                        ||> range(start: -5m)
                        |> filter(fn: (r) => (r["_measurement"] == "wind"))
                        |
                        """.trimMargin()
                val fluxQuery_temperature=
                    """from(bucket: "$INFO_BUCKET")
                        ||> range(start: -5m)
                        |> filter(fn: (r) => (r["_measurement"] == "temperature"))""".trimMargin()

                val fluxQuery_all=
                    """from(bucket: "$INFO_BUCKET")
                        ||> range(start: -5m)
                        """.trimMargin()


                //Result is returned as a stream



//                client
//                    .getQueryKotlinApi()
//                    .query(fluxQuery_all)
//                    .consumeAsFlow()
//                    .collect { println("Measurement: ${it.table}") }
//                client
//                    .getQueryKotlinApi()
//                    .query(fluxQuery_wind)
//                    .consumeAsFlow()
//                    .collect { println("Measurement: ${it.measurement}, value: ${it.value}, wind direction: ${it.getValueByKey("windDirection")}, time: ${it.time}") }
//                /* Query results */
//
//                client
//                    .getQueryKotlinApi()
//                    .query(fluxQuery_temperature)
//                    .consumeAsFlow()
//                    .collect { println("Measurement: ${it.measurement}, value: ${it.value},  time: ${it.time}") }

                //client.close()
            }
        }
    }
    fun queryFromInfluxDBwithKey(fluxQuery: String, key:String, bucket: String) {
        runBlocking {
            val client = InfluxDBClientKotlinFactory.create( url = URL, token = TOKEN, org = ORGANIZATION)
            client
                .getQueryKotlinApi()
                .query(fluxQuery)
                .consumeAsFlow()
                .collect { println("Measurement: ${it.measurement}, value: ${it.value},  time: ${it.time}, ${it.values.getValue(key)}") }


            client.close()
        }
    }
    fun queryFromInfluxDBwithoutKey(fluxQuery: String, bucket: String) {
        runBlocking {
            val client = InfluxDBClientKotlinFactory.create( url = URL, token = TOKEN, org = ORGANIZATION)

            client
                .getQueryKotlinApi()
                .query(fluxQuery)
                .consumeAsFlow()
                .collect { println("Measurement: ${it.measurement}, value: ${it.value}, ${it.field}") }

            client.close()
        }
    }
    fun writeMsgToInfluxDB(msg: Payload.PUBLISHPayload, bucket: String) {
        runBlocking {
            //publisher data
            val location = Pair(msg.geofence.center.lat, msg.geofence.center.lon)
            val messageContent = msg.content

            val jsonObject = JSONObject(messageContent)

            val direction = jsonObject.get(WIND_DIRECTION) as Int

            var speed = (jsonObject.get(WIND_VELOCITY)).toString().toDouble()
            speed = String.format("%.2f", speed).toDouble()

            val temp = (jsonObject.get(TEMPERATURE)).toString().toDouble()








//            val humidity = jsonObject.get("Humidity").toString()
//            val timeSent = jsonObject.get("timeSent").toString()
//            val publisherID = jsonObject.get("Publisher ID").toString()

            // Initialize client
            val client = InfluxDBClientKotlinFactory.create( url = URL, token = TOKEN, org = ORGANIZATION, bucket = bucket)

            client.use {
                val writeApi = client.getWriteKotlinApi()
                val temperature = Temperature(temp, Instant.now())// Write by DataClass
                writeApi.writeMeasurement(temperature, WritePrecision.NS)//nano-second

                val wind = Wind(direction, speed, Instant.now())// Write by DataClass
                writeApi.writeMeasurement(wind, WritePrecision.NS)//nano-second

//                /* Query results */
//                val fluxQuery_wind =
//                    """from(bucket: "$INFO_BUCKET") |> range(start: 0) |> filter(fn: (r) => (r["_measurement"] == "wind"))"""
//                val fluxQuery_temperature=
//                    """from(bucket: "$INFO_BUCKET") |> range(start: 0) |> filter(fn: (r) => (r["_measurement"] == "temperature"))"""
////
//                client
//                    .getQueryKotlinApi()
//                    .query(fluxQuery_wind)
//                    .consumeAsFlow()
//                    .collect { println("Measurement: ${it.measurement}, value: ${it.value}, wind direction: ${it.getValueByKey("windDirection")}, time: ${it.time}") }
////                /* Query results */
//
//                client
//                    .getQueryKotlinApi()
//                    .query(fluxQuery_temperature)
//                    .consumeAsFlow()
//                    .collect { println("Measurement: ${it.measurement}, value: ${it.value},  time: ${it.time}") }
////
//                //client.close()
            }
        }
    }


//}


}

fun main(){
    val testBucket = INFO_BUCKET

    val fluxQuery_wind=
        """from(bucket: "$testBucket") |> range(start: 0) |> filter(fn: (r) => (r["_measurement"] == "wind" and r["_field"] == "windDirection"))"""


    val fluxQuery_temperature=
        """from(bucket: "$testBucket") |> range(start: 0) |> filter(fn: (r) => (r["_measurement"] == "temperature"))"""

    val query = """from(bucket: "info")
     |> range(start: 0)
     |> filter(fn: (r) => (r["_measurement"] == "wind"))
     
"""


    val influxdb = InfluxDB()
//    influxdb.queryFromInfluxDBwithKey(fluxQuery_wind, "Wind Direction", testBucket)
    influxdb.queryFromInfluxDBwithoutKey(fluxQuery_wind, testBucket)
//    influxdb.queryFromInfluxDBwithoutKey(query, testBucket)

}