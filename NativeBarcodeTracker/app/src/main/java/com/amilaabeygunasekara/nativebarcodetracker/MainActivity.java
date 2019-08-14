package com.amilaabeygunasekara.nativebarcodetracker;

import android.app.Activity;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

public class MainActivity extends Activity {

    static {
        System.loadLibrary("native-tracker");
    }

    public static final String TAG = "NativeBarcodeTracker";

    SurfaceView mSurfaceView;
    SurfaceHolder mSurfaceHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // send class activity and assest fd to native code
        onCreateJNI(this, getAssets());

        mSurfaceView = (SurfaceView)findViewById(R.id.texturePreview);
        mSurfaceHolder = mSurfaceView.getHolder();

        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                /*Canvas canvas = mSurfaceHolder.lockCanvas();
                if (canvas != null) {
                    canvas.drawColor(Color.BLUE);
                    mSurfaceHolder.unlockCanvasAndPost(canvas);
                }*/
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

    @Override
    protected void onPause() {
        super.onPause();
        releaseCVMain();
    }

    public native void onCreateJNI(Activity callerActivity, AssetManager assetManager);
    // Sends surface buffer to NDK
    public native void setSurface(Surface surface);

    public native void releaseCVMain();
}
