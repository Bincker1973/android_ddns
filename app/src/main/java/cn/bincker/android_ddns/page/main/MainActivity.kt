package cn.bincker.android_ddns.page.main

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.bincker.android_ddns.page.configuration.ConfigurationActivity
import cn.bincker.android_ddns.service.DispatcherService
import cn.bincker.android_ddns.ui.theme.Android_ddnsTheme
import cn.bincker.android_ddns.ui.theme.Primary
import cn.bincker.android_ddns.ui.theme.TextDescribe

class MainActivity : ComponentActivity() {
    private val viewModel: MainActivityViewModel by viewModels()
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
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    RefreshableStatus(viewModel = viewModel)
                }
            }
        }
        startService(Intent(baseContext, DispatcherService::class.java))
        bindService(Intent(baseContext, DispatcherService::class.java), dispatcherServiceConnection, BIND_AUTO_CREATE)
        viewModel.initDomainNameHostingService(applicationContext)
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(dispatcherServiceConnection)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefreshableStatus(viewModel: MainActivityViewModel) {
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
        Status(viewModel = viewModel)
        PullToRefreshContainer(state = swipeRefreshState, modifier = Modifier.align(Alignment.TopCenter))
    }
}

@Composable
fun Status(viewModel: MainActivityViewModel){
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize().verticalScroll(ScrollState(0)), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("DDNS", fontSize = 72.sp, fontWeight = FontWeight.Bold, color = Primary, modifier = Modifier.padding(top = 20.dp))
        Text("on Android", fontSize = 20.sp, color = TextDescribe, modifier = Modifier.padding(bottom = 50.dp))
        InfoRow {
            Text("Your IPv6 addr: ")
            Text(viewModel.ipv6Addr.value, color = TextDescribe, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        InfoRow {
            Text("Interval: ")
            Text(viewModel.checkTimeIntervalStr.value, color = TextDescribe)
        }
        InfoRow {
            Text("Check time: ")
            Text(viewModel.lastCheckTimeStr.value, color = TextDescribe)
        }
        InfoRow {
            Text("Check IPv6 addr: ")
            Text(viewModel.lastIpv6Addr.value, color = TextDescribe, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        InfoRow {
            Text("Next time: ")
            Text(viewModel.nextCheckTime.value, color = TextDescribe)
        }
        InfoRow({
            context.startActivity(Intent(context, ConfigurationActivity::class.java))
        }) {
            Text("Domain name hosting service: ")
            Text(viewModel.domainNameHostingService.value, color = TextDescribe)
        }
    }
}

@Composable
fun InfoRow(onClick: ()->Unit = {}, content: @Composable RowScope.() -> Unit) {
    Row(modifier = Modifier.fillMaxSize().clickable(onClick = onClick).padding(50.dp, 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        content()
    }
}

@Preview
@Composable
fun StatusPreview(){
    Status(MainActivityViewModel())
}
