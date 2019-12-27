package com.leviponti.tourcasaenergia;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.altbeacon.beacon.*;

import android.view.View;

import com.leviponti.tourcasaenergia.datamodel.SiteStack;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements BeaconConsumer {

    private BeaconManager beaconManager;
    private WebView webView;
    private SiteStack siteStack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.siteStack=new SiteStack();

        // Se il dispositivo supporta il bluetooth e se è disattivato chiede di attivarlo
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }

        setContentView(R.layout.activity_main);

        //Impostazioni WebView
        this.webView=(WebView) findViewById(R.id.webpage);

        this.webView.getSettings().setJavaScriptEnabled(true);
        this.webView.setWebViewClient(new WebViewClient());

        if(getIntent()!=null) {

            this.webView.loadUrl(getIntent().getStringExtra("message"));

            this.siteStack.addSite(getIntent().getStringExtra("message"));

        }

        //Imposta la modalità immersive
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN);

        beaconManager = BeaconManager.getInstanceForApplication(this);
        // To detect proprietary beacons, you must add a line like below corresponding to your beacon
        // type.  Do a web search for "setBeaconLayout" to get the proper expression.
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.bind(this);
        Toast.makeText(getApplicationContext(), "Start Scan",Toast.LENGTH_LONG).show();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
        Toast.makeText(getApplicationContext(), "Stop Scan",Toast.LENGTH_LONG).show();

    }

    @Override
    public void onBeaconServiceConnect() {

        beaconManager.removeAllMonitorNotifiers();
        beaconManager.addMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                Toast.makeText(getApplicationContext(), "I just saw an beacon for the first time!",Toast.LENGTH_LONG).show();
            }

            @Override
            public void didExitRegion(Region region) {
                Toast.makeText(getApplicationContext(), "I no longer see an beacon",Toast.LENGTH_LONG).show();
            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {
                Toast.makeText(getApplicationContext(), "I have just switched from seeing/not seeing beacons: "+state,Toast.LENGTH_LONG).show();
            }
        });

        try {
            beaconManager.startMonitoringBeaconsInRegion(new Region("Sala1", Identifier.parse("dbffdaea-2570-11ea-978f-2e728ce88125"), null, null));
        } catch (RemoteException e) {
            Toast.makeText(getApplicationContext(), "Error while monitoring beacon in region",Toast.LENGTH_LONG).show();
        }

    }

    public void scanQr(View view){

        Intent intent = new Intent(this, QRCodeScannerActivity.class);

        startActivity(intent);

        finish();

    }

    public void getNextSite(View view){

        String site=this.siteStack.getNext();
        if(site!=null){
            this.webView.loadUrl(site);
        }

    }

    public void getPrevSite(View view){

        String site=this.siteStack.getPrev();
        if(site!=null){
            this.webView.loadUrl(site);
        }

    }

    @Override
     protected void onSaveInstanceState(Bundle outState) {
          super.onSaveInstanceState(outState);
          outState.putStringArrayList("siteStack",new ArrayList<String>(this.siteStack.getSites()));

     }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);
        this.siteStack =new SiteStack(savedInstanceState.getStringArrayList("siteStack"));
    }
}
