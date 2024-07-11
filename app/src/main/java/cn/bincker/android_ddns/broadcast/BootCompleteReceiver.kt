package cn.bincker.android_ddns.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import cn.bincker.android_ddns.service.DispatcherService

class BootCompleteReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        context.startService(Intent(context, DispatcherService::class.java))
    }
}