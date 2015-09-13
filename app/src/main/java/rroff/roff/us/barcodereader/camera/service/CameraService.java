/*
 * Copyright(c) 2015 Ron Roff
 * All Rights Reserved.
 *
 * Author: Ron Roff (rroff@roff.us)
 * Creation Date: 9/12/2015
 */
package rroff.roff.us.barcodereader.camera.service;

import android.app.Service;
import android.content.Intent;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import rroff.roff.us.barcodereader.camera.CameraUtility;

public class CameraService extends Service {

    private static final String LOG_TAG = CameraService.class.getName();

    private IBinder mBinder = new CameraBinder();

    private CameraServiceTask mServiceThread;

    @Override
    public void onCreate() {
        super.onCreate();

        // Start management thread
        mServiceThread = new CameraServiceTask(this);
        mServiceThread.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
        if (mServiceThread != null) {
            mServiceThread.terminate();
        }
        super.onDestroy();
    }

    public void changeCamera(int cameraId) {
        if (mServiceThread != null) {
            mServiceThread.changeCamera(cameraId);
        } else {
            Log.w(LOG_TAG, "Camera management thread unavailable");
        }
    }

    public Camera getActiveCamera() {
        Camera camera = null;

        if (mServiceThread != null) {
            camera = mServiceThread.getActiveCamera();
        }

        return camera;
    }

    public int getActiveCameraId() {
        int cameraId;

        if (mServiceThread != null) {
            cameraId = mServiceThread.getActiveCameraId();
        } else {
            cameraId = CameraUtility.NO_CAMERA;
        }

        return cameraId;
    }

    public Camera.CameraInfo[] getCameraArray() {
        Camera.CameraInfo[] cameraArray = null;

        if (mServiceThread != null) {
            cameraArray = mServiceThread.getCameraArray();
        }

        return cameraArray;
    }

    public void registerCameraListener(CameraConnection listener) {
        if (mServiceThread != null) {
            mServiceThread.registerCameraListener(listener);
        }
    }

    public void unregisterCameraListener(CameraConnection listener) {
        if (mServiceThread != null) {
            mServiceThread.unregisterCameraListener(listener);
        }
    }

    public class CameraBinder extends Binder {
        public CameraService getService() {
            return CameraService.this;
        }
    }
}