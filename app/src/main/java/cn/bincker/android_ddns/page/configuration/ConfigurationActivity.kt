package cn.bincker.android_ddns.page.configuration

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cn.bincker.android_ddns.config.ConfigurationHelper
import cn.bincker.android_ddns.ddns.HostingService
import cn.bincker.android_ddns.ddns.impl.AliyunHostingService
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
        configHelper.hostRecord = viewModel.hostRecord.value
        configHelper.domainNameHostingService = viewModel.domainNameHostingService.value
        configHelper.aliyunAccessKeyId = viewModel.aliyunAccessKeyId.value
        configHelper.aliyunAccessKeySecret = viewModel.aliyunAccessKeySecret.value
        finish()
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Configuration(viewModel: ConfigurationActivityViewModel){
    val context = LocalContext.current
    val activity: ConfigurationActivity? = if(context is ConfigurationActivity) context else null
    var showSelectDialog by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = (viewModel.checkTimeInterval.value / 1000).toString(),
            onValueChange = { viewModel.setCheckTimeInterval(it.toLong() * 1000) },
            label = { Text("检测时间间隔") },
            placeholder = { Text("如：bincker.cn", color = MaterialTheme.colorScheme.outlineVariant) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            suffix = { Text("秒") }
        )
        TextField(
            value = viewModel.domainName.value,
            onValueChange = { viewModel.setDomainName(it) },
            label = { Text("绑定域名") },
            placeholder = { Text("如：bincker.cn", color = MaterialTheme.colorScheme.outlineVariant) },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = viewModel.hostRecord.value,
            onValueChange = { viewModel.setHostRecord(it) },
            label = { Text("记录") },
            placeholder = { Text("如：www", color = MaterialTheme.colorScheme.outlineVariant) },
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { showSelectDialog = true }.fillMaxWidth().height(TextFieldDefaults.MinHeight).background(color = MaterialTheme.colorScheme.surfaceVariant).padding(18.dp),
        ) {
            Text("域名托管服务商", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(viewModel.domainNameHostingService.value, color = MaterialTheme.colorScheme.outline)
        }
        if (showSelectDialog) {
            BasicAlertDialog(
                onDismissRequest = { showSelectDialog = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.background),
            ) {
                Column {
                    HostingService.hostingServices.keys.forEach { key ->
                        OptionItem(key) {
                            viewModel.setDomainNameHostingService(key)
                            showSelectDialog = false
                        }
                    }
                }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.scrim)
        when (viewModel.domainNameHostingService.value) {
            AliyunHostingService.NAME -> {
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
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Bottom, horizontalAlignment = Alignment.CenterHorizontally) {
            Button({activity?.save()}, modifier = Modifier.padding(30.dp).fillMaxWidth()) {
                Text("保存")
            }
        }
    }
}

@Composable
fun OptionItem(title: String, onClick: ()->Unit = {}){
    Text(title, modifier = Modifier.clickable { onClick() }.fillMaxWidth().padding(vertical = 8.dp, horizontal = 16.dp))
}

@Preview
@Composable
fun ConfigurationPreview(){
    Configuration(ConfigurationActivityViewModel())
}