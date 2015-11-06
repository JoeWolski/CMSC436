package com.example.joe.thermalcamera;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.HexDump;
import com.hoho.android.usbserial.util.SerialInputOutputManager;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbDeviceConnection;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Place_Holder extends AppCompatActivity {


    private final String TAG = "USB_APP";
    private TextView mText;
    private Button mReadButton;
    private Switch mLaser;
    private ThermalSensor therm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place__holder);

        mText = (TextView) findViewById(R.id.recData);
        mReadButton = (Button) findViewById(R.id.button);
        mLaser = (Switch) findViewById(R.id.laserSwitch);

        therm = new ThermalSensor();
        if(therm.init((UsbManager) getSystemService(Context.USB_SERVICE))) {

            mReadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    double tmp = therm.read();
                    mText.setText(Double.toString(tmp));
                }
            });

            mLaser.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Log.v(TAG, "Switch pressed!");
                    if (isChecked) {
                        therm.setLaser(true);
                    } else {
                        therm.setLaser(false);
                    }
                }
            });
        } else {
            mText.setText("Failed to initialize");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        therm.pause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_place__holder, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
