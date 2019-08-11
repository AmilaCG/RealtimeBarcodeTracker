package com.amila.barcodetracker;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.WindowManager;

import androidx.core.app.ActivityCompat;

public class MainActivity extends Activity
        implements TextureView.SurfaceTextureListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

    public static final String TAG = "BarcodeTracker";

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
        System.loadLibrary("opencv_java3");
        System.loadLibrary("camera_textureview");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Log.e(TAG, "Amila " + validateCam(0, 0));
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
    public native String validateCam(long matAddrGr, long matAddrRgba);

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {

    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }


/*
    private Mat drawBoundingBox (Mat frame) {
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Scalar colour = new Scalar(0, 255, 0);
        double maxArea = 0;
        int largestContIndex = 0;
        Mat image = frame;

        frame = extractGradients(frame);
        frame = cleanUp(frame);

        // Extract all contours
        Imgproc.findContours(frame, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // Find the largest contour
        for (int i = 0; i < contours.size(); i++) {
            double contourArea = Imgproc.contourArea(contours.get(i));
            //Imgproc.drawContours(drawing, contours, i, colour, 2, Core.LINE_8, hierarchy, 0, new Point());
            if (maxArea < contourArea) {
                maxArea = contourArea;
                largestContIndex = i;
            }
        }
        // Draw the largest contour
        Imgproc.drawContours(image, contours, largestContIndex, colour, 2, Core.LINE_8, hierarchy, 0, new Point());

        return image;
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
*/
}
