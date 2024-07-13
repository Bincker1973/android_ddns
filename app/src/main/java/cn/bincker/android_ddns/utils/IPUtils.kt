package cn.bincker.android_ddns.utils

import okhttp3.OkHttpClient
import okhttp3.Request

object IPUtils {
    fun getIpv6(): String?{
        try {
            return OkHttpClient()
                .newCall(Request.Builder().url("https://6.ipw.cn").build()).execute()
                .body
                .string()
                .trim()
                .ifEmpty { null }
        }catch (e:Exception){
            SimpleLogger.getInstance().e("get ip address failed")
            return null
        }
    }
}