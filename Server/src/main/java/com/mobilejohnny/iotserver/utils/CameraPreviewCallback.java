package com.mobilejohnny.iotserver.utils;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

import java.io.IOException;

/**
 * Created by admin2 on 2015/5/12.
 */
public class CameraPreviewCallback implements SurfaceHolder.Callback {
    private final SurfaceView surfaceView;
    private Camera camera;

    public CameraPreviewCallback(Context context,Camera camera,SurfaceView surfaceView) {
        this.camera = camera;
        this.surfaceView = surfaceView;
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Camera.Parameters cameraParam = camera.getParameters();
        Camera.Size prevSize = cameraParam.getPreviewSize();
        ViewGroup.LayoutParams layoutParams = surfaceView.getLayoutParams();
        layoutParams.width = surfaceHolder.getSurfaceFrame().height() * prevSize.height / prevSize.width;
        surfaceView.setLayoutParams(layoutParams);
        Log.i(getClass().getSimpleName(), "设置宽度：" + layoutParams.width);
        Log.i(getClass().getSimpleName(), "预览画面尺寸：" + prevSize.width + " " + prevSize.height);
        cameraParam.setPreviewFpsRange(30000, 30000);
        camera.setParameters(cameraParam);
        try {

            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }
}
