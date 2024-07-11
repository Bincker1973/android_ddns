package cn.bincker.android_ddns

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import cn.bincker.android_ddns.service.DispatcherService
import cn.bincker.android_ddns.ui.theme.Android_ddnsTheme

class MainActivity : ComponentActivity() {
    private var dispatcherBinder: DispatcherService.DispatchBinder? = null
    private val dispatcherServiceConnection = object: ServiceConnection{
        override fun onServiceConnected(cn: ComponentName?, binder: IBinder?) {
            dispatcherBinder = binder as DispatcherService.DispatchBinder?
        }
        override fun onServiceDisconnected(p0: ComponentName?) {
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Android_ddnsTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Status()
                }
            }
        }
        startService(Intent(baseContext, DispatcherService::class.java))
        bindService(Intent(baseContext, DispatcherService::class.java), dispatcherServiceConnection, BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(dispatcherServiceConnection)
    }
}

@Composable
fun Status(modifier: Modifier = Modifier) {
    Column(verticalArrangement = Arrangement.Center) {
        Text(
            text = "上一次更新时间：",
            modifier = modifier
        )
        Text(text = "", modifier = modifier)
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Android_ddnsTheme {
        Status()
    }
}