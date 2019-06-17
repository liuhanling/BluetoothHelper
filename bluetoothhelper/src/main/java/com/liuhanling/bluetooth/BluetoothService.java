package com.liuhanling.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.liuhanling.bluetooth.constant.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class BluetoothService {

    private final Handler mHandler;
    private final BluetoothAdapter mAdapter;

    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    private ReceiveThread mReceiveThread;

    private boolean mIsAndroid = false;
    private int mState = BluetoothState.STATE_NONE;

    public BluetoothService(Handler handler) {
        this.mHandler = handler;
        this.mAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    /**
     * 设置状态
     *
     * @param state
     */
    private synchronized void setState(int state) {
        mState = state;
        mHandler.obtainMessage(BluetoothState.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    /**
     * 获取状态
     *
     * @return
     */
    public synchronized int getState() {
        return mState;
    }

    /**
     * 是否已连接
     *
     * @return
     */
    public synchronized boolean isConnected() {
        return mState == BluetoothState.STATE_CONNECTED;
    }

    /**
     * 是否正连接
     *
     * @return
     */
    public synchronized boolean isConnecting() {
        return mState == BluetoothState.STATE_CONNECTING;
    }

    /**
     * 是否已断开
     *
     * @return
     */
    public synchronized boolean isStopped() {
        return mState == BluetoothState.STATE_NONE;
    }

    /**
     * 是否安卓设备
     *
     * @return
     */
    public boolean isAndroid() {
        return mIsAndroid;
    }

    /**
     * 设置安卓设备
     *
     * @return
     */
    public void setAndroid(boolean isAndroid) {
        mIsAndroid = isAndroid;
    }

    /**
     * 开启服务
     *
     * @param isAndroid
     */
    public synchronized void start(boolean isAndroid) {
        closeConnect();
        closeReceive();
        setState(BluetoothState.STATE_LISTEN);
        if (mAcceptThread == null) {
            setAndroid(isAndroid);
            startAccept();
        }
    }

    /**
     * 重连服务
     */
    public void restart() {
        start(mIsAndroid);
    }

    /**
     * 停止服务
     */
    public synchronized void stop() {
        closeConnect();
        closeReceive();
        closeAccept();
        setState(BluetoothState.STATE_NONE);
    }

    /**
     * 连接设备
     *
     * @param device
     */
    public synchronized void connect(BluetoothDevice device) {
        if (isConnecting()) {
            closeConnect();
        }
        closeReceive();
        startConnect(device);
        setState(BluetoothState.STATE_CONNECTING);
    }

    /**
     * 连接设备
     *
     * @param socket
     * @param device
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        closeConnect();
        closeReceive();
        closeAccept();

        startReceive(socket);

        Bundle bundle = new Bundle();
        bundle.putString(Constants.DEVICE_NAME, device.getName());
        bundle.putString(Constants.DEVICE_ADDR, device.getAddress());
        Message message = mHandler.obtainMessage(BluetoothState.MESSAGE_CONNECTED);
        message.setData(bundle);
        mHandler.sendMessage(message);

        setState(BluetoothState.STATE_CONNECTED);
    }

    /**
     * 开始监听线程
     */
    public void startAccept() {
        mAcceptThread = new AcceptThread();
        mAcceptThread.start();
    }

    /**
     * 开始连接线程
     *
     * @param device
     */
    public void startConnect(BluetoothDevice device) {
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
    }

    /**
     * 开始读写线程
     *
     * @param socket
     */
    public void startReceive(BluetoothSocket socket) {
        mReceiveThread = new ReceiveThread(socket);
        mReceiveThread.start();
    }

    /**
     * 发数据包
     *
     * @param buffer
     */
    public void write(byte[] buffer) {
        synchronized (this) {
            if (!isConnected()) {
                sendMessage(BluetoothState.MESSAGE_TOAST, "发送失败！");
                return;
            }
        }
        mReceiveThread.write(buffer);
    }

    /**
     * 回数据包
     *
     * @param what
     * @param obj
     */
    public void sendMessage(int what, Object obj) {
        if (mHandler != null) {
            mHandler.obtainMessage(what, obj).sendToTarget();
        }
    }

    /**
     * 停止监听
     */
    private void closeAccept() {
        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }
    }

    /**
     * 停止连接
     */
    private void closeConnect() {
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
    }

    /**
     * 停止接收
     */
    private void closeReceive() {
        if (mReceiveThread != null) {
            mReceiveThread.cancel();
            mReceiveThread = null;
        }
    }

    /**
     * 蓝牙SDP服务
     */
    private class AcceptThread extends Thread {

        private BluetoothServerSocket mServerSocket;
        private boolean isRunning = true;

        public AcceptThread() {
            BluetoothServerSocket serverSocket = null;
            try {
                if (isAndroid()) {
                    serverSocket = mAdapter.listenUsingRfcommWithServiceRecord(Constants.NAME_SECURE, Constants.UUID_ANDROID);
                } else {
                    serverSocket = mAdapter.listenUsingRfcommWithServiceRecord(Constants.NAME_SECURE, Constants.UUID_OTHER);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            mServerSocket = serverSocket;
        }

        public void run() {
            if (mServerSocket == null) return;
            BluetoothSocket socket;
            while (!isConnected() && isRunning) {
                try {
                    socket = mServerSocket.accept();
                } catch (IOException e) {
                    //e.printStackTrace();
                    break;
                }
                if (socket != null) {
                    synchronized (BluetoothService.class) {
                        switch (BluetoothService.this.getState()) {
                            case BluetoothState.STATE_LISTEN:
                            case BluetoothState.STATE_CONNECTING:
                                connected(socket, socket.getRemoteDevice());
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

        public void kill() {
            isRunning = false;
        }
    }

    /**
     * 蓝牙连接线程
     */
    private class ConnectThread extends Thread {

        private final BluetoothSocket mSocket;
        private final BluetoothDevice mDevice;

        public ConnectThread(BluetoothDevice device) {
            this.mDevice = device;
            BluetoothSocket socket = null;
            try {
                if (isAndroid()) {
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
            if (mSocket == null) {
                return;
            }
            try {
                mSocket.connect();
            } catch (IOException e) {
                e.printStackTrace();
                restart();
                return;
            }
            synchronized (BluetoothService.this) {
                mConnectThread = null;
            }
            connected(mSocket, mDevice);
        }

        public void cancel() {
            try {
                if (mSocket != null) {
                    mSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 蓝牙收发线程
     */
    private class ReceiveThread extends Thread {

        private final BluetoothSocket mSocket;
        private final InputStream mInputStream;
        private final OutputStream mOutputStream;

        public ReceiveThread(BluetoothSocket socket) {
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
            while (true) {
                try {
                    int data = mInputStream.read();
                    if (data == 0x0A) { // \n 换行
                        // do nothing
                    } else if (data == 0x0D) { // \r 回车
                        buffer = new byte[array.size()];
                        for (int i = 0; i < array.size(); i++) {
                            buffer[i] = array.get(i).byteValue();
                        }
                        sendMessage(BluetoothState.MESSAGE_READ, buffer);
                        array = new ArrayList<>();
                    } else {
                        array.add(data);
                    }
                } catch (IOException e) {
                    restart();
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
                if (mSocket != null) {
                    mSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}