package com.amilaabeygunasekara.nativebarcodetracker;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends Activity {

    private static Context mContext;

    static {
        System.loadLibrary("native-tracker");
    }

    public static final String TAG = "NativeBarcodeTracker";
    private int CAMERA_PERMISSION_CODE = 1;

    SurfaceView mSurfaceView;
    SurfaceHolder mSurfaceHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mContext = getApplicationContext();

        mSurfaceView = (SurfaceView)findViewById(R.id.texturePreview);
        mSurfaceHolder = mSurfaceView.getHolder();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
        } else {
            mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder surfaceHolder) {
                    // Sends surface buffer to NDK
                    setSurface(surfaceHolder.getSurface());
                }

                @Override
                public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

                }

                @Override
                public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

                }
            });
        }
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // [HACK] Restaring the app because camera preview is not displayed when granting camera
                // premission for the first time
                restartApp(mContext);
            } else {
                Toast.makeText(getApplicationContext(), "Please allow permission to use the camera",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void restartApp (Context context) {
        Intent mStartActivity = new Intent(context, MainActivity.class);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 30, mPendingIntent);
        System.exit(0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCVMain();
    }

    // Sends surface buffer to NDK
    public native void setSurface(Surface surface);
    // Release native resources
    public native void releaseCVMain();
}
