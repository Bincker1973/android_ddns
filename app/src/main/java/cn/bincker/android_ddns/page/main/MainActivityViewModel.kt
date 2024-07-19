package cn.bincker.android_ddns.page.main

import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.bincker.android_ddns.config.ConfigurationHelper
import cn.bincker.android_ddns.service.DispatcherService
import cn.bincker.android_ddns.utils.IPUtils
import cn.bincker.android_ddns.utils.SimpleLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class MainActivityViewModel: ViewModel() {
    var binder: DispatcherService.DispatchBinder? = null

    private val _refreshing = mutableStateOf(false)
    val refreshing: State<Boolean> = _refreshing

    private val _ipv6Addr = mutableStateOf("")
    val ipv6Addr: State<String> = _ipv6Addr

    private val _lastCheckTime = mutableLongStateOf(0L)

    private val _checkTimeInterval = mutableLongStateOf(0L)
    val checkTimeIntervalStr: State<String> = derivedStateOf {
        if (_checkTimeInterval.longValue == 0L){
            "-"
        }else{
            var seconds = _checkTimeInterval.longValue / 1000
            var result = ""
            fun simpleNumFormat(n: Number) = n.toString().let { if (it.length < 2) "0$it" else it }
            if (seconds > 60 * 60){
                result += simpleNumFormat(seconds / (60 * 60)) + "h"
                seconds %= 60 * 60
            }
            if (seconds > 60){
                result += simpleNumFormat(seconds / 60) + "m"
                seconds %= 60
            }
            if (seconds > 0){
                result += seconds.toString() + "s"
            }
            result
        }
    }

    private val _lastIpv6Addr = mutableStateOf("")
    val lastIpv6Addr: State<String> = _lastIpv6Addr

    val lastCheckTimeStr: State<String> = derivedStateOf {
        if (_lastCheckTime.longValue == 0L){
            "-"
        }else{
            SimpleDateFormat.getDateTimeInstance().format(Date(_lastCheckTime.longValue))
        }
    }

    val nextCheckTime: State<String> = derivedStateOf {
        if (_lastCheckTime.longValue == 0L){
            SimpleDateFormat.getDateTimeInstance().format(Date(System.currentTimeMillis() + _checkTimeInterval.longValue))
        }else{
            SimpleDateFormat.getDateTimeInstance().format(Date(_lastCheckTime.longValue + _checkTimeInterval.longValue))
        }
    }

    private val _domainNameHostingService = mutableStateOf("aliyun")
    val domainNameHostingService: State<String> = _domainNameHostingService

    private var _logs = mutableStateOf(SimpleLogger.getInstance().getLogs())
    val logs: State<List<String>> = _logs

    init {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                _logs.value = emptyList()
                _logs.value = SimpleLogger.getInstance().getLogs()
            }
        }
        viewModelScope.launch {
            while (true) {
                delay((binder?.getTimeInterval() ?: 1000) - (System.currentTimeMillis() - (binder?.getLastCheckTime() ?: System.currentTimeMillis())))
                _lastCheckTime.longValue = binder?.getLastCheckTime() ?: 0
                _checkTimeInterval.longValue = binder?.getTimeInterval() ?: 0
                _lastIpv6Addr.value = binder?.getLastIpv6Addr() ?: ""
            }
        }
    }

    fun initDomainNameHostingService(context: Context) {
        val configHelper = ConfigurationHelper(context)
        configHelper.sharedPreferences.registerOnSharedPreferenceChangeListener { sp, key ->
            if (key == "domainNameHostingService"){
                _domainNameHostingService.value = sp.getString("domainNameHostingService", "aliyun") ?: "aliyun"
            }
        }
        _domainNameHostingService.value = configHelper.domainNameHostingService
    }

    fun refresh() {
        _refreshing.value = true
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    Log.d("MainActivityViewModel", "refresh: get current ipv6")
                    _ipv6Addr.value = IPUtils.getIpv6() ?: ""
                }
                _lastCheckTime.longValue = binder?.getLastCheckTime() ?: 0
                _checkTimeInterval.longValue = binder?.getTimeInterval() ?: 0
                _lastIpv6Addr.value = binder?.getLastIpv6Addr() ?: ""
            }finally {
                _refreshing.value = false
            }
        }
    }
}