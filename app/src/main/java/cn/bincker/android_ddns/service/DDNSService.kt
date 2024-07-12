package cn.bincker.android_ddns.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.aliyun.auth.credentials.Credential
import com.aliyun.auth.credentials.provider.StaticCredentialProvider
import com.aliyun.sdk.service.alidns20150109.AsyncClient
import com.aliyun.sdk.service.alidns20150109.models.DescribeDomainRecordsRequest
import darabonba.core.client.ClientOverrideConfiguration


class DDNSService : Service() {

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    fun ddns(){
        val provider = StaticCredentialProvider.create(
            Credential.builder()
                .accessKeyId("")
                .accessKeySecret("")
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


        val describeDomainRecordsRequest = DescribeDomainRecordsRequest.builder()
            .domainName("bincker.cn")
            .build()

        val response = client.describeDomainRecords(describeDomainRecordsRequest)

        val resp = response.get()
        for (record in resp.body.domainRecords.record) {
            println(record.rr + "\t" + record.type + "\t" + record.value)
        }
    }
}