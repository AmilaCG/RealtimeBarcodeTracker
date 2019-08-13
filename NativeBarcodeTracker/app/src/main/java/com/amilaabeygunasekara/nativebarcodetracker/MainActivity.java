package com.amilaabeygunasekara.nativebarcodetracker;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

public class MainActivity extends Activity {

    static {
        System.loadLibrary("native-lib");
    }

    public static final String TAG = "NativeBarcodeTracker";

    SurfaceView mSurfaceView;
    SurfaceHolder mSurfaceHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mSurfaceView = (SurfaceView)findViewById(R.id.texturePreview);
        mSurfaceHolder = mSurfaceView.getHolder();

        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                Canvas canvas = mSurfaceHolder.lockCanvas();
                if (canvas != null) {
                    canvas.drawColor(Color.BLUE);
                    mSurfaceHolder.unlockCanvasAndPost(canvas);
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

            }
        });
    }

    public native String stringFromJNI();
}
