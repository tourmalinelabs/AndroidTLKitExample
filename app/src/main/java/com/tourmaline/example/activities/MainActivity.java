/* ******************************************************************************
 * Copyright 2016, 2017 Tourmaline Labs, Inc. All rights reserved.
 * Confidential & Proprietary - Tourmaline Labs, Inc. ("TLI")
 *
 * The party receiving this software directly from TLI (the "Recipient")
 * may use this software as reasonably necessary solely for the purposes
 * set forth in the agreement between the Recipient and TLI (the
 * "Agreement"). The software may be used in source code form solely by
 * the Recipient's employees (if any) authorized by the Agreement. Unless
 * expressly authorized in the Agreement, the Recipient may not sublicense,
 * assign, transfer or otherwise provide the source code to any third
 * party. Tourmaline Labs, Inc. retains all ownership rights in and
 * to the software
 *
 * This notice supersedes any other TLI notices contained within the software
 * except copyright notices indicating different years of publication for
 * different portions of the software. This notice does not supersede the
 * application of any third party copyright notice to that third party's
 * code.
 ******************************************************************************/
package com.tourmaline.example.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.tourmaline.context.CompletionListener;
import com.tourmaline.context.Engine;
import com.tourmaline.example.ExampleApplication;
import com.tourmaline.example.R;
import com.tourmaline.example.helpers.Preferences;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";

    private static final String AUTO_START_MONITORING = "PrefAutoStartMonitoring";

    private static final int PERMISSIONS_REQUEST = 0;
    private LinearLayout apiLayout;
    private TextView engStateTextView;
    private Button startButton;
    private Button stopButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_main);

        apiLayout = (LinearLayout) findViewById(R.id.api_layout);
        engStateTextView = (TextView) findViewById(R.id.engine_State);
        startButton = (Button) findViewById(R.id.start_button);
        stopButton = (Button) findViewById(R.id.stop_button);

        makeUIChangesOnEngineMonitoring(Engine.Monitoring());

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tryToStartMonitoring();
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopMonitoring();
            }
        });

        final Button locationsButton = (Button) findViewById(R.id.locations_button);
        locationsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent intent = new Intent(MainActivity.this, LocationsActivity.class);
                startActivity(intent);
            }
        });

        final Button drivesButton = (Button) findViewById(R.id.drives_button);
        drivesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent intent = new Intent(MainActivity.this, DrivesActivity.class);
                startActivity(intent);
            }
        });

        if(Preferences.getInstance(getApplicationContext()).getBoolean(AUTO_START_MONITORING, false)) {
            tryToStartMonitoring();
        }
    }

    private void tryToStartMonitoring() {
        final int googlePlayStat = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if (googlePlayStat != ConnectionResult.SUCCESS) { //check GooglePlayServices
            Log.i( TAG, "Google play status is " + googlePlayStat );
            stopMonitoring();
            try {
                GooglePlayServicesUtil.getErrorDialog(googlePlayStat, this, 0).show();
            } catch ( Exception e ) {
                Log.e(TAG, "GooglePlay Service exception:" + e.toString());
            }
        } else {
            final String[] missingPerms = Engine.MissingPermissions(this);
            if (missingPerms.length > 0) { //Check Permissions (Location)
                ActivityCompat.requestPermissions(this, missingPerms, PERMISSIONS_REQUEST);
            } else {
                startEngineAndMonitoring();
            }
        }
    }

    private void startEngineAndMonitoring() {
        boolean isEngineRunning = Engine.IsInitialized();
        if (isEngineRunning) { //check Engine State
           startMonitoring();
        } else {
            ((ExampleApplication)getApplication()).startEngine(new CompletionListener() {
                @Override
                public void OnSuccess() {
                    startMonitoring();
                }

                @Override
                public void OnFail(int i, String s) {
                    Toast.makeText(MainActivity.this, "Error starting the Engine", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void startMonitoring() {
        Engine.Monitoring(true);
        makeUIChangesOnEngineMonitoring(true);
        Preferences.getInstance(getApplicationContext()).putBoolean(AUTO_START_MONITORING, true);
    }

    private void stopMonitoring() {
        Engine.Monitoring(false);
        makeUIChangesOnEngineMonitoring(false);
        Preferences.getInstance(getApplicationContext()).putBoolean(AUTO_START_MONITORING, false);
    }

    private boolean permissionGranted(String permissions[], int[] grantResults) {
        boolean permissionGranted = true;
        for ( int i = 0; i < grantResults.length; ++i ) {
            if( grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "Failed to get grant results for " + permissions[i]);
                permissionGranted = false;
            }
        }
        return permissionGranted;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if(requestCode == PERMISSIONS_REQUEST) {
            if(permissionGranted(permissions, grantResults) ) {
                Log.i( TAG, "All permissions granted");
                startEngineAndMonitoring();
            } else {
                Log.i( TAG, "permissions missing!");
                stopMonitoring();
                Toast.makeText(MainActivity.this, "Permissions missing!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void makeUIChangesOnEngineMonitoring(final boolean monitoring) {
        apiLayout.setVisibility(monitoring?View.VISIBLE:View.GONE);
        engStateTextView.setText(getResources().getString(monitoring?R.string.monitoring:R.string.not_monitoring));
        startButton.setEnabled(!monitoring);
        stopButton.setEnabled(monitoring);

    }
}
