package rroff.roff.us.barcodereader.camera.service;

import android.app.Service;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.FrameLayout;

import rroff.roff.us.barcodereader.camera.CameraPreview;
import rroff.roff.us.barcodereader.camera.CameraUtility;

/**
 * Created by rroff on 9/12/2015.
 */
public class CameraService extends Service {

    private static final String LOG_TAG = CameraService.class.getName();

    private IBinder mBinder = new CameraBinder();

    private Camera.CameraInfo[] mCameraArray;

    private Camera mCamera;

    @Override
    public void onCreate() {
        super.onCreate();
        mCameraArray = CameraUtility.getCameraArray(this);
        mCamera = null;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void changeCamera(int cameraId, FrameLayout previewFrame) {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }

        if (mCamera == null) {
            mCamera = CameraUtility.getCameraInstance(this, cameraId);
            if (mCamera != null) {
                if (previewFrame != null) {
                    previewFrame.addView(new CameraPreview(this, mCamera));
                }
            } else {
                Log.e(LOG_TAG, "Unable to access camera " + cameraId);
            }
        }
    }

    public Camera.CameraInfo[] getCameraArray() {
        return mCameraArray;
    }

    public class CameraBinder extends Binder {
        public CameraService getService() {
            return CameraService.this;
        }
    }
}
