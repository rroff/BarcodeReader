/*
 * Copyright(c) 2015 Ron Roff
 * All Rights Reserved.
 *
 * Author: Ron Roff (rroff@roff.us)
 * Creation Date: 9/11/2015
 */
package rroff.roff.us.barcodereader.camera;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.util.Log;

public class CameraUtility {
    private static final String LOG_TAG = CameraUtility.class.getSimpleName();

    public static final int NO_CAMERA = -1;

    public static boolean isCameraPresent(Context context) {
        boolean hasCamera = false;
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            hasCamera = true;
        }
        return hasCamera;
    }

    public static int getActiveCamera() {
        return NO_CAMERA;
    }

    public static Camera.CameraInfo[] getCameraArray(Context context) {
        Camera.CameraInfo[] cameraArray = null;

        if (isCameraPresent(context)) {
            int numCameras = Camera.getNumberOfCameras();
            if (numCameras > 0) {
                cameraArray = new Camera.CameraInfo[numCameras];
                for (int ii=0; ii<numCameras; ++ii) {
                    cameraArray[ii] = new Camera.CameraInfo();
                    Camera.getCameraInfo(ii, cameraArray[ii]);
                }
            }
        }

        return cameraArray;
    }

    public static Camera getCameraInstance(Context context, int cameraId) {
        Camera camera = null;

        if (  (isCameraPresent(context))
           && (cameraId >= 0) && (cameraId < Camera.getNumberOfCameras()) ) {
            try {
                camera = Camera.open(cameraId);
                Log.d(LOG_TAG, "Camera " + cameraId + " is connected");
            } catch (Exception e) {
                Log.e(LOG_TAG, "Unable to access camera " + cameraId
                        + ": " + e.getMessage());
            }
        }

        return camera;
    }
}
