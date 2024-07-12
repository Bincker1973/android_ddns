package cn.bincker.android_ddns.page.configuration

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cn.bincker.android_ddns.config.ConfigurationHelper
import cn.bincker.android_ddns.ui.theme.Android_ddnsTheme

class ConfigurationActivity : ComponentActivity() {
    private val viewModel: ConfigurationActivityViewModel by viewModels()
    private lateinit var configHelper: ConfigurationHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configHelper = ConfigurationHelper(applicationContext)
        viewModel.initValue(configHelper)
        setContent {
            Android_ddnsTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Configuration(viewModel)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("ConfigurationActivity", "onDestroy")
    }

    fun save() {
        configHelper.domainName = viewModel.domainName.value
        configHelper.domainNameHostingService = viewModel.domainNameHostingService.value
        configHelper.aliyunAccessKeyId = viewModel.aliyunAccessKeyId.value
        configHelper.aliyunAccessKeySecret = viewModel.aliyunAccessKeySecret.value
        finish()
    }

}

@Composable
fun Configuration(viewModel: ConfigurationActivityViewModel){
    val context = LocalContext.current
    val activity: ConfigurationActivity? = if(context is ConfigurationActivity) context else null
    var domainNameHostingServiceExpand by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = viewModel.domainName.value,
            onValueChange = { viewModel.setDomainName(it) },
            label = { Text("解析域名") },
            modifier = Modifier
                .fillMaxWidth()
        )
        TextField(
            value = viewModel.domainNameHostingService.value,
            onValueChange = { viewModel.setDomainNameHostingService(it) },
            label = { Text("域名托管服务商") },
            modifier = Modifier
                .fillMaxWidth()
        )
        TextField(
            value = viewModel.aliyunAccessKeyId.value,
            onValueChange = { viewModel.setAliyunAccessKeyId(it) },
            label = { Text("aliyunAccessKeyId") },
            modifier = Modifier
                .fillMaxWidth()
        )
        TextField(
            value = viewModel.aliyunAccessKeySecret.value,
            onValueChange = { viewModel.setAliyunAccessKeySecret(it) },
            label = { Text("aliyunAccessKeySecret") },
            modifier = Modifier
                .fillMaxWidth()
        )
        Box {
            OutlinedButton({domainNameHostingServiceExpand = true}) {
                Text("DomainNameHostingService")
            }
            DropdownMenu(domainNameHostingServiceExpand, onDismissRequest = {domainNameHostingServiceExpand = false}) {
                DropdownMenuItem(text = {Text("option 1")}, onClick = {})
                DropdownMenuItem(text = {Text("option 2")}, onClick = {})
                DropdownMenuItem(text = {Text("option 3")}, onClick = {})
                DropdownMenuItem(text = {Text("option 4")}, onClick = {})
                DropdownMenuItem(text = {Text("option 5")}, onClick = {})
            }
        }
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Bottom, horizontalAlignment = Alignment.CenterHorizontally) {
            Button({activity?.save()}, modifier = Modifier.padding(30.dp).fillMaxWidth()) {
                Text("保存")
            }
        }
    }
}

@Preview
@Composable
fun ConfigurationPreview(){
    Configuration(ConfigurationActivityViewModel())
}