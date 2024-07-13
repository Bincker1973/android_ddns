package cn.bincker.android_ddns.ddns.impl

import android.util.Log
import cn.bincker.android_ddns.config.ConfigurationHelper
import cn.bincker.android_ddns.ddns.HostingService
import com.aliyun.auth.credentials.Credential
import com.aliyun.auth.credentials.provider.StaticCredentialProvider
import com.aliyun.sdk.service.alidns20150109.AsyncClient
import com.aliyun.sdk.service.alidns20150109.models.*
import darabonba.core.client.ClientOverrideConfiguration

class AliyunHostingService: HostingService() {
    companion object {
        private const val CONFIG_KEY_RECORD_ID = "aliyun.config.key.record.id"
        const val NAME = "aliyun"
    }

    override fun checkAndUpdate(currentAddr: String, configHelper: ConfigurationHelper): RecordInfo? {
        val domainName = configHelper.domainName
        val hostRecord = configHelper.hostRecord
        val aliyunAccessKeyId = configHelper.aliyunAccessKeyId
        val aliyunAccessKeySecret = configHelper.aliyunAccessKeySecret

        if (domainName.isEmpty()) {
            logger().e("domainName is empty")
            return null
        }
        if (hostRecord.isEmpty()) {
            logger().e("hostRecord is empty")
            return null
        }
        if (aliyunAccessKeyId.isEmpty()) {
            logger().e("aliyunAccessKeyId is empty")
            return null
        }
        if (aliyunAccessKeySecret.isEmpty()) {
            logger().e("aliyunAccessKeySecret is empty")
            return null
        }

        try {
            val provider = StaticCredentialProvider.create(
                Credential.builder()
                    .accessKeyId(aliyunAccessKeyId)
                    .accessKeySecret(aliyunAccessKeySecret)
                    .build()
            )

            val client =
                AsyncClient.builder()
                    .credentialsProvider(provider)
                    .overrideConfiguration(
                        ClientOverrideConfiguration.create()
                            .setEndpointOverride("alidns.cn-hangzhou.aliyuncs.com")
                    )
                    .build()

            var recordId = configHelper.sharedPreferences.getString(CONFIG_KEY_RECORD_ID, null)
            logger().i("recordId: $recordId")
            var domainRecord: DescribeDomainRecordInfoResponseBody? = null
            if (recordId == null) {
                logger().i("find record...")
                val describeDomainRecordsRequest = DescribeDomainRecordsRequest.builder()
                    .domainName(domainName)
                    .build()

                val response = client.describeDomainRecords(describeDomainRecordsRequest)

                val resp = response.get()
                if (resp.statusCode == 200) {
                    for (r in resp.body.domainRecords.record) {
                        if (r.rr == hostRecord) {
                            logger().i("found recordId: $recordId")
                            domainRecord = DescribeDomainRecordInfoResponseBody.Builder()
                                .recordId(r.recordId)
                                .domainName(r.domainName)
                                .line(r.line)
                                .locked(r.locked)
                                .priority(r.priority)
                                .rr(r.rr)
                                .recordId(r.recordId)
                                .remark(r.remark)
                                .status(r.status)
                                .TTL(r.ttl)
                                .type(r.type)
                                .value(r.value)
                                .build()
                            recordId = r.recordId
                            break
                        }
                    }
                    if (recordId != null) configHelper.sharedPreferences.edit()
                        .putString(CONFIG_KEY_RECORD_ID, recordId)
                        .apply()
                }else{
                    logger().e("get describe domain records fail: statusCode=${resp.statusCode}")
                    return null
                }
            } else {
                val request = DescribeDomainRecordInfoRequest.builder()
                    .recordId(recordId)
                    .build()
                val response = client.describeDomainRecordInfo(request).get()
                domainRecord = response.body
            }

            if (domainRecord == null) {
                //新增记录
                logger().i("add record: domainName=$domainName, hostRecord=$hostRecord, type=AAAA, value=$currentAddr")
                val request = AddDomainRecordRequest.builder()
                    .domainName(domainName)
                    .rr(hostRecord)
                    .type("AAAA")
                    .value(currentAddr)
                    .build()
                val response = client.addDomainRecord(request).get()
                if (response.statusCode == 200){
                    configHelper.sharedPreferences.edit().putString(CONFIG_KEY_RECORD_ID, response.body.recordId).apply()
                    logger().i("add record success: recordId=${response.body.recordId}")
                }else{
                    logger().e("add record failed: statusCode=${response.statusCode}")
                }
            } else {
                //修改记录
                if (domainRecord.value == currentAddr){
                    logger().i("record same.")
                }else{
                    logger().i("update record: recordId=$recordId, hostRecord=$hostRecord, type=AAAA, value=$currentAddr")
                    val request = UpdateDomainRecordRequest.builder()
                        .recordId(recordId)
                        .rr(hostRecord)
                        .type("AAAA")
                        .value(currentAddr)
                        .build()
                    val response = client.updateDomainRecord(request).get()
                    if (response.statusCode == 200){
                        logger().i("update record success: recordId=${response.body.recordId}, value=$currentAddr")
                    }else{
                        logger().e("update record failed: statusCode=${response.statusCode}")
                    }
                }
            }
            return RecordInfo(domainRecord?.domainName, domainRecord?.rr, domainRecord?.ttl, domainRecord?.type, domainRecord?.value)
        }catch (e: Exception) {
            Log.e("AliyunHostingService", "checkAndUpdate fail", e)
            logger().e("check and update fail: ${e.message}")
            return null
        }
    }
}