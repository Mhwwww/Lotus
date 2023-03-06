package de.hasenburg.geoverdemo.cbf.common

import de.hasenburg.geobroker.commons.model.message.Topic
import de.hasenburg.geobroker.commons.model.spatial.Location
import kotlin.random.Random

const val numberOfClients = 50
const val numberOfRepeats = 100
val locations = List(numberOfClients) { Location.random(Random(1)) }
val publishTopic = Topic("unfiltered")
val matchingTopic = Topic("filtered")