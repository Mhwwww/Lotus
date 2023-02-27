
import org.apache.logging.log4j.LogManager
import java.io.IOException
import java.util.*

val TINYFAAS_UPLOAD_PATH = "/Users/minghe/tinyFaaS/scripts/upload.sh\t"
val TINYFAAS_DELETE_PATH = "/Users/minghe/tinyFaaS/scripts/delete.sh\t"
val TINYFAAS_LIST_PATH = "/Users/minghe/tinyFaaS/scripts/list.sh\t"
val TINYFAAS_LOGS_PATH = "/Users/minghe/tinyFaaS/scripts/logs.sh\t"


private val logger = LogManager.getLogger()
fun cmdUploadTotinyFaaS(path:String, functionName:String, fnEnv:String, thread: Int) {
    val os = System.getProperty("os.name").lowercase(Locale.getDefault())
    println(os)
    if (os.lowercase(Locale.getDefault()).contains("mac")) {
        val upload: String = TINYFAAS_UPLOAD_PATH + path + "\t" + functionName + "\t" + fnEnv+ "\t" + thread
        runOnMac(upload)

    } else if (os.contains("windows")) {
        //TODO: cmd on windows and linux
        runOnWindows("dir")
        runOnWindows("git log")
    }
}

fun cmdDeleteFromtinyFaaS(functionName:String) {
    val os = System.getProperty("os.name").lowercase(Locale.getDefault())
    logger.debug("Operating System is {}", os)
    if (os.lowercase(Locale.getDefault()).contains("mac")) {
        val delete = TINYFAAS_DELETE_PATH+ functionName
        runOnMac(delete)

    } else if (os.contains("windows")) {
        runOnWindows("dir")
        runOnWindows("git log")
    }
}
fun cmdGetAllFunctionsLogsFromTinyFaaS(): List<String>? {
    val os = System.getProperty("os.name").lowercase(Locale.getDefault())
    logger.debug("Operating System is {}", os)
    if (os.lowercase(Locale.getDefault()).contains("mac")) {
        val logs = TINYFAAS_LOGS_PATH
        runOnMac(logs)

    } else if (os.contains("windows")) {
        runOnWindows("dir")
        runOnWindows("git log")
    }
    return null
}

fun cmdGetFuncList(): List<String>? {
    val os = System.getProperty("os.name").lowercase(Locale.getDefault())
    logger.debug("Operating System is {}", os)
    if (os.lowercase(Locale.getDefault()).contains("mac")) {
        val getList = TINYFAAS_LIST_PATH
        return listOf(runOnMac(getList))

    } else if (os.contains("windows")) {

        val getList = TINYFAAS_LIST_PATH
        return listOf(runOnMac(getList))
    }
    return null
}

fun runOnWindows(cmd: String) {
    val runtime = Runtime.getRuntime()
    val realCmd = "cmd /c $cmd"
    try {
        val p = runtime.exec(realCmd)
        val result: String = p.inputStream.bufferedReader().readText()

        val output = String.format("execute cmd : %s and result is \n\n%s ", cmd, result)
        logger.debug(output)
    } catch (e: IOException) {
        e.printStackTrace()
    }
}

fun runOnMac(cmd: String):String {
    val runtime = Runtime.getRuntime()
    try {
        val p = runtime.exec(cmd)
        val result: String = p.inputStream.bufferedReader().readText()

        val output = String.format("execute cmd : \" %s \" and result is \n\n%s ", cmd, result)
        logger.debug(output)

        return result
    } catch (e: IOException) {

        return  e.printStackTrace().toString()
    }
}


fun main(){
    //cmdGetFuncList()
    //cmdGetAllFunctionsLogsFromTinyFaaS()
}