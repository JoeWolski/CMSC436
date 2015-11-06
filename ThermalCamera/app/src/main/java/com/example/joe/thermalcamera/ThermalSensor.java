package com.example.joe.thermalcamera;

import android.content.Context;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.HexDump;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Joe on 10/23/15.
 */

public class ThermalSensor {

    private UsbManager mUsbManager;
    private UsbSerialPort sPort = null;
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private SerialInputOutputManager mSerialIoManager = null;
    private final String TAG = "USB_APP";

    private double temp;
    private boolean fresh_data;

    private final SerialInputOutputManager.Listener mListener =
            new SerialInputOutputManager.Listener() {

                @Override
                public void onRunError(Exception e) {
                    Log.d(TAG, "Runner stopped.");
                }

                @Override
                public void onNewData(final byte[] data) {
                    String input = new String(data);
                    synchronized (ThermalSensor.this) {
                        temp = Double.parseDouble(input);
                        fresh_data = true;
                    }
                }
            };

    public boolean init(UsbManager usbManager) {

        mUsbManager = usbManager;
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(mUsbManager);
        if (availableDrivers.isEmpty()) {
            Log.v(TAG, "No Drivers");
            return false;
        } else {

            // Open a connection to the first available driver.
            UsbSerialDriver driver = availableDrivers.get(0);
            UsbDeviceConnection connection = mUsbManager.openDevice(driver.getDevice());
            if (connection == null) {
                Log.v(TAG, "Couldn't connect");
                return false;
            } else {
                sPort = driver.getPorts().get(0);
                if (sPort != null) {
                    try {
                        sPort.open(connection);
                        sPort.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
                        mSerialIoManager = new SerialInputOutputManager(sPort, mListener);
                        mExecutor.submit(mSerialIoManager);
                    } catch (IOException e) {
                        Log.v(TAG, "IOException");
                        return false;
                    }
                } else {
                    Log.v(TAG, "No Port");
                    return false;
                }
            }
            return true;
        }
    }

    public double read() {
        synchronized (this) {
            fresh_data = false;
        }
        byte[] send = new byte[2];
        send[0] = 'r';
        send[1] = '\n';
        Log.v(TAG, "About to write");
//        mSerialIoManager.writeAsync(send);
        try {
            sPort.write(send, 1000);
        } catch (IOException e) {
            Log.v(TAG, "Failed to write");
        }
        while(!fresh_data);
        return temp;
    }

    public void setLaser(boolean on) {

        byte[] send = new byte[2];
        if(on) {
            send[0] = 'n';

        } else {
            send[0] = 'f';
        }
        send[1] = '\n';

        try {
            sPort.write(send, 1000);
        } catch (IOException e) {
            Log.v(TAG, "Failed to write");
        }

    }

    public void pause() {
        if (mSerialIoManager != null) {
            Log.i(TAG, "Stopping io manager ..");
            mSerialIoManager.stop();
            mSerialIoManager = null;
        }
    }
}