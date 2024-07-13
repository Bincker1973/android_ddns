package cn.bincker.android_ddns.service

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.util.Log
import cn.bincker.android_ddns.config.ConfigurationHelper
import cn.bincker.android_ddns.utils.SimpleLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask

class DispatcherService : Service() {
    private var timeInterval = 1000L
    private var lastCheckTime = 0L
    private var lastIpv6Addr = ""
    private var timer: Timer? = null
    private lateinit var configHelper: ConfigurationHelper
    private val job = Job()
    private val scope = CoroutineScope(job)
    private val ddnsConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder) {
            if (binder is DDNSService.DDNSBinder){
                scope.launch {
                    Log.d("DispatcherService", "onServiceConnected: wait complete start.")
                    binder.waitComplete()
                    Log.d("DispatcherService", "onServiceConnected: wait complete over. logs: ${SimpleLogger.getInstance().getLogs().size}")
                    lastCheckTime = System.currentTimeMillis()
                    lastIpv6Addr = binder.addr ?: ""
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {}
    }
    private val task = object: TimerTask(){
        override fun run() {
            try {
                Log.d("DispatcherService", "start bind ddns service.")
                applicationContext.bindService (
                    Intent(this@DispatcherService, DDNSService::class.java),
                    ddnsConnection,
                    Context.BIND_AUTO_CREATE
                )
                applicationContext.startService(Intent(this@DispatcherService, DDNSService::class.java))
            }catch (e:Exception){
                Log.e("DispatcherService", "dispatch error", e)
            }
        }
    }
    private val binder = DispatchBinder()

    inner class DispatchBinder: Binder() {
        fun getLastCheckTime() = lastCheckTime
        fun getTimeInterval() = timeInterval
        fun getLastIpv6Addr() = lastIpv6Addr
    }

    override fun onCreate() {
        super.onCreate()
        configHelper = ConfigurationHelper(applicationContext)
        timeInterval = configHelper.checkTimeInterval
        configHelper.sharedPreferences.registerOnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == "checkTimeInterval"){
                timeInterval = configHelper.checkTimeInterval
                timer?.cancel()
                timer?.purge()
                timer = Timer()
                timer?.schedule(task, timeInterval, timeInterval)
            }
        }
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