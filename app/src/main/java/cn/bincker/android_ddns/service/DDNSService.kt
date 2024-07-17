package cn.bincker.android_ddns.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import cn.bincker.android_ddns.config.ConfigurationHelper
import cn.bincker.android_ddns.ddns.HostingService
import cn.bincker.android_ddns.utils.IPUtils
import cn.bincker.android_ddns.utils.SimpleLogger
import kotlinx.coroutines.*


class DDNSService : Service() {
    private val job = Job()
    private val scope = CoroutineScope(job)
    private val logger = SimpleLogger.getInstance()
    private val notifyLock = Object()
    private val binder = DDNSBinder(notifyLock)

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        scope.launch {
            try {
                logger.i("get current ip address...")
                val currentAddr = withContext(Dispatchers.IO) {
                    IPUtils.getIpv6()
                }
                if (currentAddr.isNullOrBlank()) {
                    logger.e("IP address is null!")
                    return@launch
                }
                logger.i("current ip address is $currentAddr...")
                val configHelper = ConfigurationHelper(applicationContext)
                val result = withContext(Dispatchers.IO) {
                    val service = HostingService.hostingServices[configHelper.domainNameHostingService]
                    if (service == null){
                        logger.e("HostingService is null! domainNameHostingService=${configHelper.domainNameHostingService}")
                        return@withContext null
                    }
                    service.checkAndUpdate(
                        currentAddr,
                        configHelper
                    )
                }
                binder.addr = currentAddr
                binder.result = result
            }catch (e: Exception){
                logger.e("execute check failed", e)
            }finally {
                synchronized(notifyLock){
                    notifyLock.notifyAll()
                }
            }
        }
        return START_NOT_STICKY
    }

    class DDNSBinder(@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN") private val lock: Object): Binder(){
        var addr: String? = null
        var result: HostingService.RecordInfo? = null
        fun waitComplete(){
            synchronized(lock){
                lock.wait()
            }
        }
    }
}