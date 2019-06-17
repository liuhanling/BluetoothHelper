[![](https://jitpack.io/v/liuhanling/BluetoothHelper.svg)](https://jitpack.io/#liuhanling/BluetoothHelper)
# BluetoothHelper
Android Bluetooth Framework 蓝牙开发框架。

Feature
--------------
- 基于蓝牙SPP协议，兼容Android 2.0以上的系统。
- 支持搜索、连接、读写等一系列蓝牙操作。
- 支持连接Android设备和非Android设备。
- 支持意外中断后自动重连。
- 发送数据时可选择添加CRLF。
- 接收数据按照CRLF进行分包。

Dependency
--------------

1. Add it in your root `build.gradle` at the end of repositories

```gradle
allprojects {
    repositories {
        ...
        maven { url "https://jitpack.io" }
    }
}
```

2. Add the dependency to your moudle `build.gradle`

```gradle
dependencies {
    implementation 'com.github.liuhanling:BluetoothHelper:1.0'
}
```

Usage
--------------

- AndroidManifest.xml
```xml
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
```

- Application初始化
```java
BluetoothHelper.getInstance().init(this);
```

- 是否支持蓝牙
```java
boolean isBluetoothAvailable()
```

- 蓝牙是否打开
```java
boolean isBluetoothEnable()
```

- 开启/关闭蓝牙
```java
void enableBluetooth()
void enableBluetooth(final Activity activity) // Dialog
void disableBluetooth()
```

- 开启蓝牙服务
```java
void startService(boolean isAndroid)

true: For connection with android device.
false:For connection with any microcontroller which support serial port profile module.
```

- 蓝牙服务状态定义
```java
BluetoothState.STATE_NULL
BluetoothState.STATE_NONE
BluetoothState.STATE_LISTEN
BluetoothState.STATE_CONNECTING
BluetoothState.STATE_CONNECTED
```
- 蓝牙服务状态获取
```java
int getServiceState()
```

- 蓝牙服务状态判断
```java
boolean isServiceAvailable()
boolean isServiceConnected()
boolean isServiceConnecting()
boolean isServiceStopped()
```

- 蓝牙扫描监听
```java
BluetoothHelper.getInstance().scan(8000, new BluetoothScanner.ScanListener() {
    @Override
    public void onScanStart() {
        // mDeviceAdapter.clear();
        // mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onScanDevice(BluetoothDevice device) {
        // mDeviceList.add(device);
        // mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onScanFinish(List<BluetoothDevice> devices) {
        // mProgressBar.setVisibility(View.GONE);
    }
});
```

- 蓝牙连接监听
```java
BluetoothHelper.getInstance().setConnectListener(new BluetoothConnectListener() {
    @Override
    public void onConnected(String name, String address) {
        // hideLoading();
        // showMsg("蓝牙连接成功！");
    }

    @Override
    public void onDisconnected() {
        // hideLoading();
        // showMsg("蓝牙连接断开！");
    }

    @Override
    public void onConnectionFailed() {
        // hideLoading();
        // showMsg("蓝牙连接失败！");
    }
});
```

- 蓝牙状态监听
```java
BluetoothHelper.getInstance().setServiceListener(new BluetoothServiceListener() {
    @Override
    public void onServiceChanged(int state) {
        // updateState(state);
    }
});
```

- 蓝牙接收监听
```java
BluetoothHelper.getInstance().setReceiveListener(new BluetoothReceiveListener() {
    @Override
    public void onReceiveMessage(byte[] data, String message) {
        // handleMessage(message);
    }
});
```

- 连接蓝牙设备
```java
BluetoothHelper.getInstance().connect(address);
```

- 断开蓝牙设备
```java
void disconnect();
```

- 发送消息
```java
void send(byte[] data, boolean CRLF)
void send(String data, boolean CRLF)
```

- 清除监听
```java
void clearListener()
```
