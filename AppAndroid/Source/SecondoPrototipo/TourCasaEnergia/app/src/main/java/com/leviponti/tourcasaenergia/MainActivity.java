package com.leviponti.tourcasaenergia;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

public class MainActivity extends AppCompatActivity{

    private WebView webView;
    private SensoroManager sensoroManager;
    private BeaconManagerListener beaconManagerListener;
    private Beacon currentBeacon;
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
        this.webView.setWebViewClient(new WebViewClient());


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
                for(Beacon b:arrayList){
                    Log.e("UPDATE BEACON-> ","UUID:"+b.getProximityUUID()+" url:"+b.getEddystoneURL());
                    uploadUrl(b);
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
     * Metodo che permette di visualizzare sulla webview il sito precedente a quello corrente
     *
     * @param view
     */
    public void getPrevSite(View view){

        if(this.webView.canGoBack()){
            this.webView.loadUrl((webView.copyBackForwardList().getItemAtIndex(webView.copyBackForwardList().getCurrentIndex()-1)).getUrl());
        }

    }

    /**
     *
     * Metodo che permette di visualizzare sulla webview il sito successivo a quello corrente
     *
     * @param view
     */
    public void getForwardSite(View view){
        if(this.webView.canGoForward()){
            this.webView.loadUrl((webView.copyBackForwardList().getItemAtIndex(webView.copyBackForwardList().getCurrentIndex()+1)).getUrl());
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
    private void uploadUrl(final Beacon b){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(!b.equals(currentBeacon) && b.getEddystoneURL()!=null) {
                    if(b.getEddystoneURL()!=webView.getUrl()) {
                        webView.loadUrl(b.getEddystoneURL());
                        currentBeacon = b;
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


