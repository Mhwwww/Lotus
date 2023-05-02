package de.hasenburg.geoverdemo.multiRule.common

import de.hasenburg.geobroker.commons.model.message.Topic
import de.hasenburg.geobroker.commons.model.spatial.Location
import kotlin.random.Random

const val numberOfClients = 5
const val numberOfRepeats = 10
private val rand = Random(1)
val locations = List(numberOfClients) { Location.safeRandom(rand) }
val publishTopic = Topic("info")
val matchingTopic = Topic("warnings")