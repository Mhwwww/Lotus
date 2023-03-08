package de.hasenburg.geoverdemo.transform.common

import de.hasenburg.geobroker.commons.model.message.Topic
import de.hasenburg.geobroker.commons.model.spatial.Location
import kotlin.random.Random

const val numberOfClients = 50
const val numberOfRepeats = 100
private val rand = Random(1)
val locations = List(numberOfClients) { Location.safeRandom(rand) }
val publishTopic = Topic("raw")
val subscriberTopic = Topic("processed")