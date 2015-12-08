/*
 * Copyright(c) 2015 Ron Roff
 * All Rights Reserved.
 *
 * Author: Ron Roff (rroff@roff.us)
 * Creation Date: 9/4/2015
 */
package us.roff.rroff.barcodereader;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import us.roff.rroff.barcodereader.camera.CameraPreview;
import us.roff.rroff.barcodereader.camera.CameraUtility;
import us.roff.rroff.barcodereader.camera.service.CameraConnection;
import us.roff.rroff.barcodereader.camera.service.CameraService;

public class BarcodeScannerFragment extends Fragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private final String LOG_TAG = BarcodeScannerFragment.class.getSimpleName();

    /**
     * PlayerService binding.
     */
    private CameraService mBoundService;

    /**
     * CameraService binding flag.
     */
    private boolean mServiceBound = false;

    private ArrayAdapter<String> mCameraAdapter;

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
                .setBarcodeFormats(Barcode.EAN_8 | Barcode.EAN_13 | Barcode.ISBN)
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
                if (mServiceBound) {
                    Camera camera = mBoundService.getActiveCamera();
                    if (camera != null) {
                        camera.takePicture(
                                null,
                                null,
                                new Camera.PictureCallback() {
                                    @Override
                                    public void onPictureTaken(byte[] data, Camera camera) {
                                        if (data != null) {
                                            Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                                            processBarcode(bmp);
                                        } else {
                                            Log.e(LOG_TAG, "Unable to capture image");
                                        }
                                    }
                                });
                    }
                }
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
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        prefs.unregisterOnSharedPreferenceChangeListener(this);

        // Unbind service
        unbindService();

        super.onPause();
    }

    @Override
    public void onResume() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        prefs.registerOnSharedPreferenceChangeListener(this);

        // Bind to service
        Intent intent = new Intent(getActivity(), CameraService.class);
        getActivity().startService(intent);
        getActivity().bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);

        super.onResume();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_active_camera_key))) {
            mHolder.cameraFrm.removeAllViews();

            if (mServiceBound) {
                Camera camera = mBoundService.getActiveCamera();
                if (camera != null) {
                    CameraPreview cameraPreview = new CameraPreview(getActivity(), camera);
                    mHolder.cameraFrm.addView(cameraPreview);
                }
            }
        }
    }

    private void configureCameraSpinner() {
        mCameraAdapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_cameras);
        if (mServiceBound) {
            Camera.CameraInfo[] cameraArray = mBoundService.getCameraArray();

            for (int position = 0; position < cameraArray.length; ++position) {
                String cameraName = position + " - "
                        + (cameraArray[position].facing == Camera.CameraInfo.CAMERA_FACING_FRONT ? "Front" : "Back");
                mCameraAdapter.add(cameraName);
            }
        }
        mHolder.cameraSpn.setAdapter(mCameraAdapter);
    }

    private void changeCamera(int cameraId) {
        if (mServiceBound) {
            mBoundService.changeCamera(cameraId);
        }
    }

    private void processBarcode(Bitmap bitmap) {
        Log.d(LOG_TAG, "Processing captured image");
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

        if (barcodes.size() > 0) {
            Barcode thisCode = barcodes.valueAt(0);
            mHolder.outputTv.setText(thisCode.rawValue);
        } else {
            Log.d(LOG_TAG, "No barcodes found");
        }
    }

    /**
     * Changes value for Active Camera in Shared Preferences in order to wake up listener.
     *
     * @param activeCameraId ID of Active Camera
     */
    private void setActiveCamera(int activeCameraId) {
        SharedPreferences.Editor editor
                = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
        editor.putInt(getString(R.string.pref_active_camera_key), activeCameraId);
        editor.commit();
    }

    /**
     * Unbinds camera service.
     */
    private void unbindService() {
        if (mServiceBound) {
            mBoundService.unregisterCameraListener(mCameraConnection);
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

    private CameraConnection mCameraConnection = new CameraConnection() {
        @Override
        public void onCameraConnected() {
            if (mServiceBound) {
                setActiveCamera(mBoundService.getActiveCameraId());
            }
        }

        @Override
        public void onCameraDisconnected() {
            setActiveCamera(CameraUtility.NO_CAMERA);
        }
    };

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

            setActiveCamera(mBoundService.getActiveCameraId());
            mBoundService.registerCameraListener(mCameraConnection);

            // Reinitialize camera spinner choices
            configureCameraSpinner();
        }
    };
}
