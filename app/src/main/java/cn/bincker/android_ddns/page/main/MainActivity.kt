package cn.bincker.android_ddns.page.main

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.bincker.android_ddns.page.configuration.ConfigurationActivity
import cn.bincker.android_ddns.service.DDNSService
import cn.bincker.android_ddns.service.DispatcherService
import cn.bincker.android_ddns.ui.theme.Android_ddnsTheme
import cn.bincker.android_ddns.ui.theme.LightPrimary
import cn.bincker.android_ddns.utils.SimpleLogger

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
                    RefreshableContainer(viewModel = viewModel)
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
fun RefreshableContainer(viewModel: MainActivityViewModel) {
    val swipeRefreshState = rememberPullToRefreshState()
    LaunchedEffect(swipeRefreshState.isRefreshing){
        if (swipeRefreshState.isRefreshing) {
            viewModel.refresh()
        }
    }
    LaunchedEffect(viewModel.refreshing.value){
        if (viewModel.refreshing.value){
            swipeRefreshState.startRefresh()
        }else{
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
        Text("DDNS", fontSize = 72.sp, fontWeight = FontWeight.Bold, color = LightPrimary, modifier = Modifier.padding(top = 20.dp))
        Text("on Android", fontSize = 20.sp, color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(bottom = 50.dp))
        InfoRow {
            Text("Your IPv6 addr: ")
            Text(viewModel.ipv6Addr.value, color = MaterialTheme.colorScheme.outlineVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        InfoRow {
            Text("Interval: ")
            Text(viewModel.checkTimeIntervalStr.value, color = MaterialTheme.colorScheme.outlineVariant)
        }
        InfoRow {
            Text("Check time: ")
            Text(viewModel.lastCheckTimeStr.value, color = MaterialTheme.colorScheme.outlineVariant)
        }
        InfoRow {
            Text("Check IPv6 addr: ")
            Text(viewModel.lastIpv6Addr.value, color = MaterialTheme.colorScheme.outlineVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        InfoRow {
            Text("Next time: ")
            Text(viewModel.nextCheckTime.value, color = MaterialTheme.colorScheme.outlineVariant)
        }
        InfoRow({
            context.startActivity(Intent(context, ConfigurationActivity::class.java))
        }) {
            Text("Domain name hosting service: ")
            Text(viewModel.domainNameHostingService.value, color = MaterialTheme.colorScheme.outlineVariant)
        }
        Spacer(modifier = Modifier.height(10.dp))
        Column(modifier = Modifier.weight(1f).fillMaxSize()) {
            Logs(viewModel)
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Bottom, horizontalAlignment = Alignment.CenterHorizontally) {
                Text("https://github.com", modifier = Modifier.padding(top = 20.dp), color = MaterialTheme.colorScheme.outlineVariant)
                Button({
                    context.startService(Intent(context, DDNSService::class.java))
                }, modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp).fillMaxWidth()) {
                    Text("Now!")
                }
            }
        }
    }
}

@Composable
fun InfoRow(onClick: ()->Unit = {}, content: @Composable RowScope.() -> Unit) {
    Row(modifier = Modifier.fillMaxSize().clickable(onClick = onClick).padding(50.dp, 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        content()
    }
}

@Composable
fun Logs(viewModel: MainActivityViewModel){
    val listState = rememberLazyListState()
    val logs by viewModel.logs
    LaunchedEffect(logs.size){
        Log.d("MainActivity", "update log scroll")
        listState.animateScrollToItem(logs.size - 1)
    }
    LazyColumn(state = listState, modifier = Modifier.padding(vertical = 20.dp).fillMaxWidth().height(260.dp)) {
        items(logs) {item->
            Text(text = item, color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier, maxLines = 5, fontSize = 12.sp)
        }
    }
}

@Preview
@Composable
fun StatusPreview(){
    Status(MainActivityViewModel())
}
