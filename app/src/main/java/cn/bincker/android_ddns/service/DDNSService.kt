package cn.bincker.android_ddns.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

class DDNSService : Service() {

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}