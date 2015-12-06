package com.example.team436.thermalviz;

import android.app.Activity;
import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;
import android.os.Environment;
import android.widget.GridView;
import android.widget.ImageView;
import android.graphics.BitmapFactory;
import android.widget.AdapterView.OnItemClickListener;
import java.io.File;
import java.util.Arrays;

import android.view.View;
import android.widget.AdapterView;
import android.graphics.Bitmap;


/**
 * Created by John on 11/9/15.
 *
 * Code based on:
 * http://stackoverflow.com/questions/13418807/how-can-i-display-images-from-a-specific-folder-on-android-gallery
 */
public class GalleryActivity extends Activity {

    private File file;
    private String[] FilePathStrings;
    private File[] listFile;
    private GridView grid;
    private GalleryAdapter adapter;
    public static Bitmap bmp = null;
    private ImageView imageview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery_layout);

        //Check for SD card
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            Toast.makeText(this, "Error! No SDCARD Found!",
                    Toast.LENGTH_LONG).show();
        } else {
            // Locate the image folder in your SD Card
            file = new File(Environment.getExternalStorageDirectory()
                    .getPath() + "/images/Colored_Images");
        }

        if (file.isDirectory()) {
            listFile = file.listFiles();
            FilePathStrings = new String[listFile.length];
            for (int i = 0; i < listFile.length; i++)
            {
                FilePathStrings[i] = listFile[i].getAbsolutePath();
            }
        }

        Arrays.sort(FilePathStrings);
        int length = FilePathStrings.length;
        for (int i = 0; i < FilePathStrings.length / 2; i++) {
            String tmp = FilePathStrings[i];
            FilePathStrings[i] = FilePathStrings[length - 1 - i];
            FilePathStrings[length - 1 - i] = tmp;
        }

        grid = (GridView)findViewById(R.id.gridview);
        adapter = new GalleryAdapter(this, FilePathStrings);
        grid.setAdapter(adapter);

        grid.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                imageview = (ImageView) findViewById(R.id.imageView1);
                int targetWidth = 700;
                int targetHeight = 500;
                BitmapFactory.Options bmpOptions = new BitmapFactory.Options();
                bmpOptions.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(FilePathStrings[position],
                        bmpOptions);
                int currHeight = bmpOptions.outHeight;
                int currWidth = bmpOptions.outWidth;
                int sampleSize = 1;
                if (currHeight > targetHeight || currWidth > targetWidth) {
                    if (currWidth > currHeight)
                        sampleSize = Math.round((float) currHeight
                                / (float) targetHeight);
                    else
                        sampleSize = Math.round((float) currWidth
                                / (float) targetWidth);
                }
                bmpOptions.inSampleSize = sampleSize;
                bmpOptions.inJustDecodeBounds = false;
                bmp = BitmapFactory.decodeFile(FilePathStrings[position],
                        bmpOptions);
                imageview.setImageBitmap(bmp);
                imageview.setScaleType(ImageView.ScaleType.FIT_XY);
                bmp = null;

            }
        });
    }
}
