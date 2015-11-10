package com.example.team436.thermalviz;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.widget.Button;
import android.view.MenuItem;
import android.content.Intent;

public class MainActivity extends AppCompatActivity {

    private static final int MENU_DELETE = Menu.FIRST;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final Button cam_button = (Button) findViewById(R.id.scanButton);
        cam_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent scan = new Intent(getApplicationContext(), CameraActivity.class);
                startActivity(scan);
            }
            });

        final Button gallery_button = (Button) findViewById(R.id.galleryButton);
        gallery_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent view = new Intent(getApplicationContext(), GalleryActivity.class);
                startActivity(view);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.add(Menu.NONE, MENU_DELETE, Menu.NONE, "Delete");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case MENU_DELETE:

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
