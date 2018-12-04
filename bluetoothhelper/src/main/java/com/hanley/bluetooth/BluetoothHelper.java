package com.hanley.bluetooth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import com.hanley.bluetooth.constant.Constants;
import com.hanley.bluetooth.listener.BluetoothConnectListener;
import com.hanley.bluetooth.listener.BluetoothReceiveListener;
import com.hanley.bluetooth.listener.BluetoothServiceListener;

import java.util.Set;

public class BluetoothHelper {

    private Context mAppContext;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothService mBluetoothService;
    private BluetoothScanner mBluetoothScanner;

    private BluetoothConnectListener mConnectListener;
    private BluetoothServiceListener mServiceListener;
    private BluetoothReceiveListener mReceiveListener;

    private String mDeviceName;
    private String mDeviceAddr;

    private int mConnectionCount = 0;
    private boolean isConnected = false;
    private boolean isConnecting = false;
    private boolean isAutoConnect = false;
    private boolean isAndroid = false;

    @SuppressLint("StaticFieldLeak")
    private static class SingletonHolder {
        private static final BluetoothHelper INSTANCE = new BluetoothHelper();
    }

    public static BluetoothHelper getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private BluetoothHelper() {
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void init(Context context) {
        mAppContext = context.getApplicationContext();
    }

    public void setConnectListener(BluetoothConnectListener connectListener) {
        mConnectListener = connectListener;
    }

    public void setServiceListener(BluetoothServiceListener serviceListener) {
        mServiceListener = serviceListener;
    }

    public void setReceiveListener(BluetoothReceiveListener receiveListener) {
        mReceiveListener = receiveListener;
    }

    public String getDeviceName() {
        return mDeviceName;
    }

    public String getDeviceAddr() {
        return mDeviceAddr;
    }

    private boolean isConnected() {
        return isConnected;
    }

    private void setConnected(boolean connected) {
        isConnected = connected;
    }

    private boolean isConnecting() {
        return isConnecting;
    }

    private void setConnecting(boolean connecting) {
        isConnecting = connecting;
    }

    public boolean isAutoConnect() {
        return isAutoConnect;
    }

    public void setAutoConnect(boolean autoConnect) {
        isAutoConnect = autoConnect;
    }

    private boolean isAndroid() {
        return isAndroid;
    }

    private void setAndroid(boolean android) {
        isAndroid = android;
    }

    /**
     * 蓝牙是否支持
     *
     * @return
     */
    @SuppressLint("HardwareIds")
    public boolean isBluetoothAvailable() {
        return mBluetoothAdapter != null && mBluetoothAdapter.getAddress() != null;
    }

    /**
     * 蓝牙是否打开
     *
     * @return
     */
    public boolean isBluetoothEnabled() {
        return mBluetoothAdapter.isEnabled();
    }

    /**
     * 打开蓝牙
     */
    public void enableBluetooth() {
        mBluetoothAdapter.enable();
    }

    /**
     * 打开蓝牙提示
     *
     * @param activity
     */
    public void enableBluetooth(final Activity activity) {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
    }

    /**
     * 关闭蓝牙
     */
    public void disableBluetooth() {
        mBluetoothAdapter.disable();
    }

    /**
     * 停止蓝牙
     */
    public void close() {
        stopService();
        disableBluetooth();
    }

    /**
     * 正在扫描蓝牙
     */
    public boolean isDiscovering() {
        return mBluetoothAdapter.isDiscovering();
    }

    /**
     * 开始扫描蓝牙
     */
    public boolean startDiscovery() {
        return mBluetoothAdapter.startDiscovery();
    }

    /**
     * 取消扫描蓝牙
     */
    public boolean cancelDiscovery() {
        return mBluetoothAdapter.cancelDiscovery();
    }

    /**
     * 开启蓝牙可见
     */
    public void enableDiscovery() {
        Intent intent = new Intent();
        intent.setAction(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        mAppContext.startActivity(intent);
    }

    /**
     * 扫描蓝牙
     */
    public void scan(long time) {
        if (mBluetoothScanner != null) {
            mBluetoothScanner.scan(time);
        }
    }

    /**
     * 扫描蓝牙
     */
    public void scan(long time, BluetoothScanner.ScanListener listener) {
        if (mBluetoothScanner == null) {
            mBluetoothScanner = new BluetoothScanner(mAppContext, mBluetoothAdapter);
        }
        mBluetoothScanner.setScanListener(listener);
        mBluetoothScanner.scan(time);
    }

    /**
     * 清空蓝牙监听
     */
    public void clearListener() {
        mConnectListener = null;
        mBluetoothScanner = null;
    }

    /**
     * 服务是否可用
     *
     * @return
     */
    public boolean isServiceAvailable() {
        return mBluetoothService != null;
    }

    /**
     * 服务是否已连接
     *
     * @return
     */
    public boolean isServiceConnected() {
        return isServiceAvailable() && mBluetoothService.isConnected();
    }

    /**
     * 服务是否正连接
     *
     * @return
     */
    public boolean isServiceConnecting() {
        return isServiceAvailable() && mBluetoothService.isConnecting();
    }

    /**
     * 服务是否已断开
     *
     * @return
     */
    public boolean isServiceStopped() {
        return isServiceAvailable() && mBluetoothService.isStopped();
    }

    /**
     * 获取服务状态
     *
     * @return
     */
    public int getServiceState() {
        return isServiceAvailable() ? mBluetoothService.getState() : BluetoothState.STATE_NULL;
    }

    /**
     * 设置蓝牙类型
     *
     * @param isAndroid
     */
    public void setTargetDevice(boolean isAndroid) {
        stopService();
        startService(isAndroid);
    }

    /**
     * 开始蓝牙服务
     *
     * @param isAndroid
     */
    public void startService(boolean isAndroid) {
        if (mBluetoothService == null) {
            mBluetoothService = new BluetoothService(mHandler);
        }
        if (isServiceStopped()) {
            setAndroid(isAndroid);
            mBluetoothService.start(isAndroid);
        }
    }

    /**
     * 重启蓝牙服务
     */
    public void restartService() {
        startService(isAndroid);
    }

    /**
     * 停止蓝牙服务
     */
    public void stopService() {
        if (isServiceAvailable()) {
            mBluetoothService.stop();
        }
    }

    /**
     * 连接蓝牙设备
     *
     * @param intent
     */
    public void connect(Intent intent) {
        connect(intent.getStringExtra(Constants.DEVICE_ADDR));
    }

    /**
     * 连接蓝牙设备
     *
     * @param address
     */
    public void connect(String address) {
        if (isServiceAvailable()) {
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
            mBluetoothService.connect(device);
        }
    }

    /**
     * 断开蓝牙设备
     */
    public void disconnect() {
        if (isServiceAvailable()) {
            mBluetoothService.stop();
        }
    }

    /**
     * 发送数据
     *
     * @param data
     * @param CRLF
     */
    public void send(byte[] data, boolean CRLF) {
        if (isServiceConnected()) {
            if (CRLF) {
                byte[] buffer = new byte[data.length + 2];
                System.arraycopy(data, 0, buffer, 0, data.length);
                buffer[buffer.length - 2] = 0x0A;
                buffer[buffer.length - 1] = 0x0D;
                mBluetoothService.write(buffer);
            } else {
                mBluetoothService.write(data);
            }
        }
    }

    /**
     * 发送数据
     *
     * @param data
     * @param CRLF
     */
    public void send(String data, boolean CRLF) {
        if (isServiceConnected()) {
            if (CRLF) {
                data += "\r\n";
            }
            mBluetoothService.write(data.getBytes());
        }
    }

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothState.MESSAGE_STATE_CHANGE:
                    int state = msg.arg1;
                    if (mServiceListener != null) {
                        mServiceListener.onServiceChanged(state);
                    }
                    if (state == BluetoothState.STATE_CONNECTED) break;
                    if (isConnected()) {
                        setConnected(false);
                        if (mConnectListener != null) {
                            mConnectListener.onDisconnected();
                        }
                    }
                    if (isConnecting()) {
                        setConnecting(false);
                        if (mConnectListener != null) {
                            mConnectListener.onConnectionFailed();
                        }
                    } else if (state == BluetoothState.STATE_CONNECTING) {
                        setConnecting(true);
                    }
                    break;
                case BluetoothState.MESSAGE_READ:
                    byte[] data = (byte[]) msg.obj;
                    if (data != null && data.length > 0) {
                        String message = new String(data);
                        if (mReceiveListener != null) {
                            mReceiveListener.onReceiveMessage(data, message);
                        }
                    }
                    break;
                case BluetoothState.MESSAGE_WRITE:
                    break;
                case BluetoothState.MESSAGE_CONNECTED:
                    mDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    mDeviceAddr = msg.getData().getString(Constants.DEVICE_ADDR);
                    if (mConnectListener != null) {
                        mConnectListener.onConnected(mDeviceName, mDeviceAddr);
                    }
                    setConnected(true);
                    setAutoConnect(true);
                    break;
                case BluetoothState.MESSAGE_TOAST:
                    showMsg((String) msg.obj);
                    break;
            }
        }
    };

    /**
     * 自动连接服务监听
     */
    private BluetoothConnectListener mAutoConnectionListener = new BluetoothConnectListener() {
        @Override
        public void onConnected(String name, String address) {
            showMsg("蓝牙连接成功！");
            mConnectionCount = 0;
            mHandler.removeCallbacks(mRunnable);
        }

        @Override
        public void onDisconnected() {
            showMsg("蓝牙连接断开！");
            if (isBluetoothEnabled() && isAutoConnect()) {
                mHandler.postDelayed(mRunnable, 3000);
            }
        }

        @Override
        public void onConnectionFailed() {
            if (isBluetoothEnabled() && isAutoConnect()) {
                mHandler.postDelayed(mRunnable, 5000);
            } else {
                showMsg("蓝牙连接失败！");
            }
        }
    };

    /**
     * 自动连接服务操作
     */
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            mConnectionCount++;
            if (mConnectionCount <= 5) {
                showMsg("尝试重新连接蓝牙[" + mConnectionCount + "/5]...");
                disconnect();
                restartService();
                connect(getDeviceAddr());
            } else {
                mConnectionCount = 0;
                showMsg("蓝牙连接失败！请尝试手动连接！");
                disconnect();
                stopService();
            }
        }
    };

    /**
     * 消息提示
     *
     * @param msg
     */
    private void showMsg(String msg) {
        Toast.makeText(mAppContext, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * 获取绑定设备
     *
     * @return
     */
    public Set<BluetoothDevice> getBondedDevices() {
        return mBluetoothAdapter.getBondedDevices();
    }

    /**
     * 获取绑定设备名称
     *
     * @return
     */
    public String[] getBondedDevicesName() {
        int index = 0;
        Set<BluetoothDevice> devices = getBondedDevices();
        String[] names = new String[devices.size()];
        for (BluetoothDevice device : devices) {
            names[index] = device.getName();
            index++;
        }
        return names;
    }

    /**
     * 获取绑定设备地址
     *
     * @return
     */
    public String[] getBondedDevicesAddr() {
        int index = 0;
        Set<BluetoothDevice> devices = getBondedDevices();
        String[] address = new String[devices.size()];
        for (BluetoothDevice device : devices) {
            address[index] = device.getAddress();
            index++;
        }
        return address;
    }
}