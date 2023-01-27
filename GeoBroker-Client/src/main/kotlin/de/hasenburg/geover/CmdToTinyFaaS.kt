
import java.io.IOException
import java.util.*

val TINYFAAS_UPLOAD_PATH = "/Users/minghe/tinyFaaS/scripts/upload.sh\t"
val TINYFAAS_DELETE_PATH = "/Users/minghe/tinyFaaS/scripts/delete.sh\t"


fun cmdUploadTotinyFaaS(path:String, functionName:String, thread: Int) {
    val os = System.getProperty("os.name").lowercase(Locale.getDefault())
    println(os)
    if (os.lowercase(Locale.getDefault()).contains("mac")) {
        val upload: String = TINYFAAS_UPLOAD_PATH + path + "\t" + functionName + "\t" + thread
        runOnMac(upload)

    } else if (os.contains("windows")) {
        //TODO: cmd on windows and linux
        runOnWindows("dir")
        runOnWindows("git log")
    }
}

fun cmdDeleteFromtinyFaaS(functionName:String) {
    val os = System.getProperty("os.name").lowercase(Locale.getDefault())
    println(os)
    if (os.lowercase(Locale.getDefault()).contains("mac")) {
        val delete = TINYFAAS_DELETE_PATH+ functionName
        runOnMac(delete)

    } else if (os.contains("windows")) {
        runOnWindows("dir")
        runOnWindows("git log")
    }
}

fun runOnWindows(cmd: String) {
    val runtime = Runtime.getRuntime()
    val realCmd = "cmd /c $cmd"
    try {
        val p = runtime.exec(realCmd)
        val result: String = p.inputStream.bufferedReader().readText()

        val output = String.format("execute cmd : %s and result is \n\n%s ", cmd, result)
        println(output)
    } catch (e: IOException) {
        e.printStackTrace()
    }
}

fun runOnMac(cmd: String) {
    val runtime = Runtime.getRuntime()
    try {
        val p = runtime.exec(cmd)
        val result: String = p.inputStream.bufferedReader().readText()

        val output = String.format("execute cmd : \" %s \" and result is \n\n%s ", cmd, result)
        println(output)
    } catch (e: IOException) {
        e.printStackTrace()
    }
}


