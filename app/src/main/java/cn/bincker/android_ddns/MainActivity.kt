package cn.bincker.android_ddns

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import cn.bincker.android_ddns.service.DispatcherService
import cn.bincker.android_ddns.ui.theme.Android_ddnsTheme

class MainActivity : ComponentActivity() {
    val viewModel = MainActivityViewModel()
    private val dispatcherServiceConnection = object: ServiceConnection{
        override fun onServiceConnected(cn: ComponentName?, binder: IBinder?) {
            viewModel.binder = binder as DispatcherService.DispatchBinder?
            viewModel.refresh()
        }
        override fun onServiceDisconnected(p0: ComponentName?) {
            viewModel.binder = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Android_ddnsTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Status(viewModel = viewModel)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Status(modifier: Modifier = Modifier, viewModel: MainActivityViewModel) {
    val swipeRefreshState = rememberPullToRefreshState()
    if (swipeRefreshState.isRefreshing){
        viewModel.refresh()
    }
    LaunchedEffect(viewModel.refreshing.value){
        if (!viewModel.refreshing.value){
            swipeRefreshState.endRefresh()
        }
    }
    Box(modifier = Modifier.nestedScroll(swipeRefreshState.nestedScrollConnection)) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(ScrollState(0))) {
            Text("IPv6地址：" + viewModel.ipv6Addr.value)
            Text("检测时间间隔：" + viewModel.checkTimeIntervalStr.value)
            Text("上一次检测时间：" + viewModel.lastCheckTimeStr.value)
            Text("上一次检测IP地址：" + viewModel.lastIpv6Addr.value)
            Text("下一次检测时间：" + viewModel.nextCheckTime.value)
        }
        PullToRefreshContainer(state = swipeRefreshState, modifier = Modifier.align(Alignment.TopCenter))
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Android_ddnsTheme {
        Status(viewModel = MainActivityViewModel())
    }
}