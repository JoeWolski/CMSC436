package com.example.joe.opencvcameratest;

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
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import android.app.ActionBar;
import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends Activity implements CvCameraViewListener {
    private static final String TAG = "OCVSample::Activity";

    private CameraBridgeViewBase mOpenCvCameraView;
    private boolean              mIsJavaCamera = true;
    private MenuItem             mItemSwitchCamera = null;
    private ThermalSensor mThermal = new ThermalSensor();

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

    public MainActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        if (mIsJavaCamera)
            mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial1_activity_java_surface_view);
        else
            mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial1_activity_native_surface_view);

        mOpenCvCameraView.setMaxFrameSize(640, 480);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onPause() {
        mThermal.stop();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
        if(mThermal.start(this)) {
            mThermal.setLaser(true);
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

            if (mIsJavaCamera) {
                mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial1_activity_java_surface_view);
                toastMesage = "Java Camera";
            } else {
                mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial1_activity_native_surface_view);
                toastMesage = "Native Camera";
            }

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

        Point p = findLaser(inputFrame);

        if(p != null) {
            Core.circle(inputFrame, p, 5, new Scalar(255, 0, 0, 50), -1);
            String temp = Double.toString(mThermal.read());
            Core.putText(inputFrame, temp, p, Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255,0,0));
        }

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
            int radius = 5;
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
}