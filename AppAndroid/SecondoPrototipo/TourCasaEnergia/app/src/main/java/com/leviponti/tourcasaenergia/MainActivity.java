package com.leviponti.tourcasaenergia;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;
import com.sensoro.beacon.kit.Beacon;
import com.sensoro.beacon.kit.BeaconManagerListener;
import com.sensoro.cloud.SensoroManager;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity implements WebFragment.OnFragmentInteractionListener {

    private WebView webView;
    private SensoroManager sensoroManager;
    private BeaconManagerListener beaconManagerListener;
    public String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.INTERNET,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

                    }
                })
                .check();

        SensoroManager sensoroManager = SensoroManager.getInstance(getApplicationContext());


        /**
         * Check whether the Bluetooth is on
         **/
        if (!sensoroManager.isBluetoothEnabled()) {
            Intent bluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(bluetoothIntent, RESULT_OK);
        }

        /**
         * Enable cloud service (upload sensor data, including battery status, UMM, etc.)。Without setup, it keeps in closed status as default.
         **/
        sensoroManager.setCloudServiceEnable(true);

        /**
         * Enable SDK service
         **/


        beaconManagerListener=new BeaconManagerListener() {
            @Override
            public void onNewBeacon(Beacon beacon) {
                //Toast.makeText(getApplicationContext(),"Beacon rilevato",Toast.LENGTH_LONG).show();
                WebFragment webFragment=WebFragment.newInstance(beacon.getEddystoneURL());
                if(beacon.getEddystoneURL()!=null && webFragment.webView!=null)
                    webFragment.webView.loadUrl(beacon.getEddystoneURL());

                Log.d("onNewBeacon", "url: "+beacon.getEddystoneURL());




            }

            @Override
            public void onGoneBeacon(Beacon beacon) {
                //Toast.makeText(getApplicationContext(),"Uscito dal raggio del beacon",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onUpdateBeacon(ArrayList<Beacon> arrayList) {

            }
        };

        sensoroManager.setBeaconManagerListener(beaconManagerListener);

        showToast("init sensore");
        try {
            sensoroManager.startService();

            showToast("Servizio aperto");
        } catch (Exception e) {
            e.printStackTrace(); // Fetch abnormal info

        }

    }

    public static void setUrl(String url){

    }

    private void showToast(String s){
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}


