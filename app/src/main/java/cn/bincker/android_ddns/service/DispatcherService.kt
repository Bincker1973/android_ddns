package cn.bincker.android_ddns.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.util.Log
import java.util.Timer
import java.util.TimerTask

class DispatcherService : Service() {
    private var timeInterval = 1000L
    private var lastCheckTime = 0L
    private var lastIpv6Addr = ""
    private var timer: Timer? = null
    private val task = object: TimerTask(){
        override fun run() {
            lastCheckTime = System.currentTimeMillis()
            Log.i("DispatcherService", "run: dispatcher running")
        }
    }
    private val binder = DispatchBinder()

    inner class DispatchBinder: Binder() {
        fun getLastCheckTime() = lastCheckTime
        fun getTimeInterval() = timeInterval
        fun getLastIpv6Addr() = lastIpv6Addr
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (timer == null || System.currentTimeMillis() - lastCheckTime > timeInterval){
            timer = Timer()
            timer!!.schedule(task, 0, timeInterval)
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent) = binder

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }
}