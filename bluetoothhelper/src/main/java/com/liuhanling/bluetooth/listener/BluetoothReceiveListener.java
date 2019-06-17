package com.liuhanling.bluetooth.listener;

/**
 * 蓝牙接收监听
 *
 * @author liuhanling
 * @date 2018/11/16
 */
public interface BluetoothReceiveListener {
    void onReceiveMessage(byte[] data, String message);
}
