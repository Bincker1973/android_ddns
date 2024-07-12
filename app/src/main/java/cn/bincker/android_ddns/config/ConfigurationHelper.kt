package cn.bincker.android_ddns.config

import android.content.Context

class ConfigurationHelper(context: Context) {
    val sharedPreferences = context.getSharedPreferences("Config", Context.MODE_PRIVATE)

    var domainNameHostingService: String
        get() = sharedPreferences.getString("domainNameHostingService", "aliyun") ?: "aliyun"
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
}