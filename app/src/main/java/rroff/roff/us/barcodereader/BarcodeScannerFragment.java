/*
 * Copyright(c) 2015 Ron Roff
 * All Rights Reserved.
 *
 * Author: Ron Roff (rroff@roff.us)
 * Creation Date: 9/4/2015
 */
package rroff.roff.us.barcodereader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import rroff.roff.us.barcodereader.camera.CameraAdapter;
import rroff.roff.us.barcodereader.camera.CameraPreview;
import rroff.roff.us.barcodereader.camera.CameraUtility;

public class BarcodeScannerFragment extends Fragment {

    private final String LOG_TAG = BarcodeScannerFragment.class.getSimpleName();

    private Camera mCamera;

    private CameraAdapter mCameraAdapter;

    private BarcodeDetector mDetector;

    private ViewHolder mHolder;

    /**
     * Default constructor.
     */
    public BarcodeScannerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCamera = null;
        mDetector = new BarcodeDetector.Builder(getActivity())
                .setBarcodeFormats(Barcode.DATA_MATRIX | Barcode.QR_CODE)
                .build();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_barcode_scanner, container, false);

        mHolder = new ViewHolder();
        mHolder.processBtn = (Button)rootView.findViewById(R.id.button);
        mHolder.barcodeIv = (ImageView)rootView.findViewById(R.id.imgview);
        mHolder.outputTv = (TextView)rootView.findViewById(R.id.txtContent);
        mHolder.cameraSpn = (Spinner)rootView.findViewById(R.id.cameraSpinner);
        mHolder.cameraFrm = (FrameLayout)rootView.findViewById(R.id.cameraFrame);

        mHolder.processBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO
            }
        });

        mCameraAdapter = new CameraAdapter(getActivity());
        mCameraAdapter.addAll(CameraUtility.getCameraArray(getActivity()));
        mHolder.cameraSpn.setAdapter(mCameraAdapter);
        mHolder.cameraSpn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(LOG_TAG, "Camera " + position + " selected");
                changeCamera(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d(LOG_TAG, "No camera selected");
            }
        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        // TODO: Replace static barcode with captured image
        Bitmap bitmap = BitmapFactory.decodeResource(
                getActivity().getResources(),
                R.drawable.puppy);
        // processBarcode(bitmap);
    }

    private void changeCamera(int cameraId) {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }

        if (mCamera == null) {
            mCamera = CameraUtility.getCameraInstance(getActivity(), cameraId);
            if (mCamera != null) {
                mHolder.cameraFrm.addView(new CameraPreview(getActivity(), mCamera));
            } else {
                Log.e(LOG_TAG, "Unable to access camera " + cameraId);
            }
        }
    }

    private void processBarcode(Bitmap bitmap) {
        mHolder.barcodeIv.setImageBitmap(bitmap);

        // NOTE from Google Code Lab: It’s possible that, the first time our barcode detector runs,
        // Google Play Services won’t be ready to process barcodes yet.  So we need to check if our
        // detector is operational before we use it.  If it isn’t, we may have to wait for a
        // download to complete, or let our users know that they need to find an internet
        // connection or clear some space on their device.
        if(!mDetector.isOperational()){
            mHolder.outputTv.setText("Could not set up the detector!");
            return;
        }

        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        SparseArray<Barcode> barcodes = mDetector.detect(frame);

        Barcode thisCode = barcodes.valueAt(0);
        mHolder.outputTv.setText(thisCode.rawValue);
    }

    private class ViewHolder {
        public Button processBtn;
        public ImageView barcodeIv;
        public TextView outputTv;
        public Spinner cameraSpn;
        public FrameLayout cameraFrm;
    }
}
