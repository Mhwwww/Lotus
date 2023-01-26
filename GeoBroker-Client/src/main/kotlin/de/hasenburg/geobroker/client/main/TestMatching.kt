package de.hasenburg.geobroker.client.main

import de.hasenburg.geobroker.commons.model.message.Topic
import org.apache.logging.log4j.LogManager

private val logger = LogManager.getLogger()


fun WildcardMatching(p:Topic, s:Topic):String{
   val r1 = s.topic.toRegex()

    if(r1.matches(p.topic)) {
        return p.topic
    }else
        return "not match"
}

fun main(){
    val s = Topic("/berlin/.*/drone/.*")// #/drone/#

    val p1 = Topic("/berlin/drone/1")
    val p2 = Topic("/berlin/ecdf/drone/2")
    val p3 = Topic("/berlin/drone/aa")
    val p4 = Topic("/berlin/c/drone/2/3")

    logger.error(WildcardMatching(p1,s))
    logger.error(WildcardMatching(p2,s))
    logger.error(WildcardMatching(p3,s))
    logger.error(WildcardMatching(p4,s))//false
}