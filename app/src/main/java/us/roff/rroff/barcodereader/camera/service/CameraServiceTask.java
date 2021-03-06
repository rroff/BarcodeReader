/*
 * Copyright(c) 2015 Ron Roff
 * All Rights Reserved.
 *
 * Author: Ron Roff (rroff@roff.us)
 * Creation Date: 9/13/2015
 */
package us.roff.rroff.barcodereader.camera.service;

import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import java.util.List;

import us.roff.rroff.barcodereader.camera.CameraUtility;

/**
 * Thread to manage camera access for service.
 */
public class CameraServiceTask extends AsyncTask<Void, Void, Void> {
    private static final String LOG_TAG = CameraServiceTask.class.getName();

    /**
     * Wait time (in milliseconds) between polling for commands.
     */
    private static final int STATUS_POLL_WAIT_MS = 250;

    /**
     * Service owner of thread.
     */
    private CameraService mService;

    /**
     * Flag to be set when thread can be terminated.
     */
    private boolean mExitFlag;

    private Camera.CameraInfo[] mCameraArray;

    private Camera mCamera;

    private CameraConnection mCameraListener;

    private int mActiveCameraId;

    private int mSelectedCameraId;

    public CameraServiceTask(CameraService service) {
        mService          = service;
        mExitFlag         = false;
        mCamera           = null;
        mActiveCameraId   = CameraUtility.NO_CAMERA;
        mSelectedCameraId = CameraUtility.NO_CAMERA;
        mCameraArray      = CameraUtility.getCameraArray(mService);
    }

    public void changeCamera(int cameraId) {
        mSelectedCameraId = cameraId;
    }

    public Camera getActiveCamera() {
        return mCamera;
    }

    public int getActiveCameraId() {
        return mActiveCameraId;
    }

    public Camera.CameraInfo[] getCameraArray() {
        return mCameraArray;
    }

    public boolean isCameraConnected() {
        return (mActiveCameraId != CameraUtility.NO_CAMERA);
    }

    public void registerCameraListener(CameraConnection listener) {
        mCameraListener = listener;
    }

    public void terminate() {
        mExitFlag = true;
    }

    public void unregisterCameraListener(CameraConnection listener) {
        mCameraListener = null;
    }

    /**
     * Command thread.
     *
     * @param params Not used
     * @return Always null
     */
    @Override
    protected Void doInBackground(Void... params) {
        Camera camera = null;

        while (!mExitFlag) {
            if (mSelectedCameraId != mActiveCameraId) {
                changeCamera();
            }

            try {
                Thread.sleep(STATUS_POLL_WAIT_MS);
            } catch (InterruptedException e) {
                Log.w(LOG_TAG, "Thread exiting prematurely: " + e.getMessage());
                mExitFlag = true;
            }
        }

        return null;
    }

    private void changeCamera() {
        // Release existing camera
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;

            if (mCameraListener != null) {
                mCameraListener.onCameraDisconnected();
            }
        }

        // Connect to new camera
        if (mCamera == null) {
            mCamera = CameraUtility.getCameraInstance(mService, mSelectedCameraId);
        }

        // Verify new camera is connected and change internal settings
        if (mCamera != null) {
            mActiveCameraId = mSelectedCameraId;

            Camera.Parameters parameters = mCamera.getParameters();
            List<String> supportedFocusModes = parameters.getSupportedFocusModes();
            if (  android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH
               && supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            } else if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)){
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            }
            mCamera.setParameters(parameters);

            if (mCameraListener != null) {
                mCameraListener.onCameraConnected();
            }
        } else {
            mActiveCameraId = CameraUtility.NO_CAMERA;
            Log.e(LOG_TAG, "Unable to access camera " + mSelectedCameraId);
        }
    }
}
