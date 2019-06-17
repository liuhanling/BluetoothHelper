package com.liuhanling.bluetooth.listener;

/**
 * 蓝牙连接监听
 *
 * @author liuhanling
 * @date 2018/11/16
 */
public interface BluetoothConnectListener {
    void onConnected(String name, String address);

    void onDisconnected();

    void onConnectionFailed();
}
