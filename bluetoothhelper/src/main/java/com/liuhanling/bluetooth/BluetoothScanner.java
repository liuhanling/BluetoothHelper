package com.liuhanling.bluetooth;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BluetoothScanner {

    private Context mContext;
    private IntentFilter mIntentFilter;
    private ScanListener mScanListener;
    private BluetoothAdapter mAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private Handler mHandler = new Handler();
    private Set<BluetoothDevice> mDeviceSet = new HashSet<>();

    public BluetoothScanner(Context context, BluetoothAdapter adapter) {
        this.mContext = context;
        this.mAdapter = adapter;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.mBluetoothLeScanner = mAdapter.getBluetoothLeScanner();
        }
    }

    /**
     * 设置蓝牙扫描过滤
     */
    private void initFilter() {
        if (mIntentFilter == null) {
            mIntentFilter = new IntentFilter();
            mIntentFilter.addAction(BluetoothDevice.ACTION_FOUND);
            mIntentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            mIntentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        }
    }

    /**
     * 注册蓝牙扫描广播
     */
    private void registerReceiver() {
        initFilter();
        mContext.registerReceiver(mBluetoothReceiver, mIntentFilter);
        mAdapter.startDiscovery();
        if (checkVersion()) {
            mBluetoothLeScanner.startScan(mScanCallback);
        }
    }

    /**
     * 注销蓝牙扫描广播
     */
    private void unregisterReceiver() {
        mAdapter.cancelDiscovery();
        try {
            mContext.unregisterReceiver(mBluetoothReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        if (checkVersion()) {
            mBluetoothLeScanner.stopScan(mScanCallback);
        }
    }

    /**
     * 蓝牙扫描广播接收
     */
    public BroadcastReceiver mBluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mScanListener == null) return;
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (mDeviceSet.add(device)) {
                    mScanListener.onScanDevice(device);
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                mScanListener.onScanStart();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                mScanListener.onScanFinish(new ArrayList<>(mDeviceSet));
            }
        }
    };

    private boolean checkVersion() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    /**
     * 开始扫描经典蓝牙
     */
    public void scan(long time) {
        if (mAdapter.isDiscovering()) {
            mAdapter.cancelDiscovery();
        }
        mDeviceSet.clear();
        registerReceiver();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                unregisterReceiver();
                if (mScanListener != null) {
                    mScanListener.onScanFinish(new ArrayList<>(mDeviceSet));
                }
            }
        }, time);
    }

    public void setScanListener(ScanListener listener) {
        this.mScanListener = listener;
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();
            if (mScanListener != null && mDeviceSet.add(device)) {
                mScanListener.onScanDevice(device);
            }
        }
    };

    /**
     * 蓝牙扫描监听
     */
    public interface ScanListener {
        void onScanStart();

        void onScanDevice(BluetoothDevice device);

        void onScanFinish(List<BluetoothDevice> devices);
    }
}
