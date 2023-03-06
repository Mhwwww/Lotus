package de.hasenburg.geoverdemo.condensing.common

import de.hasenburg.geobroker.commons.model.message.Topic
import de.hasenburg.geobroker.commons.model.spatial.Location
import kotlin.random.Random

const val numberOfLocations = 5
const val numberOfRepeats = 2
const val numberOfUnnecessaryJson = 100
const val clientsPerLocation = 2
val locations = List(numberOfLocations) { Location.random(Random(1)) }
val publishTopic = Topic("raw")
val subscriberTopic = Topic("processed")