package com.hanley.bluetooth.service;

import android.bluetooth.BluetoothSocket;

import com.hanley.bluetooth.BluetoothService;
import com.hanley.bluetooth.BluetoothState;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * 蓝牙接发线程
 *
 * @author liuhanling
 * @date 2018/11/16
 */
public class ReceiveThread extends Thread {

    private final BluetoothService mService;
    private final BluetoothSocket mSocket;
    private final InputStream mInputStream;
    private final OutputStream mOutputStream;

    public ReceiveThread(BluetoothService service, BluetoothSocket socket) {
        this.mService = service;
        this.mSocket = socket;
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = mSocket.getInputStream();
            outputStream = mSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mInputStream = inputStream;
        mOutputStream = outputStream;
    }

    public void run() {
        if (mInputStream == null || mOutputStream == null) {
            return;
        }
        ArrayList<Integer> array = new ArrayList<>();
        byte[] buffer;
        while (!Thread.interrupted()) {
            try {
                int data = mInputStream.read();
                if (data == 0x0A) { // \n 换行
                    // do nothing
                } else if (data == 0x0D) { // \r 回车
                    buffer = new byte[array.size()];
                    for (int i = 0; i < array.size(); i++) {
                        buffer[i] = array.get(i).byteValue();
                    }
                    mService.sendMessage(BluetoothState.MESSAGE_READ, buffer);
                    array = new ArrayList<>();
                } else {
                    array.add(data);
                }
            } catch (IOException e) {
                mService.restart();
                break;
            }
        }
    }

    public void write(byte[] buffer) {
        try {
            mOutputStream.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cancel() {
        try {
            interrupt();
            mSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
