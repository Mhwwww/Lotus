package de.hasenburg.geoverdemo.crossWind.common

import de.hasenburg.geobroker.commons.model.message.Topic
import de.hasenburg.geobroker.commons.model.spatial.Location

const val numberOfClients = 1
const val numberOfRepeats = 20
//private val rand = Random(1)
//val locations = List(numberOfClients) { Location.safeRandom(rand) }

val locations = List(numberOfClients) { Location(0.0,0.0) }

val publishTopic = Topic("info")
val matchingTopic = Topic("weather")