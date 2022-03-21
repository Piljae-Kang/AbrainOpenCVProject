package com.example.myapplication;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import androidx.annotation.NonNull;

public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    SurfaceHolder surfaceHolder;
    Camera camera = null;
    Context context;

    public CameraSurfaceView(Context context) {
        super(context);
        init(context);
    }

    public boolean capture(Camera.PictureCallback callback) {
        if (camera != null)
        {
            camera.takePicture(null, null, callback);
            return true;
        }
        else
        {
            return false;
        }
    }

    private void init(Context context) {

        this.context = context;
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        setFocusableInTouchMode(true);
        setFocusable(true);
    }

    public CameraSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        camera = Camera.open();

        try{
            Camera.Parameters parameters = camera.getParameters();

            if (getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
                parameters.set("orientation", "portrait");
                camera.setDisplayOrientation(90);
                parameters.setRotation(90);
            } else {
                parameters.set("orientation", "landscape");
                camera.setDisplayOrientation(0);
                parameters.setRotation(0);
            }
            camera.setParameters(parameters);
            camera.setPreviewDisplay(surfaceHolder);
        }catch(Exception e)
        {
            e.printStackTrace();
        }

        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                camera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean b, Camera camera) {

                    }
                });
            }
        });
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        camera.startPreview();
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
        camera.stopPreview();
        camera.release();
        camera = null;
    }
}
