package com.hanley.bluetooth.listener;

/**
 * 蓝牙状态监听
 *
 * @author liuhanling
 * @date 2018/11/16
 */
public interface BluetoothServiceListener {
    void onServiceChanged(int state);
}
