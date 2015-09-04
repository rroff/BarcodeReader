/*
 * Copyright(c) 2015 Ron Roff
 * All Rights Reserved.
 *
 * Author: Ron Roff (rroff@roff.us)
 * Creation Date: 9/3/2015
 */
package rroff.roff.us.barcodereader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

public class MainActivity extends AppCompatActivity {

    BarcodeDetector mDetector;

    ViewHolder mHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHolder = new ViewHolder();
        mHolder.processBtn = (Button)findViewById(R.id.button);
        mHolder.processBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO
            }
        });
        mHolder.barcodeIv = (ImageView)findViewById(R.id.imgview);
        mHolder.outputTv = (TextView)findViewById(R.id.txtContent);

        mDetector = new BarcodeDetector.Builder(getApplicationContext())
                        .setBarcodeFormats(Barcode.DATA_MATRIX | Barcode.QR_CODE)
                        .build();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // TODO: Replace static barcode with captured image
        Bitmap bitmap = BitmapFactory.decodeResource(
                getApplicationContext().getResources(),
                R.drawable.puppy);
        processBarcode(bitmap);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class ViewHolder {
        public Button processBtn;
        public ImageView barcodeIv;
        public TextView outputTv;
    }
}
