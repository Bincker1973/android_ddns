# DDNS for Android

一个简单的DDNS应用，目前只支持AliYun托管的域名DDNS  
<img src="https://github.com/bit8192/android_ddns/blob/main/android_ddns.jpg" width="400px">

## 特点：
- 支持Aliyun
- 自动启动
- 可调检测周期
- 日志功能
- 检测和调度分离，能够稳定运行

## 如果你想扩展它
如果你想扩展更多的域名托管服务商，你只需要实现`cn.bincker.android_ddns.ddns.HostingService`类  
并加入hostingServices静态属性即可  
你可能在实现的过程中需要实现一些配置参数的设置

