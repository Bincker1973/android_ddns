package cn.bincker.android_ddns

import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.bincker.android_ddns.service.DispatcherService
import cn.bincker.android_ddns.utils.IPUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivityViewModel: ViewModel() {
    var binder: DispatcherService.DispatchBinder? = null

    private val _refreshing = mutableStateOf(false)
    val refreshing: State<Boolean> = _refreshing

    private val _ipv6Addr = mutableStateOf("")
    val ipv6Addr: State<String> = _ipv6Addr

    private val _lastCheckTime = mutableLongStateOf(0L)
    val lastCheckTime: State<Long> = _lastCheckTime

    private val _checkTimeInterval = mutableLongStateOf(0L)
    val checkTimeInterval: State<Long> = _checkTimeInterval
    val checkTimeIntervalStr: State<String> = derivedStateOf {
        if (_checkTimeInterval.longValue == 0L){
            "-"
        }else{
            var seconds = _checkTimeInterval.longValue / 1000
            var result = ""
            fun simpleNumFormat(n: Number) = n.toString().let { if (it.length < 2) "0$it" else it }
            if (seconds > 60 * 60000){
                result += simpleNumFormat(seconds / (60 * 60000)) + ":"
                seconds %= 60 * 60000;
            }
            if (seconds > 60000){
                result += simpleNumFormat(seconds % 60000) + ":"
                seconds %= 60 * 60000;
            }
            result += seconds.toString()
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
            "-"
        }else{
            SimpleDateFormat.getDateTimeInstance().format(Date(_lastCheckTime.longValue + _checkTimeInterval.longValue))
        }
    }

    fun refresh() {
        viewModelScope.launch(context = Dispatchers.IO) {
            _refreshing.value = true
            _ipv6Addr.value = IPUtils.getIpv6() ?: ""
            _lastCheckTime.longValue = binder?.getLastCheckTime() ?: 0
            _checkTimeInterval.longValue = binder?.getTimeInterval() ?: 0
            _lastIpv6Addr.value = binder?.getLastIpv6Addr() ?: ""
        }
    }
}