package com.hanley.bluetooth.service;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import com.hanley.bluetooth.BluetoothService;
import com.hanley.bluetooth.constant.Constants;

import java.io.IOException;

/**
 * 蓝牙连接线程
 *
 * @author liuhanling
 * @date 2018/11/16
 */
public class ConnectThread extends Thread {

    private final BluetoothService mService;
    private final BluetoothSocket mSocket;
    private final BluetoothDevice mDevice;

    public ConnectThread(BluetoothService service, BluetoothDevice device) {
        this.mService = service;
        this.mDevice = device;
        BluetoothSocket socket = null;
        try {
            if (mService.isAndroid()) {
                socket = device.createRfcommSocketToServiceRecord(Constants.UUID_ANDROID);
            } else {
                socket = device.createRfcommSocketToServiceRecord(Constants.UUID_OTHER);
            }
        } catch (IOException e) {
            e.printStackTrace();

        }
        mSocket = socket;
    }

    public void run() {
        if (mSocket == null) return;
        try {
            mSocket.connect();
        } catch (IOException e) {
            e.printStackTrace();
            cancel();
            mService.restart();
            return;
        }
        mService.connected(mSocket, mDevice);
    }

    public void cancel() {
        try {
            mSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
