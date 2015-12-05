package com.example.team436.thermalviz;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.engine.OpenCVEngineInterface;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class CameraActivity extends Activity implements CvCameraViewListener {
    private static final String TAG = "OCVSample::Activity";

    private CameraBridgeViewBase mOpenCvCameraView;
    private boolean              mIsJavaCamera = true;
    private MenuItem             mItemSwitchCamera = null;
    private ThermalSensor mThermal = new ThermalSensor();
    private boolean hasThermal = false;
    private boolean hasScanBase = false;
    private boolean takeScanBase = false;
    private Mat scanBase = null;
    private Mat scanningMat = null;
    private float[][] data = new float[480][640];
    private ImageButton mCamButton;
    private ImageButton mCancleButton;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public CameraActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.camera_layout);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.camview);

        mOpenCvCameraView.setMaxFrameSize(640, 480);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);

        mCamButton = (ImageButton) findViewById(R.id.scanButtton);
        mCancleButton = (ImageButton) findViewById(R.id.cancelButton);

        mCamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasScanBase == false) {
                    if (takeScanBase == false) {
                        Toast.makeText(CameraActivity.this, "Taking background image", Toast.LENGTH_SHORT).show();
                        takeScanBase = true;
                        mCamButton.setImageResource(R.drawable.checkbox);
                    }
                } else {
                    //Toast.makeText(CameraActivity.this, "Saving scan", Toast.LENGTH_SHORT).show();

//                    for(int k = 0; k < 480; k++) {
//                        data[k][k] = ((float) k)/((float) 480.0);
//                    }

                    mOpenCvCameraView.disableView();
                    new ProccessImg(data).execute();
                }
            }
        });

        mCancleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (hasScanBase == true) {
                    hasScanBase = false;
                    mCamButton.setImageResource(R.drawable.camera_very_small);
                } else {
                    finish();
                }
            }
        });

        for(int i = 0; i < 480; i++) {
            for(int j = 0; j < 640; j++) {
                data[i][j] = (float) -5000.0;
            }
        }
    }

    @Override
    public void onPause() {
        if(hasThermal)
            mThermal.stop();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
        for(int i = 0; i < 480; i++) {
            for(int j = 0; j < 640; j++) {
                data[i][j] = (float) -5000.0;
            }
        }
        if(mThermal.start(this)) {
            mThermal.setLaser(true);
            hasThermal = true;
        } else {
            hasThermal = false;
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
        mItemSwitchCamera = menu.add("Switch camera");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String toastMesage = new String();
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);

        if (item == mItemSwitchCamera) {
            mOpenCvCameraView.setVisibility(SurfaceView.GONE);
            mIsJavaCamera = !mIsJavaCamera;

            mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.camview);
            toastMesage = "Java Camera";

            mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
            mOpenCvCameraView.setCvCameraViewListener(this);
            mOpenCvCameraView.enableView();
            Toast toast = Toast.makeText(this, toastMesage, Toast.LENGTH_LONG);
            toast.show();
        }

        return true;
    }

    public void onCameraViewStarted(int width, int height) {
    }

    public void onCameraViewStopped() {
    }

    public Mat onCameraFrame(Mat inputFrame) {

        if(takeScanBase) {
            if(scanBase != null) {
                scanBase.release();
            }
            scanBase = new Mat();
            scanningMat = new Mat();

            Imgproc.cvtColor(inputFrame, scanBase, Imgproc.COLOR_RGB2GRAY);
            Imgproc.cvtColor(scanBase, scanningMat, Imgproc.COLOR_GRAY2RGB);
            hasScanBase = true;
            takeScanBase = false;
        }

        if(hasScanBase) {
            Mat ret;

            Point p = findLaser(inputFrame);

            if (p != null) {
                if (hasThermal) {
                    double temp = mThermal.read();
                    data[(int) p.y][(int) p.x] = (float) temp;

                    Core.circle(scanningMat, p, 3, new Scalar(255, 0, 0), -3);

                } else {
                    //Log.v(TAG, "No thermal!");
                }
            }
            ret = scanningMat.clone();

            return ret;
        }
        else
            return inputFrame;
    }

    public Point findLaser(Mat inputFrame) {
        Mat mHsv = new Mat();

        Imgproc.cvtColor(inputFrame, mHsv, Imgproc.COLOR_RGB2HSV);

        // Find laser center
        Mat center = new Mat();
        Core.inRange(mHsv, new Scalar(0, 0, 250), new Scalar(180, 16, 255), center);

        Mat h = new Mat();
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(center, contours, h, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        center.release();

        Mat center_mask = Mat.zeros(inputFrame.rows(), inputFrame.cols(), CvType.CV_8U);
        if(contours.size() > 0){
            for(int i = 0; i < contours.size(); i++) {
                int radius = 10;
                //Point[] cont_pos = contours.get(i).toArray();
                Moments m = Imgproc.moments(contours.get(i));
                Point p = new Point();
                p.x = m.get_m10()/m.get_m00();
                p.y = m.get_m01()/m.get_m00();
                Core.circle(center_mask, p, radius * 2, new Scalar(255), -1);
            }
        }

        // Find halo
        Mat ranged = new Mat();
        Core.inRange(mHsv, new Scalar(100, 32, 225), new Scalar(150, 255, 255), ranged);
        //Mat f_frame =ranged.clone();

        // Find halo around bright dot
        Core.bitwise_and(ranged, center_mask, ranged);
        mHsv.release();
        center_mask.release();

        // Find biggest resulting contour
        contours.clear();
        Imgproc.findContours(ranged, contours, h, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        h.release();
        ranged.release();

        if(contours.size() > 0){
            MatOfPoint biggest_cont = contours.get(0);
            double cont_size = Imgproc.contourArea(biggest_cont);
            for (int i = 1; i < contours.size(); i++) {
                MatOfPoint cur = contours.get(i);
                if(Imgproc.contourArea(cur) > cont_size) {
                    biggest_cont = cur;
                    cont_size = Imgproc.contourArea(cur);
                }
            }
            Moments m = Imgproc.moments(biggest_cont);
            Point p = new Point();
            p.x = m.get_m10()/m.get_m00();
            p.y = m.get_m01()/m.get_m00();

            return p;
        } else {
            return null;
        }
    }

//    public Mat heatmap(float[][] arr) {
//        LinkedList<com.example.team436.thermalviz.Point> points = new LinkedList<com.example.team436.thermalviz.Point>();
//        int rows = 480;
//        int col = 640;
//        float average = 0;
//        float factor = 0.9F;
//        for(int i = 0; i < rows; i++) {
//            for(int j = 0; j < col; j++) {
//                if(arr[i][j]  <= -300) {
//                    com.example.team436.thermalviz.Point p  = new com.example.team436.thermalviz.Point(j, i, arr[i][j]);
//                    points.add(p);
//                    average += arr[i][j];
//                }
//            }
//        }
//        average /= (640*480);
//        Log.v(TAG, "AVERAGE: " + Float.toString(average));
//        while(!points.isEmpty()) {
//            int x;
//            int y;
//            float temp = 0;
//            int count;
//
//            com.example.team436.thermalviz.Point p = points.remove();
//            x = p.x;
//            y = p.y;
//
//            if (arr[x][y] < -300) {
//                if (x + 1 < col) {
//                    float otmp;
//                    if(y + 1 < rows) {
//                        otmp = arr[x][y+1];
//                        if(otmp )
//                    }
//
//                    if(y - 1 >= 0) {
//
//                    }
//
//                }
//            }
//        }
//    }

    public Mat heatmap(float[][] arr, ProgressDialog pg) {
        LinkedList<com.example.team436.thermalviz.Point> points = new LinkedList<com.example.team436.thermalviz.Point>();
        int rows = 480;
        int col = 640;
        float average = 0;
        int ave_cnt = 0;
        boolean[][] orig = new boolean[480][640];
        //float[][] buf1 = new float[480][640];
        int passes = 640;
        boolean has_max = false;
        boolean has_min = false;
        float min = arr[0][0];
        float max = arr[0][0];

        pg.setMax(passes);

        for(int i = 0; i < rows; i++) {
            for(int j = 0; j < col; j++) {
                if(arr[i][j]  > -300) {
                    orig[i][j] = true;
                    average += arr[i][j];
                    ave_cnt++;
                    if(arr[i][j] > max || has_max == false) {
                        max = arr[i][j];
                        has_max = true;
                    }
                    if(arr[i][j] < min || has_min == false)
                        min = arr[i][j];
                        has_min = true;
                } else {
                    orig[i][j] = false;
                }
            }
        }

        Log.v(TAG, "ARR 480: " + Float.toString(arr[479][479]));
        Log.v(TAG, "ARR 480: " + Float.toString(arr[479][480]));
        Log.v(TAG, "MIN: " + Float.toString(min));
        Log.v(TAG, "MAX: " + Float.toString(max));

        average /= (float) ave_cnt;

        for(int i = 0; i < rows; i++) {
            for(int j = 0; j < col; j++) {
                if(arr[i][j]  <= -300) {
                    arr[i][j] = average;
                }
            }
        }

        for(int k = 0; k < passes; k++) {
            Log.v(TAG, "PASS: " + Integer.toString(k));
            for(int i = 0; i < rows; i++) {
                for (int j = 0; j < col; j++) {
                    if(!orig[i][j]) {
                        float avg = 0;
                        int count = 0;

                        if(j + 1 < 640) {
                            avg += arr[i][j + 1];
                            count++;
                        }

                        if(j - 1 >= 0) {
                            avg += arr[i][j - 1];
                            count++;
                        }

                        if(i + 1 < 480) {
                            avg += arr[i + 1][j];
                            count++;

                            if(j + 1 < 640) {
                                avg += arr[i + 1][j + 1];
                                count++;
                            }

                            if(j - 1 >= 0) {
                                avg += arr[i + 1][j - 1];
                                count++;
                            }
                        }

                        if(i - 1 > 0) {
                            avg += arr[i - 1][j];
                            count++;

                            if(j + 1 < 640) {
                                avg += arr[i - 1][j + 1];
                                count++;
                            }

                            if(j - 1 >= 0) {
                                avg += arr[i - 1][j - 1];
                                count++;
                            }
                        }
                        if(count > 0) {
                            avg = avg/((float) count);
                            arr[i][j] = avg;
                        }
                    }
                }
            }
        }

        Mat hsvImg = new Mat(480, 640, CvType.CV_8UC3);

        for(int i = 0; i < 640; i++) {
            for(int j = 0; j < 480; j++) {
                byte pix[] = new byte[3];
                float cur = arr[j][i];
                //Log.v(TAG, "PIX: " + Float.toString(cur));
                float val = (1 - ((cur - min)/(max - min)))*125;
                if(val > 125)
                    val = 125;
                if(val < 0)
                    val = 0;
                pix[0] = (byte) val;
                pix[1] = (byte) 255;
                pix[2] = (byte) 255;
                hsvImg.put(j, i, pix);
            }
        }

        return hsvImg;
    }

    public class ProccessImg extends AsyncTask<Void, Void, Void> {
        ProgressDialog progressDialog;
        float[][] in_data;
        //declare other objects as per your need

        public ProccessImg(float[][] arr) {
            super();
            in_data = arr;
        }

        @Override
        protected void onPreExecute()
        {
            progressDialog= ProgressDialog.show(CameraActivity.this, "Processing Scan", "", true);

            //do initialization of required objects objects here
        };
        @Override
        protected Void doInBackground(Void... params)
        {
            publishProgress();
            Mat hsvImg = heatmap(data, progressDialog);
            Mat finishedImage = new Mat();
            Imgproc.cvtColor(hsvImg, finishedImage, Imgproc.COLOR_HSV2BGR);

            File mediaStorageDir = new File(Environment.getExternalStorageDirectory().getPath(), "images/Colored_Images");

            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.e(TAG, "failed to create directory");
                    return null;
                }
            }

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            Log.v(TAG, "SAVING: " + mediaStorageDir.getPath() + File.separator + "scan_" + timeStamp + ".jpg");
            Highgui.imwrite(mediaStorageDir.getPath() + File.separator + "scan_" + timeStamp + ".jpg", finishedImage);

            return null;
        }
        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);
            progressDialog.dismiss();
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, CameraActivity.this, mLoaderCallback);
        };
    }
}