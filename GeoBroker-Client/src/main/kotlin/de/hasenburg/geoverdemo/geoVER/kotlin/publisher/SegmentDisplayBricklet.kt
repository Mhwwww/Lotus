package de.hasenburg.geoverdemo.geoVER.kotlin.publisher

import com.tinkerforge.BrickletSegmentDisplay4x7V2
import com.tinkerforge.IPConnection
import org.apache.logging.log4j.LogManager

class SegmentDisplayBricklet {
}

private val logger = LogManager.getLogger()


var charToSegment: HashMap<Char, BooleanArray> = hashMapOf(
    '0' to booleanArrayOf(true, true, true, true, true, true, false),
    '1' to booleanArrayOf(false, true, true, false, false, false, false),
    '2' to booleanArrayOf(true, true, false, true, true, false, true),
    '3' to booleanArrayOf(true, true, true, true, false, false, true),
    '4' to booleanArrayOf(false, true, true, false, false, true, true),
    '5' to booleanArrayOf(true, false, true, true, false, true, true),
    '6' to booleanArrayOf(true, false, true, true, true, true, true),
    '7' to booleanArrayOf(true, true, true, false, false, false, false),
    '8' to booleanArrayOf(true, true, true, true, true, true, true),
    '9' to booleanArrayOf(true, true, true, true, false, true, true),
    'n' to booleanArrayOf(false, false, false, false, false, false, false, false),// all dark
)

fun charToSegmenet(dig:Char, dot: Boolean): BooleanArray? {
    val origin = charToSegment[dig]

    return if (origin != null) {
        val size = origin.size + 1
        val result = BooleanArray(size)

        System.arraycopy(origin,0, result,0, size-1)
        result[size-1] = dot

        logger.debug("the new array size is: {}", result.count())

        result
    }else{
        logger.error("fail to set the dot")
        origin
    }

}


fun segmentConnection(): Pair<BrickletSegmentDisplay4x7V2, IPConnection> {
    val ipcon = IPConnection()
    val segmentDisplay = BrickletSegmentDisplay4x7V2(UID_SEGMENT, ipcon)

    ipcon.connect(TINKERFORGE_HOST, TINKERFORGE_PORT)

    println("Connected")
    return Pair(segmentDisplay, ipcon)
}


fun segDisplayMsg(sd: BrickletSegmentDisplay4x7V2, windSpeed: Double) {
    sd.brightness = 1 // Set to full brightnes--7
    val charList = transDoubleToSegments(windSpeed)

    if (charList.contains('.')){
        val dotPos = charList.indexOf('.')

        when(dotPos){
            1->{//windspeed is in sigle digital
                if (charList.size >= 4){
                    val dig0 = charToSegment['n']
                    val dig1 = charToSegmenet(charList[0] , true)
                    val dig2 = charToSegmenet(charList[2], false)
                    val dig3 = charToSegmenet(charList[3], false)

                    sd.setSegments(dig0, dig1, dig2, dig3, booleanArrayOf(false, false),false)
                }else{//1.2--> 1.20
                    val dig0 = charToSegment['n']
                    val dig1 = charToSegmenet(charList[0] , true)
                    val dig2 = charToSegmenet(charList[2], false)
                    val dig3 = charToSegmenet('0', false)

                    sd.setSegments(dig0, dig1, dig2, dig3, booleanArrayOf(false, false),false)

                }

            }
            2->{
                if (charList.size >=5){//12.34
                    val dig0 = charToSegmenet(charList[0],false)
                    val dig1 = charToSegmenet(charList[1],true)
                    val dig2 = charToSegmenet(charList[3],false)
                    val dig3 = charToSegmenet(charList[4], false)

                    sd.setSegments(dig0, dig1, dig2, dig3, booleanArrayOf(false, false),false)

                }else{//21.2->21.20

                    val dig0 = charToSegmenet(charList[0],false)
                    val dig1 = charToSegmenet(charList[1],true)
                    val dig2 = charToSegmenet(charList[3],false)
                    val dig3 = charToSegmenet('0', false)

                    sd.setSegments(dig0, dig1, dig2, dig3, booleanArrayOf(false, false),false)
                }

            }

        }
}
}

fun transDoubleToSegments(double: Double): List<Char> {
    val numberString = double.toString()
    val charList = mutableListOf<Char>()

    for (char in numberString) {
        if (char.isDigit() || char == '.') {
            charList.add(char)
        }
    }

    println(charList)
    return charList
}

fun main() {
    val segmentDisplay = segmentConnection()
    val segmentBricklet = segmentDisplay.first
    val segmentIPConnection = segmentDisplay.second

    segDisplayMsg(segmentBricklet, 56.79)

    segmentIPConnection.disconnect()
}

