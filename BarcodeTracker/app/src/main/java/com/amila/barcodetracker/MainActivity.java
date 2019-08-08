package com.amila.barcodetracker;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class MainActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    public static final String TAG = "BarcodeTracker";
    private JavaCameraView myJavaCameraView;
    private Mat mRgba;
    private Mat mProcessed;

    BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case BaseLoaderCallback.SUCCESS: {
                    myJavaCameraView.enableView();
                    break;
                }
                default: {
                    super.onManagerConnected(status);
                    break;
                }
            }
        }
    };

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
        System.loadLibrary("opencv_java3");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        myJavaCameraView = (JavaCameraView) findViewById(R.id.MyOpenCvView);
        myJavaCameraView.setVisibility(SurfaceView.VISIBLE);
        myJavaCameraView.setCvCameraViewListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (myJavaCameraView != null)
            myJavaCameraView.disableView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (myJavaCameraView != null)
            myJavaCameraView.disableView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (OpenCVLoader.initDebug()) {
            Log.i(TAG, "Opencv loaded successfully");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        } else {
            Log.e(TAG, "Opencv not loaded");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback);
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
    public native String validate(long matAddrGr, long matAddrRgba);

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC1);
        mProcessed = new Mat();
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();

        mProcessed = extractGradients(mRgba);

        return mProcessed;
    }

    private Mat extractGradients(Mat frame) {
        Mat grayImage = new Mat();
        Mat detectedEdges = new Mat();
        int ddepth = CvType.CV_16S;
        Mat gradX = new Mat();
        Mat gradY = new Mat();
        Mat absGradX = new Mat();
        Mat absGradY = new Mat();

        // Convert to grayscale
        Imgproc.cvtColor(frame, grayImage, Imgproc.COLOR_BGR2GRAY);

        // Gradient X
        Imgproc.Sobel(grayImage, gradX, ddepth, 1, 0);
        Core.convertScaleAbs(gradX, absGradX);

        // Gradient Y
        Imgproc.Sobel(grayImage, gradY, ddepth, 0, 1);
        Core.convertScaleAbs(gradY, absGradY);

        // Total Gradient (approximate)
        Core.addWeighted(absGradX, 0.5, absGradY, 0.5, 0, detectedEdges);

        //Core.subtract(gradX, gradY, detectedEdges);
        //Core.convertScaleAbs(detectedEdges, detectedEdges);

        // Reduce noise with a 3x3 kernel
        Imgproc.GaussianBlur(detectedEdges, detectedEdges, new Size(3, 3), 0, 0, Core.BORDER_DEFAULT);
        //Imgproc.blur(detectedEdges, detectedEdges, new Size(9, 9));

        return detectedEdges;
    }

    void rotate (Mat src, Mat dst, int deg) {
        //Core.transpose(src, dst);
        //Core.flip(dst, dst, deg);
        Mat rotMat;
        Point center = new Point(dst.cols()/2, dst.rows()/2);
        rotMat = Imgproc.getRotationMatrix2D(center, deg, 1);
        Imgproc.warpAffine(src, dst, rotMat, dst.size());
    }

}
