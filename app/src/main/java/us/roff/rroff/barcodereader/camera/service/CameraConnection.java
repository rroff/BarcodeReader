/*
 * Copyright(c) 2015 Ron Roff
 * All Rights Reserved.
 *
 * Author: Ron Roff (rroff@roff.us)
 * Creation Date: 9/13/2015
 */
package us.roff.rroff.barcodereader.camera.service;

public interface CameraConnection {
    public void onCameraConnected();
    public void onCameraDisconnected();
}
