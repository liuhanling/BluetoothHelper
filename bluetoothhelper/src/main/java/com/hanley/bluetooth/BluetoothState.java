package com.hanley.bluetooth;

public class BluetoothState {

    // Current connection state
    public static final int STATE_NULL = -1;
    public static final int STATE_NONE = 0;
    public static final int STATE_LISTEN = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;

    // Handler message type
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_CONNECTED = 4;
    public static final int MESSAGE_TOAST = 5;

    // Intent request codes
    public static final int REQUEST_ENABLE_BT = 101;
}
