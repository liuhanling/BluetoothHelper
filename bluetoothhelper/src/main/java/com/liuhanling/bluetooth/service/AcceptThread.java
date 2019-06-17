package com.liuhanling.bluetooth.service;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import com.liuhanling.bluetooth.BluetoothService;
import com.liuhanling.bluetooth.BluetoothState;
import com.liuhanling.bluetooth.constant.Constants;

import java.io.IOException;

/**
 * 蓝牙SDP服务
 *
 * @author liuhanling
 * @date 2018/11/16
 */
public class AcceptThread extends Thread {

    private BluetoothService mService;
    private BluetoothServerSocket mServerSocket;

    public AcceptThread(BluetoothService service, BluetoothAdapter adapter) {
        this.mService = service;
        try {
            if (mService.isAndroid()) {
                mServerSocket = adapter.listenUsingRfcommWithServiceRecord(Constants.NAME_SECURE, Constants.UUID_ANDROID);
            } else {
                mServerSocket = adapter.listenUsingRfcommWithServiceRecord(Constants.NAME_SECURE, Constants.UUID_OTHER);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        if (mServerSocket == null) return;
        BluetoothSocket socket;
        while (!Thread.interrupted() && !mService.isConnected()) {
            try {
                socket = mServerSocket.accept();
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
            if (socket == null) break;
            synchronized (BluetoothService.class) {
                switch (mService.getState()) {
                    case BluetoothState.STATE_LISTEN:
                    case BluetoothState.STATE_CONNECTING:
                        mService.connected(socket, socket.getRemoteDevice());
                        break;
                    case BluetoothState.STATE_NONE:
                    case BluetoothState.STATE_CONNECTED:
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                }
            }
        }
    }

    public void cancel() {
        try {
            if (mServerSocket != null) {
                mServerSocket.close();
                mServerSocket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
