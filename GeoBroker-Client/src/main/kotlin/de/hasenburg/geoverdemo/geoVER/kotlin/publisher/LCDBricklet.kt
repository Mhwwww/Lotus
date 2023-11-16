
import com.tinkerforge.BrickletLCD128x64
import com.tinkerforge.IPConnection
import de.hasenburg.geoverdemo.geoVER.kotlin.publisher.*
import org.apache.logging.log4j.LogManager
import java.awt.image.BufferedImage

private val logger = LogManager.getLogger()
private var i = 0
private val MSG_NUM = 5

@Throws(Exception::class)
fun drawImage(lcd: BrickletLCD128x64, image: BufferedImage) {
    val pixels = BooleanArray(HEIGHT * WIDTH)
    var h: Short
    var w: Short
    h = 0
    while (h < HEIGHT) {
        w = 0
        while (w < WIDTH) {
            pixels[h * WIDTH + w] = image.getRGB(w.toInt(), h.toInt()) and 0x00FFFFFF > 0
            w++
        }
        h++
    }
    lcd.writePixels(0, 0, WIDTH - 1, HEIGHT - 1, pixels)
}

fun lcdConnection(): Pair<BrickletLCD128x64,IPConnection>{
    // Create IP connection--> Create device object--> Connect to brick daemon
    val ipcon = IPConnection()
    val lcd = BrickletLCD128x64(UID_LCD, ipcon)

    ipcon.connect(TINKERFORGE_HOST, TINKERFORGE_PORT)
    return Pair(lcd,ipcon)
}

fun lcdDisconnect(ipcon:IPConnection){
    ipcon.disconnect();
}
fun sendTextToLCD(lcd:BrickletLCD128x64, msg: String) {
    //TODO: display the msg properly
    if(i < MSG_NUM){
        lcd.writeLine(i, 0, msg)
        logger.warn("LCD Message {}: {}", i, msg)
        i++
    }else{
        logger.warn("Going to Clear Screen")
        lcd.clearDisplay()

        i = 0
        lcd.writeLine(i, 0, msg)
        i++
    }




    //lcd.drawText(0, 12, BrickletLCD128x64.FONT_24X32, BrickletLCD128x64.COLOR_BLACK, "hello")
//todo: only keep latest 5 warning messages on the screen--> call clearDisplay() every 5 messages

}

//todo: show the received warning in LCD?-->!
fun main() {
    val lcdBrickletInfo = lcdConnection()
    val lcdBricklet = lcdBrickletInfo.first
    val lcdIpConfig = lcdBrickletInfo.second

    sendTextToLCD(lcdBricklet, "hello world")

    println("Press key to exit")
    System.`in`.read()


}



//val ipcon = IPConnection()
//val lcd = BrickletLCD128x64(UID_LCD, ipcon)
//
//ipcon.connect(TINKERFORGE_HOST, TINKERFORGE_PORT)
//
//// Clear display
//lcd.clearDisplay()
//
//val image = BufferedImage(WIDTH.toInt(), HEIGHT.toInt(), BufferedImage.TYPE_INT_ARGB)
//val originX = WIDTH / 2
//val originY = HEIGHT / 2
//val length = HEIGHT / 2 - 2
//var angle = 0
//
//println("Press ctrl+c to exit")
//
//while (true) {
//    val radians = Math.toRadians(angle.toDouble())
//    val x = (originX + length * Math.cos(radians)).toInt()
//    val y = (originY + length * Math.sin(radians)).toInt()
//    val g: Graphics = image.createGraphics()
//    g.color = Color.black
//    g.fillRect(0, 0, WIDTH.toInt(), HEIGHT.toInt())
//    g.color = Color.white
//    g.drawLine(originX, originY, x, y)
//    g.dispose()
//    drawImage(lcd, image)
//    Thread.sleep(25)
//    angle++
//}



