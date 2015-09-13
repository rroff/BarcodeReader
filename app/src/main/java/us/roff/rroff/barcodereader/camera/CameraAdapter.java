/*
 * Copyright(c) 2015 Ron Roff
 * All Rights Reserved.
 *
 * Author: Ron Roff (rroff@roff.us)
 * Creation Date: 9/11/2015
 */
package us.roff.rroff.barcodereader.camera;

import android.content.Context;
import android.hardware.Camera;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import us.roff.rroff.barcodereader.R;

public class CameraAdapter extends ArrayAdapter<Camera.CameraInfo> {

    public CameraAdapter(Context context) {
        super(context, R.layout.list_item_cameras);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        // Create view if it does not exist
        if (v == null) {
            LayoutInflater vi = (LayoutInflater)getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.list_item_cameras, parent, false);
        }

        // Load data from array into view fields
        if (position < getCount()) {
            TextView trackNameTv = (TextView) v.findViewById(R.id.list_item_camera_name);

            String cameraName = position + " - "
                    + (getItem(position).facing == Camera.CameraInfo.CAMERA_FACING_FRONT ? "Front" : "Back");
            trackNameTv.setText(cameraName);
        }

        return v;
    }
}
