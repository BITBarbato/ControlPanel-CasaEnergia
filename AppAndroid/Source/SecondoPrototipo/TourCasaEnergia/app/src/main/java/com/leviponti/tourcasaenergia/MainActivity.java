package com.leviponti.tourcasaenergia;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.TextView;
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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity{

    private WebView webView;
    private SensoroManager sensoroManager;
    private BeaconManagerListener beaconManagerListener;
    private Beacon currentBeacon;
    private DBClass dbClass;
    private boolean exit;
    public String url;
    private static final int CAMERA_REQUEST = 1888;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        hideSystemUI();



        /**
         * Settaggio impostazioni della web view
         **/
        this.webView=(WebView)findViewById(R.id.web);
        this.webView.getSettings().setJavaScriptEnabled(true);
        this.webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        this.webView.setWebViewClient(new WebViewClient());
        this.webView.setLayerType(View.LAYER_TYPE_HARDWARE,null);


        /*
            Verifica dei permessi dell'applicazione a internet e per la localizzazione
         */
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


        sensoroManager.setBackgroundBetweenScanPeriod(6000);
        sensoroManager.setForegroundBetweenScanPeriod(6000);

        /**
         * Si verifica se il bluetooth è acceso, altrimenti viene richiesto di accenderlo
         **/
        if (!sensoroManager.isBluetoothEnabled()) {
            Intent bluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(bluetoothIntent, RESULT_OK);
        }

        /**
         * Viene abilitato il servizio cloud dei beacon
         **/
        sensoroManager.setCloudServiceEnable(false);

        /**
         * Viene abilitato l'SDK dei beacon Sensoro
         **/
        beaconManagerListener=new BeaconManagerListener() {
            @Override
            public void onNewBeacon(Beacon beacon) {

                Log.e("NEW BEACON FOUND!->",beacon.getEddystoneUID()+" url: "+beacon.getEddystoneURL()+" power transmit: "+beacon.getTransmitPower());

            }

            @Override
            public void onGoneBeacon(Beacon beacon) {

            }

            @Override
            public void onUpdateBeacon(ArrayList<Beacon> arrayList) {
                Collections.sort(arrayList, new Comparator<Beacon>() {
                    @Override
                    public int compare(Beacon o1, Beacon o2) {
                        if(o1.getRssi()>o2.getRssi()){
                            return -1;
                        }else{
                            return 1;
                        }
                    }
                });
                if(arrayList.size()!=0) {
                    Log.e("UPDATE BEACON", "List:");
                    for (Beacon b : arrayList) {
                        Log.e("UPDATE BEACON", "url:" + b.getEddystoneURL() + " rssi:" + b.getRssi());
                    }
                    Beacon b = arrayList.get(0);
                    Log.e("## UPDATE URL BEACON-> ", "UUID:" + b.getProximityUUID() + " url:" + b.getEddystoneURL() + " ##");

                    try {
                        dbClass=new DBClass();
                        String s=dbClass.getBeaconURL(b.getEddystoneURL());
                        dbClass.close();
                        uploadUrl(s);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                }
            }
        };


        sensoroManager.setBeaconManagerListener(beaconManagerListener);



        try {
            /**
             * Viene inizializzato il servizio di scansione dei beacon
             */
            sensoroManager.startService();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     *
     * Metodo che permette di entrare nell'Activity dove verrà effettuata la scansione del codice QR
     *
     * @param view
     */
    public void getQrScanner(View view){
        Intent intent=new Intent(this,QRCodeScannerActivity.class);

        startActivityForResult(intent,CAMERA_REQUEST);
    }

    /**
     *
     * Metodo di utilità per visualizzare  i Toast sull'applicazione
     *
     * @param s Stringa da visualizzare sul Toast
     */
    private void showToast(String s){
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_SHORT).show();
    }

    /**
     *
     * Metodo utilizzato per settare nella webview la url del sito ottenuto dalla scansione dei beacon
     *
     * @param b Beacon scansionato
     */
    /*private void uploadUrl(final Beacon b){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(b.getEddystoneURL()!=null) {
                    if(!b.equals(currentBeacon)) {


                        webView.loadUrl( b.getEddystoneURL());
                        currentBeacon = b;


                    }
                }
            }
        });
    }*/

    private void uploadUrl(final String s){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                    if(!s.equals(url)) {
                        if(s!=null){
                            webView.loadUrl(s);
                            url=s;
                        }else {
                            Log.e("ERR", "URL NULL");
                        }

                    }

            }
        });
    }

    /**
     *
     * Metodo che permette di aggiornare la url del sito della webview in seguito alla scansione del codice QR
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

            hideSystemUI();

            if(requestCode==CAMERA_REQUEST){
                webView.loadUrl(data.getStringExtra("url"));
            }

    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText pass=new EditText(this);
        pass.setVisibility(View.VISIBLE);

        builder.setMessage("Insert password to exit from app")
                .setView(pass)
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if(pass.getText().toString().equals("0000")){
                            MainActivity.this.finish();
                        }
                        else{
                            dialog.cancel();
                        }

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
        //hideSystemUI();
    }

    @Override
    protected void onPause() {
        super.onPause();

        ActivityManager activityManager = (ActivityManager) getApplicationContext()
                .getSystemService(Context.ACTIVITY_SERVICE);

        activityManager.moveTaskToFront(getTaskId(), 0);
    }

    @Override
    protected void onResume() {
        super.onResume();

        hideSystemUI();
    }

    /**
     *
     * Metodo che permette di nascondere la UI di sistema
     *
     */
    private void hideSystemUI() {

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }
}


