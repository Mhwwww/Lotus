package de.hasenburg.geoverdemo.transform.common

import de.hasenburg.geobroker.commons.model.message.Topic
import de.hasenburg.geobroker.commons.model.spatial.Location
import kotlin.random.Random

const val numberOfClients = 5
const val numberOfRepeats = 2
private val rand = Random(1)
val locations = List(de.hasenburg.geoverdemo.cbf.common.numberOfClients) { Location.random(rand) }
//val locations = List(numberOfClients) { Location.random(Random(1)) }
val publishTopic = Topic("raw")
val subscriberTopic = Topic("processed")