package cn.bincker.android_ddns.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import java.util.Timer
import java.util.TimerTask

class DispatcherService : Service() {
    private var timeInterval = 1000L
    private var lastActiveTime = 0L
    private var timer: Timer? = null
    private val task = object: TimerTask(){
        override fun run() {
            lastActiveTime = System.currentTimeMillis()
            Log.i("DispatcherService", "run: dispatcher running")
        }
    }
    private val binder = DispatchBinder()

    inner class DispatchBinder: Binder() {
        fun getLastActiveTime() = lastActiveTime
        fun getTimeInterval() = timeInterval
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (timer == null || System.currentTimeMillis() - lastActiveTime > timeInterval){
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