
import java.io.IOException
import java.util.*


fun cmdUploadTotinyFaaS(path:String, functionName:String, thread: Int) {
    val os = System.getProperty("os.name").lowercase(Locale.getDefault())
    println(os)
    if (os.lowercase(Locale.getDefault()).contains("mac")) {
        val upload: String = "/Users/minghe/tinyFaaS/scripts/upload.sh\t" + path + "\t" + functionName + "\t" + thread
        runOnMac(upload)

    } else if (os.contains("windows")) {
        runOnWindows("dir")
        runOnWindows("git log")
    }
}

fun cmdDeleteFromtinyFaaS(functionName:String) {
    val os = System.getProperty("os.name").lowercase(Locale.getDefault())
    println(os)
    if (os.lowercase(Locale.getDefault()).contains("mac")) {
        val delete = "/Users/minghe/tinyFaaS/scripts/delete.sh "+ functionName
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


