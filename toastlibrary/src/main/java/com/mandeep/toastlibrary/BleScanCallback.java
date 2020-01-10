package com.mandeep.toastlibrary;

import android.bluetooth.BluetoothDevice;

import java.util.List;

public interface BleScanCallback {
    void onScanFinished(List<BluetoothDevice> scanResultList);
    void onScanFailed(String reason);
    void onMessageReceived(byte[] msg);
}
