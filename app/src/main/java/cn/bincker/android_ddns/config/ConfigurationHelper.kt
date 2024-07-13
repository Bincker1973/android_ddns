package cn.bincker.android_ddns.config

import android.content.Context
import cn.bincker.android_ddns.ddns.impl.AliyunHostingService

class ConfigurationHelper(context: Context) {
    val sharedPreferences = context.getSharedPreferences("Config", Context.MODE_PRIVATE)

    var checkTimeInterval: Long
        get() = sharedPreferences.getLong("checkTimeInterval", 10 * 60 * 1000)
        set(value) = sharedPreferences.edit().putLong("checkTimeInterval", value).apply()

    var domainNameHostingService: String
        get() = sharedPreferences.getString("domainNameHostingService", AliyunHostingService.NAME) ?: AliyunHostingService.NAME
        set(value) { sharedPreferences.edit().putString("domainNameHostingService", value).apply() }

    var aliyunAccessKeyId: String
        get() = sharedPreferences.getString("aliyun.access.keyId", "") ?: ""
        set(value) { sharedPreferences.edit().putString("aliyun.access.keyId", value).apply() }

    var aliyunAccessKeySecret: String
        get() = sharedPreferences.getString("aliyun.access.keySecret", "") ?: ""
        set(value) { sharedPreferences.edit().putString("aliyun.access.keySecret", value).apply() }

    var domainName: String
        get() = sharedPreferences.getString("domainName", "") ?: ""
        set(value) { sharedPreferences.edit().putString("domainName", value).apply() }

    var hostRecord: String
        get() = sharedPreferences.getString("hostRecord", "") ?: ""
        set(value) { sharedPreferences.edit().putString("hostRecord", value).apply() }
}