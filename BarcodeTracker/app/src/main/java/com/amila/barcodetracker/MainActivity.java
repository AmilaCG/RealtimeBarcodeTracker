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
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    public static final String TAG = "BarcodeTracker";
    private JavaCameraView myJavaCameraView;
    private Mat mRgba;

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
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();

        return drawBoundingBox(mRgba);
    }

    private Mat drawBoundingBox (Mat frame) {
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Mat drawing;
        Scalar colour = new Scalar(0, 255, 0);

        frame = extractGradients(frame);
        frame = cleanUp(frame);

        // Extract all contours
        Imgproc.findContours(frame, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        //Point[] cont = contours.get(0).toArray();
        //Log.e(TAG, "[Amila] contours.size(): " + contours.size());

        drawing = Mat.zeros(frame.size(), CvType.CV_8UC3);
        for (int i = 0; i < contours.size(); i++) {
            Imgproc.drawContours(drawing, contours, i, colour, 2, Core.LINE_8, hierarchy, 0, new Point());
        }
        //Imgproc.drawContours(drawing, contours, 0, colour, 2, Core.LINE_8, hierarchy, 0, new Point());

        return drawing;
    }

    private Mat cleanUp (Mat frame) {
        Mat kernel;
        Mat thresh = new Mat();
        Mat cleaned = new Mat();
        Mat anchor = new Mat();

        // Reducing noise further by using threshold
        Imgproc.threshold(frame, thresh, 120, 255, Imgproc.THRESH_BINARY);

        // Close gaps using a closing kernel
        kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(21,7));
        Imgproc.morphologyEx(thresh, cleaned, Imgproc.MORPH_CLOSE, kernel);

        // Perform erosions and dilations
        Imgproc.erode(cleaned, cleaned, anchor, new Point(-1, -1), 4);
        Imgproc.dilate(cleaned, cleaned, anchor, new Point(-1, -1), 4);

        return cleaned;
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
