package com.example.team436.thermalviz;

import android.util.Log;
import android.widget.BaseAdapter;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.view.ViewGroup;
import android.graphics.BitmapFactory;

/**
 * Created by John on 11/18/15.
 *
 * Based on code from http://stackoverflow.com/questions/13418807/how-can-i-display-images-from-a-specific-folder-on-android-gallery
 */
public class GalleryAdapter extends BaseAdapter{

    private Activity                activity;
    private String[]                filepath;
    private static LayoutInflater   inflater    = null;
    Bitmap                          bmp         = null;

    public GalleryAdapter (Activity a, String[] fpath)
    {
        activity = a;
        filepath = fpath;
        inflater = (LayoutInflater)activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount () {
        if(filepath != null)
            return filepath.length;
        else
            return 0;
    }

    public Object getItem (int position)
    {
        return position;
    }

    public long getItemId (int position)
    {
        return position;
    }

    public View getView (int position, View convertView, ViewGroup parent)
    {
        View vi = convertView;
        if (convertView == null)
            vi = inflater.inflate(R.layout.image_layout, null);
        ImageView image = (ImageView)vi.findViewById(R.id.picture);
        int targetWidth = 100;
        int targetHeight = 75;
        BitmapFactory.Options bmpOptions = new BitmapFactory.Options();
        bmpOptions.inJustDecodeBounds = true;
        //BitmapFactory.decodeFile(filepath[position], bmpOptions);
        int currHeight = bmpOptions.outHeight;
        int currWidth = bmpOptions.outWidth;
        int sampleSize = 1;
        if (currHeight > targetHeight || currWidth > targetWidth) {
            if (currWidth > currHeight)
                sampleSize = Math.round((float)currHeight / (float)targetHeight);
            else
                sampleSize = Math.round((float)currWidth / (float)targetWidth);
        }
        bmpOptions.inSampleSize = sampleSize;
        bmpOptions.inJustDecodeBounds = false;

        Log.v("GALLERY", "LOADING IMAGE WITH PATH \"" + filepath[position] + "\"");
        bmp = BitmapFactory.decodeFile(filepath[position], bmpOptions);
        if(image == null)
            Log.v("IMGCHECK", "image is null");
        if(bmp == null)
            Log.v("IMGCHECK", "bmp is null");
        image.setImageBitmap(bmp);
        image.setScaleType(ImageView.ScaleType.CENTER);

        bmp = null;
        return vi;
    }
}
