package cn.bincker.android_ddns.utils

import android.util.Log
import cn.bincker.android_ddns.App
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.*

class SimpleLogger private constructor() {
    companion object {
        private lateinit var _instance: SimpleLogger
        fun getInstance(): SimpleLogger {
            if (!::_instance.isInitialized) {
                synchronized(this) {
                    if (!::_instance.isInitialized) {
                        _instance = SimpleLogger()
                    }
                }
            }
            return _instance
        }
        private const val MAX_LOG_LENGTH = 1000
        private const val LOG_FILE_NAME = "log.txt"
        private const val SAVE_INTERVAL = 10 * 60 * 1000L
    }

    private val cache = LinkedList<String>()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
    private val timer: Timer = Timer()
    private val saveTask = object : TimerTask() {
        override fun run() {
            val logFile = File(App.getContext().filesDir, LOG_FILE_NAME)
            try {
                OutputStreamWriter(FileOutputStream(logFile)).use { writer->
                    for (line in cache) {
                        writer.write(line)
                        writer.write("\n")
                    }
                }
            }catch (e:Exception){
                Log.e("SimpleLogger", "save log file failed.", e)
            }
        }
    }

    init {
        val logFile = File(App.getContext().filesDir, LOG_FILE_NAME)
        if (logFile.exists()) {
            cache.addAll(logFile.readLines())
            while (cache.size > MAX_LOG_LENGTH) cache.removeFirst()
        }
        timer.schedule(saveTask, SAVE_INTERVAL, SAVE_INTERVAL)
    }

    fun i(msg: String) {
        log("i", msg)
        Log.i("SimpleLogger", msg)
    }

    fun e(msg: String, throwable: Throwable? = null) {
        log("e", msg + ": " + throwable?.message)
        Log.e("SimpleLogger", msg, throwable)
    }

    private fun log(type: String, msg: String) {
        cache.add("[" + dateFormat.format(Date()) + "] $type:$msg")
        while (cache.size > MAX_LOG_LENGTH) cache.removeFirst()
    }

    fun getLogs(): List<String> = cache
}