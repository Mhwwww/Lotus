package de.hasenburg.geover
import de.hasenburg.geoverdemo.geoVER.kotlin.TINYFASS_PATH
import org.apache.logging.log4j.LogManager
import java.io.File
import java.io.IOException

// tinyFaaS is a submodule to the main repo
// seven levels up from this should do
var TINYFAAS_BASE_PATH = TINYFASS_PATH

var TINYFAAS_UPLOAD_PATH = "${TINYFAAS_BASE_PATH}scripts/upload.sh\t"
var TINYFAAS_DELETE_PATH = "${TINYFAAS_BASE_PATH}scripts/delete.sh\t"
var TINYFAAS_LIST_PATH = "${TINYFAAS_BASE_PATH}scripts/list.sh\t"
var TINYFAAS_LOGS_PATH = "${TINYFAAS_BASE_PATH}scripts/logs.sh\t"

var TINYFAAS_START_PATH = "${TINYFAAS_BASE_PATH}\t"

private val logger = LogManager.getLogger()
fun cmdUploadToTinyFaaS(path:String, functionName:String, fnEnv:String, thread: Int) {

    logger.debug("isUnix: {}", isUnix())
    if (isUnix()) {
        val upload: String = TINYFAAS_UPLOAD_PATH + path + "\t" + functionName + "\t" + fnEnv+ "\t" + thread
        runOnUnix(upload)
//        runOnUnixWithDirectory(upload,"/Users/minghe/geobroker")

    } else {
        //TODO: cmd on windows and linux
        runOnWindows("dir")
        runOnWindows("git log")
    }
}

fun cmdDeleteFromTinyFaaS(functionName:String) {
    if (isUnix()) {
        val delete = TINYFAAS_DELETE_PATH + functionName
        runOnUnix(delete)

    } else {
        runOnWindows("dir")
        runOnWindows("git log")
    }
}
fun cmdGetAllFunctionsLogsFromTinyFaaS(): String {
    return if (isUnix()) {
        val logs = TINYFAAS_LOGS_PATH
        runOnUnix(logs)

    } else {
        runOnWindows("dir")
        runOnWindows("git log")
    }
}

fun cmdGetFuncList(): List<String> {
    return if (isUnix()) {
        val getList = TINYFAAS_LIST_PATH
        listOf(runOnUnix(getList))

    } else {

        val getList = TINYFAAS_LIST_PATH
        listOf(runOnUnix(getList))
    }
}

fun isUnix(): Boolean {
    return System.getProperty("os.name").lowercase().matches("mac os x|linux".toRegex())
}

fun runOnWindows(cmd: String): String {
    val runtime = Runtime.getRuntime()
    val realCmd = "cmd /c $cmd"
    return try {
        val p = runtime.exec(realCmd)
        val result: String = p.inputStream.bufferedReader().readText()

        val output = String.format("execute cmd : %s and result is \n\n%s ", cmd, result)
        logger.debug(output)
        output
    } catch (e: IOException) {
        e.printStackTrace()
        ""
    }
}

fun runOnUnix(cmd: String):String {
    val runtime = Runtime.getRuntime()
    return try {
        logger.info("running cmd: {}", cmd)

        val p = runtime.exec(cmd)
        val result: String = p.inputStream.bufferedReader().readText()

        val output = String.format("execute cmd : \" %s \" and result is \n\n%s ", cmd, result)
        logger.info(output)

        if (p.exitValue() != 0) {
            logger.error("Command was not successful! error code {} with err stream {}", p.exitValue(), p.errorStream.bufferedReader().readText())
        }

        result
    } catch (e: IOException) {

        e.printStackTrace()
        e.printStackTrace().toString()
    }
}




fun main(){
    //de.hasenburg.geover.cmdGetFuncList()
    //de.hasenburg.geover.cmdGetAllFunctionsLogsFromTinyFaaS()
    startTinyFaaS()
}

fun startTinyFaaS() {
    logger.debug("isUnix: {}", isUnix())
    if (isUnix()) {
        val basePath = TINYFAAS_BASE_PATH
        val make = "make"
        runOnUnixWithDirectory(make, basePath)
    } else {
        //TODO: cmd on Windows and Linux
        runOnWindows("dir")
        runOnWindows("git log")
    }
}

fun runOnUnixWithDirectory(cmd: String, workingDirectory: String): String {
    val runtime = Runtime.getRuntime()
    return try {
        val processBuilder = ProcessBuilder("sh", "-c", cmd)
        processBuilder.directory(File(workingDirectory))

        logger.info("running cmd: {}", cmd)
        val process = processBuilder.start()
        val result: String = process.inputStream.bufferedReader().readText()

        val output = String.format("execute cmd : \" %s \" and result is \n\n%s ", cmd, result)
        logger.info(output)

        if (process.waitFor() != 0) {
            val errorResult: String = process.errorStream.bufferedReader().readText()
            logger.error("Command was not successful! error code {} with err stream {}", process.exitValue(), errorResult)
        }

        result
    } catch (e: IOException) {
        e.printStackTrace()
        e.printStackTrace().toString()
    }
}
