package com.example.team436.thermalviz;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.widget.Button;
import android.view.MenuItem;
import android.content.Intent;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    private static final int MENU_DELETE = Menu.FIRST;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        TextView desc = (TextView) findViewById(R.id.desc);
        Typeface customFont = Typeface.createFromAsset(getAssets(), "fonts/HammersmithOne.ttf");
        desc.setTypeface(customFont);

        ((RelativeLayout) findViewById(R.id.bgrnd)).setBackgroundResource(R.drawable.background_with_top_bar);
        ((RelativeLayout) findViewById(R.id.bgrnd)).invalidate();

        final ImageButton cam_button = (ImageButton) findViewById(R.id.scanButton);
        cam_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent scan = new Intent(getApplicationContext(), CameraActivity.class);
                startActivity(scan);
            }
        });

        cam_button.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                    cam_button.setBackgroundResource(R.drawable.scan_btn_active);

                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL)
                    cam_button.setBackgroundResource(R.drawable.scan_btn);

                return false;
            }
        });

        final Button gallery_button = (Button) findViewById(R.id.gallery_btn);
        gallery_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent view = new Intent(getApplicationContext(), GalleryActivity.class);
                startActivity(view);
            }
        });

        final ImageButton mMenu = (ImageButton) findViewById(R.id.galleryButton);

        gallery_button.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                    mMenu.setBackgroundResource(R.drawable.menu_active);

                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL)
                    mMenu.setBackgroundResource(R.drawable.menu_normal);

                return false;
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
