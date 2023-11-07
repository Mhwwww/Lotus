package de.hasenburg.geoverdemo.geoVER.kotlin

import com.influxdb.annotations.Column
import com.influxdb.annotations.Measurement
import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.kotlin.InfluxDBClientKotlinFactory
import de.hasenburg.geobroker.commons.model.message.Payload
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
//    @Column(tag = true) val location: String,
    @Column val windDirection: Int,
    @Column val value: Double,//wind speed
    @Column(timestamp = true) val time: Instant,
//    @Column val location: Pair <Double, Double>
)

class InfluxDB{
    //args: msg to store & the bucket
//    fun writeToInfluxDB(msg: String, bucket: String) {
    fun writeToInfluxDB(msg: String, bucket: String) {
        runBlocking {
            //publisher data
            val jsonObject = JSONObject(msg)

            val direction = jsonObject.get("windDirection") as Int
            var speed = jsonObject.get("windVelocity") as Double
            speed = String.format("%.2f", speed).toDouble()
            val temp = jsonObject.get("temperature") as Double
            val humidity = jsonObject.get("humidity").toString()
            val timeSent = jsonObject.get("timeSent").toString()
            val publisherID = jsonObject.get("publisher ID").toString()

            // Initialize client
            val client = InfluxDBClientKotlinFactory.create( url = URL, token = TOKEN, org = ORGANIZATION, bucket = bucket)

            client.use {
                val writeApi = client.getWriteKotlinApi()
                val temperature = Temperature(temp, Instant.now())// Write by DataClass
                writeApi.writeMeasurement(temperature, WritePrecision.NS)//nano-second

                val wind = Wind(direction, speed, Instant.now())// Write by DataClass
                writeApi.writeMeasurement(wind, WritePrecision.NS)//nano-second

//                /* Query results */
//                //TODO: leave here for testing, delete later.
                val fluxQuery_wind =
                    """from(bucket: "$INFO_BUCKET") |> range(start: 0) |> filter(fn: (r) => (r["_measurement"] == "wind"))"""
                val fluxQuery_temperature=
                    """from(bucket: "$INFO_BUCKET") |> range(start: 0) |> filter(fn: (r) => (r["_measurement"] == "temperature"))"""

                val fluxQuery_delete =
                    """"""
//
                client
                    .getQueryKotlinApi()
                    .query(fluxQuery_wind)
                    .consumeAsFlow()
                    .collect { println("Measurement: ${it.measurement}, value: ${it.value}, wind direction: ${it.getValueByKey("windDirection")}, time: ${it.time}") }
//                /* Query results */

                client
                    .getQueryKotlinApi()
                    .query(fluxQuery_temperature)
                    .consumeAsFlow()
                    .collect { println("Measurement: ${it.measurement}, value: ${it.value},  time: ${it.time}") }
//
//                //client.close()
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
                .collect { println("Measurement: ${it.measurement}, value: ${it.value},  time: ${it.time}") }

            client.close()
        }
    }
    fun writeMsgToInfluxDB(msg: Payload.PUBLISHPayload, bucket: String) {
        runBlocking {
            //publisher data
            val location = Pair(msg.geofence.center.lat, msg.geofence.center.lon)
            val messageContent = msg.content

            val jsonObject = JSONObject(messageContent)

            val direction = jsonObject.get("windDirection") as Int
            var speed = jsonObject.get("windVelocity") as Double
            speed = String.format("%.2f", speed).toDouble()
            val temp = jsonObject.get("temperature") as Double
            val humidity = jsonObject.get("humidity").toString()
            val timeSent = jsonObject.get("timeSent").toString()
            val publisherID = jsonObject.get("publisher ID").toString()

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


}

fun main(){
    val testBucket = INFO_BUCKET

    val fluxQuery_wind =
                    """from(bucket: "$testBucket") |> range(start: 0) |> filter(fn: (r) => (r["_measurement"] == "wind"))"""
    val fluxQuery_temperature=
                    """from(bucket: "$testBucket") |> range(start: 0) |> filter(fn: (r) => (r["_measurement"] == "temperature"))"""

    val query = """from(bucket: "info")
     |> range(start: 0)
     |> filter(fn: (r) => (r["_measurement"] == "wind"))
     |> DURATION(1s)
"""


    val influxdb = InfluxDB()
    influxdb.queryFromInfluxDBwithKey(fluxQuery_wind, "location", testBucket)
//    influxdb.queryFromInfluxDBwithoutKey(fluxQuery_temperature, testBucket)
//    influxdb.queryFromInfluxDBwithoutKey(query, testBucket)


}


//TODO: clear the table content && delete the table
fun clearBucket() {

}

fun deleteBucket() {

}
