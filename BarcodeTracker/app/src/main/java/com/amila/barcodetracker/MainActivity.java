package com.amila.barcodetracker;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.view.Gravity;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.core.app.ActivityCompat;

import static android.hardware.camera2.CameraMetadata.LENS_FACING_BACK;

public class MainActivity extends Activity
        implements TextureView.SurfaceTextureListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

    public static final String TAG = "BarcodeTracker";
    long ndkCamera_;
    private TextureView textureView_;
    Surface surface_ = null;
    private Size cameraPreviewSize_;

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

        //Log.e(TAG, "Amila " + validateCam(0, 0));
        if (isCamera2Device()) {
            RequestCamera();
        } else {
            Log.e("CameraSample", "Found legacy camera device, this sample needs camera2 device");
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    private boolean isCamera2Device() {
        CameraManager camMgr = (CameraManager)getSystemService(Context.CAMERA_SERVICE);
        boolean camera2Dev = true;
        try {
            String[] cameraIds = camMgr.getCameraIdList();
            if (cameraIds.length != 0 ) {
                for (String id : cameraIds) {
                    CameraCharacteristics characteristics = camMgr.getCameraCharacteristics(id);
                    int deviceLevel = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
                    int facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                    if (deviceLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY &&
                            facing == LENS_FACING_BACK) {
                        camera2Dev =  false;
                    }
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
            camera2Dev = false;
        }
        return camera2Dev;
    }

    private void createTextureView() {
        textureView_ = (TextureView) findViewById(R.id.texturePreview);
        textureView_.setSurfaceTextureListener(this);
        if (textureView_.isAvailable()) {
            onSurfaceTextureAvailable(textureView_.getSurfaceTexture(),
                    textureView_.getWidth(), textureView_.getHeight());
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        createNativeCamera();

        resizeTextureView(width, height);
        surfaceTexture.setDefaultBufferSize(cameraPreviewSize_.getWidth(),
                cameraPreviewSize_.getHeight());
        surface_ = new Surface(surfaceTexture);
        onPreviewSurfaceCreated(ndkCamera_, surface_);
    }

    private void resizeTextureView(int textureWidth, int textureHeight) {
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int newWidth = textureWidth;
        int newHeight = textureWidth * cameraPreviewSize_.getWidth() / cameraPreviewSize_.getHeight();

        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            newHeight = (textureWidth * cameraPreviewSize_.getHeight()) / cameraPreviewSize_.getWidth();
        }
        textureView_.setLayoutParams(
                new FrameLayout.LayoutParams(newWidth, newHeight, Gravity.CENTER));
        configureTransform(newWidth, newHeight);
    }

    /**
     * configureTransform()
     * Courtesy to https://github.com/google/cameraview/blob/master/library/src/main/api14/com/google/android/cameraview/TextureViewPreview.java#L108
     *
     * @param width  TextureView width
     * @param height is TextureView height
     */
    void configureTransform(int width, int height) {
        int mDisplayOrientation = getWindowManager().getDefaultDisplay().getRotation() * 90;
        Matrix matrix = new Matrix();
        if (mDisplayOrientation % 180 == 90) {
            //final int width = getWidth();
            //final int height = getHeight();
            // Rotate the camera preview when the screen is landscape.
            matrix.setPolyToPoly(
                    new float[]{
                            0.f, 0.f, // top left
                            width, 0.f, // top right
                            0.f, height, // bottom left
                            width, height, // bottom right
                    }, 0,
                    mDisplayOrientation == 90 ?
                            // Clockwise
                            new float[]{
                                    0.f, height, // top left
                                    0.f, 0.f,    // top right
                                    width, height, // bottom left
                                    width, 0.f, // bottom right
                            } : // mDisplayOrientation == 270
                            // Counter-clockwise
                            new float[]{
                                    width, 0.f, // top left
                                    width, height, // top right
                                    0.f, 0.f, // bottom left
                                    0.f, height, // bottom right
                            }, 0,
                    4);
        } else if (mDisplayOrientation == 180) {
            matrix.postRotate(180, width / 2, height / 2);
        }
        textureView_.setTransform(matrix);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        onPreviewSurfaceDestroyed(ndkCamera_, surface_);
        deleteCamera(ndkCamera_, surface_);
        ndkCamera_ = 0;
        surface_ = null;
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }

    private static final int PERMISSION_REQUEST_CODE_CAMERA = 1;

    public void RequestCamera() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSION_REQUEST_CODE_CAMERA);
            return;
        }
        createTextureView();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (PERMISSION_REQUEST_CODE_CAMERA != requestCode) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length == 1 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Thread initCamera = new Thread(new Runnable() {
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            createTextureView();
                        }
                    });
                }
            });
            initCamera.start();
        }
    }

    private void createNativeCamera() {
        Display display = getWindowManager().getDefaultDisplay();
        int height = display.getMode().getPhysicalHeight();
        int width = display.getMode().getPhysicalWidth();

        ndkCamera_ = createCamera(width, height);

        cameraPreviewSize_ = getMinimumCompatiblePreviewSize(ndkCamera_);

    }

    /*
     * Functions calling into NDKCamera side to:
     *     CreateCamera / DeleteCamera object
     *     Start/Stop Preview
     *     Pulling Camera Parameters
     */
    private native long createCamera(int width, int height);

    private native Size getMinimumCompatiblePreviewSize(long ndkCamera);

    private native void onPreviewSurfaceCreated(long ndkCamera, Surface surface);

    private native void onPreviewSurfaceDestroyed(long ndkCamera, Surface surface);

    private native void deleteCamera(long ndkCamera, Surface surface);

    public native String stringFromJNI();
    public native String validateCam(long matAddrGr, long matAddrRgba);

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
