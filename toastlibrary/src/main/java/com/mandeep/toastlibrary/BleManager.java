package com.mandeep.toastlibrary;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BleManager {
    private static  UUID CONFIG_UUID = convertFromInteger(0x2902);
    private static  UUID CHAR_UUID = UUID.fromString("f000112104514000b000000000000000");
    //f0001121-0451-4000-b000-000000000000
    private static  UUID SERVICE_UUID = UUID.fromString("f000112004514000b000000000000000");
    //f0001120-0451-4000-b000-000000000000
    private Application context;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BleScanCallback bleScanCallback;
    private boolean isScanning = false;
    private BluetoothLeScanner bluetoothLeScanner;
    private List<BluetoothDevice> bluetoothDeviceList = new ArrayList<>();
    private static String TAG = "tag";

    /**
    *  first method should be init
    * */

    public static BleManager getInstance() {
        return new BleManager();
    }

    public void init(Application app) {
        if (context == null && app != null) {
            context = app;
            TAG = context.getClass().getSimpleName();
            if (isSupportBle()) {
                bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            } else {
                ToastMessage.showToast(context, "Ble is not supported in your device");
                return;
            }
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

        }
    }

    /**
     * is support ble?
     *
     * @return
     */
    public boolean isSupportBle() {
        return context.getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    /**
     * Open bluetooth
     */
    public void enableBluetooth() {
        if (bluetoothAdapter != null) {
            bluetoothAdapter.enable();
        }
    }

    /**
     * Disable bluetooth
     */
    public void disableBluetooth() {
        if (bluetoothAdapter != null) {
            if (bluetoothAdapter.isEnabled())
                bluetoothAdapter.disable();
        }
    }

    /**
     * judge Bluetooth is enable
     *
     * @return
     */
    public boolean isBlueEnable() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }


    /**
     *  start scanning
     *
     */


    public void startScan(final BleScanCallback bleScanCallback){
        this.bleScanCallback = bleScanCallback;

        if (!isBleManagerInitialised())
            return;

        if (!isScanning){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopScanning();
                    bleScanCallback.onScanFinished(bluetoothDeviceList);
                }
            }, 5000);
            isScanning = true;
            bluetoothDeviceList.clear();
            bluetoothLeScanner.startScan(scanCallback);
        } else {
            stopScanning();
        }

    }

    /**
     *  stop scanning
     *
     */

    public void stopScanning(){
        if (isScanning)
            if (bluetoothAdapter!=null) {
                isScanning = false;
                bluetoothLeScanner.stopScan(scanCallback);
            }

    }

    /**
     *  leScan callbacks
     *
     */

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if (!bluetoothDeviceList.contains(result.getDevice())){
                bluetoothDeviceList.add(result.getDevice());
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            bleScanCallback.onScanFailed("error code is "+errorCode);
        }
    };



    /**
     *  check bleManager is initialised or not
     */

    public boolean isBleManagerInitialised(){
        boolean isInit = false;
        if (context!=null&&bluetoothAdapter!=null){
            isInit = true;
        }

        return isInit;
    }


    /**
     *  connect device
     */


    public void connectDevice(BluetoothDevice device){
        if (device==null){
            Log.e(TAG, "connectDevice: device is null");
            return;
        }
        stopScanning();
        device.connectGatt(context,false,bluetoothGattCallback);
    }


    private BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            if (newState == BluetoothGatt.STATE_CONNECTED){
                gatt.discoverServices();
                Log.d(TAG, "onConnectionStateChange: connected");
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED){
                Log.d(TAG, "onConnectionStateChange: disconnected");
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, "Device service discovery unsuccessful, status " + status);
                return;
            }
            gatt.getServices();

//            setNotification(gatt);

        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            bleScanCallback.onMessageReceived(characteristic.getValue());
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            bleScanCallback.onMessageReceived(characteristic.getValue());
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
        }
    };

    /**
     *  setNotification enable
     * */

    private void setNotification(BluetoothGatt gatt) {


        BluetoothGattCharacteristic characteristic =gatt.getService(SERVICE_UUID)
                .getCharacteristic(CHAR_UUID);




        /* Enable notification  on the heart rate measurement characteristic */
        gatt.setCharacteristicNotification(characteristic, true);



        BluetoothGattDescriptor descriptor =
                characteristic.getDescriptor(CONFIG_UUID);

        descriptor.setValue(
                BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);


        gatt.writeDescriptor(descriptor);

    }

    /**
     * convert from an integer to UUID.
     * @param i integer input
     * @return UUID
     */
    private static UUID convertFromInteger(int i) {
        final long MSB = 0x0000000000001000L;
        final long LSB = 0x800000805f9b34fbL;
        long value = i & 0xFFFFFFFF;
        return new UUID(MSB | (value << 32), LSB);
    }



}
