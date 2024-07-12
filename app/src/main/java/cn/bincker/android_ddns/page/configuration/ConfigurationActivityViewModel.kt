package cn.bincker.android_ddns.page.configuration

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import cn.bincker.android_ddns.config.ConfigurationHelper

class ConfigurationActivityViewModel: ViewModel() {
    private val _domainName = mutableStateOf("")
    val domainName: State<String> = _domainName

    private val _domainNameHostingService = mutableStateOf("")
    val domainNameHostingService: State<String> = _domainNameHostingService

    private val _aliyunAccessKeyId = mutableStateOf("")
    val aliyunAccessKeyId: State<String> = _aliyunAccessKeyId

    private val _aliyunAccessKeySecret = mutableStateOf("")
    val aliyunAccessKeySecret: State<String> = _aliyunAccessKeySecret

    fun initValue(configHelper: ConfigurationHelper){
        _domainName.value = configHelper.domainName
        _domainNameHostingService.value = configHelper.domainNameHostingService
        _aliyunAccessKeyId.value = configHelper.aliyunAccessKeyId
        _aliyunAccessKeySecret.value = configHelper.aliyunAccessKeySecret
    }

    fun setDomainName(value: String){
        _domainName.value = value
    }

    fun setDomainNameHostingService(value: String){
        _domainNameHostingService.value = value
    }

    fun setAliyunAccessKeyId(value: String){
        _aliyunAccessKeyId.value = value
    }

    fun setAliyunAccessKeySecret(value: String){
        _aliyunAccessKeySecret.value = value
    }
}