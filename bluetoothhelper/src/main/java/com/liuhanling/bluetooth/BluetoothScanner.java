package com.liuhanling.bluetooth;

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

/**
 * 蓝牙扫描类
 */
public class BluetoothScanner {

    private Context mContext;
    private IntentFilter mIntentFilter;
    private ScanListener mScanListener;
    private BluetoothAdapter mAdapter;
    private BluetoothLeScanner mScanner;
    private ScanCallback mScanCallback;
    private Handler mHandler = new Handler();
    private Set<BluetoothDevice> mDeviceSet = new HashSet<>();

    public BluetoothScanner(Context context) {
        this.mContext = context;
        this.initScanner();
    }

    public void setScanListener(ScanListener listener) {
        this.mScanListener = listener;
    }

    /**
     * 设置蓝牙扫描过滤
     */
    private void initScanner() {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mIntentFilter == null) {
            mIntentFilter = new IntentFilter();
            mIntentFilter.addAction(BluetoothDevice.ACTION_FOUND);
            mIntentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            mIntentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (mScanner == null) {
                mScanner = mAdapter.getBluetoothLeScanner();
            }
            // 调用时构建，否则4.4会报 NoClassDefFoundError
            if (mScanCallback == null) {
                mScanCallback = new ScanCallback() {
                    @Override
                    public void onScanResult(int callbackType, ScanResult result) {
                        super.onScanResult(callbackType, result);
                        BluetoothDevice device = result.getDevice();
                        if (mScanListener != null && mDeviceSet.add(device)) {
                            mScanListener.onScanDevice(device);
                        }
                    }
                };
            }
        }
    }

    /**
     * 蓝牙扫描
     */
    public void scan(long time) {
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

    /**
     * 注册蓝牙扫描广播
     */
    private void registerReceiver() {
        mContext.registerReceiver(mBluetoothReceiver, mIntentFilter);
        mAdapter.startDiscovery();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mScanner.startScan(mScanCallback);
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mScanner.stopScan(mScanCallback);
        }
    }

    /**
     * 蓝牙广播接收
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

    /**
     * 蓝牙扫描监听
     */
    public interface ScanListener {
        void onScanStart();

        void onScanDevice(BluetoothDevice device);

        void onScanFinish(List<BluetoothDevice> devices);
    }
}
