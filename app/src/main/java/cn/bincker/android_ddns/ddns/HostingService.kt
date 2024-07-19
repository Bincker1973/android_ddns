package cn.bincker.android_ddns.ddns

import cn.bincker.android_ddns.config.ConfigurationHelper
import cn.bincker.android_ddns.ddns.impl.AliyunHostingService
import cn.bincker.android_ddns.utils.SimpleLogger

abstract class HostingService {
    companion object {
        private val _hostingServices = HashMap<String, HostingService>()
        val hostingServices: Map<String, HostingService> get() = _hostingServices

        init {
            _hostingServices[AliyunHostingService.NAME] = AliyunHostingService()
        }
    }

    /**
     * 检测和更新
     * @param currentAddr 当前IPv6地址
     * @param configHelper 配置读取助手
     * @return 新增/更新的域名信息
     */
    abstract fun checkAndUpdate(currentAddr: String, configHelper: ConfigurationHelper): RecordInfo?

    fun logger() = SimpleLogger.getInstance()

    class RecordInfo(
        val domainName: String?,
        val hostRecord: String?,
        val ttl: Long?,
        val type: String?,
        val value: String?
    )
}