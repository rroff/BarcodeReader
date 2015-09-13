/*
 * Copyright(c) 2015 Ron Roff
 * All Rights Reserved.
 *
 * Author: Ron Roff (rroff@roff.us)
 * Creation Date: 9/4/2015
 */
package rroff.roff.us.barcodereader;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
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
import rroff.roff.us.barcodereader.camera.service.CameraService;

public class BarcodeScannerFragment extends Fragment {

    private final String LOG_TAG = BarcodeScannerFragment.class.getSimpleName();

    /**
     * PlayerService binding.
     */
    private CameraService mBoundService;

    /**
     * CameraService binding flag.
     */
    private boolean mServiceBound = false;

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

        configureCameraSpinner();
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

        mHolder.processBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO
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

    @Override
    public void onPause() {
        super.onPause();

        // Unbind service
        unbindService();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Bind to service
        Intent intent = new Intent(getActivity(), CameraService.class);
        getActivity().startService(intent);
        getActivity().bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void configureCameraSpinner() {
        mCameraAdapter = new CameraAdapter(getActivity());
        if (mServiceBound) {
            mCameraAdapter.addAll(mBoundService.getCameraArray());
        }
        mHolder.cameraSpn.setAdapter(mCameraAdapter);
    }

    private void changeCamera(int cameraId) {
        if (mServiceBound) {
            mBoundService.changeCamera(cameraId, mHolder.cameraFrm);
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

    /**
     * Unbinds camera service.
     */
    private void unbindService() {
        if (mServiceBound) {
            getActivity().unbindService(mServiceConnection);
            mServiceBound = false;
        }
    }

    private class ViewHolder {
        public Button processBtn;
        public ImageView barcodeIv;
        public TextView outputTv;
        public Spinner cameraSpn;
        public FrameLayout cameraFrm;
    }

    /**
     * Manages connection to camera service.
     */
    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceBound = false;
            Log.d(LOG_TAG, "Disconnected (" + name.getClassName() + ")");
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // Save server binding
            CameraService.CameraBinder cameraBinder = (CameraService.CameraBinder)service;
            mBoundService = cameraBinder.getService();
            mServiceBound = true;
            Log.d(LOG_TAG, "Connected (" + name.getClassName() + ")");

            // Reinitialize camera spinner choices
            configureCameraSpinner();
        }
    };
}