//    lcd.addTouchPositionListener { pressure, x, y, age ->
//        println("Pressure: $pressure")
//        println("X: $x")
//        println("Y: $y")
//        println("Age: $age")
//        println("")
//    }
//
// Add touch gesture listener
//    lcd.addTouchGestureListener { gesture, duration, pressureMax, xStart, xEnd, yStart, yEnd, age ->
//        if (gesture == BrickletLCD128x64.GESTURE_LEFT_TO_RIGHT) {
//            println("Gesture: Left To Right")
//        } else if (gesture == BrickletLCD128x64.GESTURE_RIGHT_TO_LEFT) {
//            println("Gesture: Right To Left")
//        } else if (gesture == BrickletLCD128x64.GESTURE_TOP_TO_BOTTOM) {
//            println("Gesture: Top To Bottom")
//        } else if (gesture == BrickletLCD128x64.GESTURE_BOTTOM_TO_TOP) {
//            println("Gesture: Bottom To Top")
//        }
//        println("Duration: $duration")
//        println("Pressure Max: $pressureMax")
//        println("X Start: $xStart")
//        println("X End: $xEnd")
//        println("Y Start: $yStart")
//        println("Y End: $yEnd")
//        println("Age: $age")
//        println("")
//    }
//
//    // Set period for touch position callback to 0.1s (100ms)
//    lcd.setTouchPositionCallbackConfiguration(100, true)
//    // Set period for touch gesture callback to 0.1s (100ms)
//    lcd.setTouchGestureCallbackConfiguration(100, true)



//    lcd.addGUIButtonPressedListener { index, pressed ->
//        println("Index: $index")
//        println("Pressed: $pressed")
//        println("")
//    }
//
//    // Add GUI slider value listener
//
//    // Add GUI slider value listener
//    lcd.addGUISliderValueListener { index, value ->
//        println("Index: $index")
//        println("Value: $value")
//        println("")
//    }
//
//    // Add GUI tab selected listener
//
//    // Add GUI tab selected listener
//    lcd.addGUITabSelectedListener { index -> println("Index: $index") }
//
//    // Clear display
//
//    // Clear display
//    lcd.clearDisplay()
//    lcd.removeAllGUI()
//
//    // Add GUI elements: Button, Slider and Graph with 60 data points
//
//    // Add GUI elements: Button, Slider and Graph with 60 data points
//    lcd.setGUIButton(0, 0, 0, 60, 20, "button")
//    lcd.setGUISlider(0, 0, 30, 60, DIRECTION_HORIZONTAL, 50)
//    lcd.setGUIGraphConfiguration(
//        0, GRAPH_TYPE_LINE, 62, 0, 60, 52,
//        "X", "Y"
//    )
//
//    // Add a few data points (the remaining points will be 0)
//
//    // Add a few data points (the remaining points will be 0)
//    lcd.setGUIGraphData(0, intArrayOf(20, 40, 60, 80, 100, 120, 140, 160, 180, 200, 220, 240))
//
//    // Add 5 text tabs without and configure it for click and swipe without auto-redraw
//
//    // Add 5 text tabs without and configure it for click and swipe without auto-redraw
//    lcd.setGUITabConfiguration(
//        CHANGE_TAB_ON_CLICK_AND_SWIPE,
//        false
//    )
//    lcd.setGUITabText(0, "Tab A")
//    lcd.setGUITabText(1, "Tab B")
//    lcd.setGUITabText(2, "Tab C")
//    lcd.setGUITabText(3, "Tab D")
//    lcd.setGUITabText(4, "Tab E")
//
//    // Set period for GUI button pressed callback to 0.1s (100ms)
//    lcd.setGUIButtonPressedCallbackConfiguration(100, true)
//
//    // Set period for GUI slider value callback to 0.1s (100ms)
//    lcd.setGUISliderValueCallbackConfiguration(100, true)
//
//    // Set period for GUI tab selected callback to 0.1s (100ms)
//    lcd.setGUITabSelectedCallbackConfiguration(100, true)