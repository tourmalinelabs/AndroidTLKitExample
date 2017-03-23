/*******************************************************************************
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
package com.tourmaline.ckexample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.tourmaline.context.Engine;

public class CKExampleActivity extends Activity {
    private final static String TAG = "CKExampleActivity";
    private static final int PERMISSIONS_REQUEST = 0;
    private LinearLayout apiLayout;
    private TextView engStateTextView;
    private Button startButton;
    private Button stopButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_ckexample);

        apiLayout = (LinearLayout) findViewById(R.id.api_layout);
        engStateTextView = (TextView) findViewById(R.id.engine_State);
        startButton = (Button) findViewById(R.id.start_button);
        stopButton = (Button) findViewById(R.id.stop_button);

        if (Engine.Monitoring()) {
            makeUIChangesOnEngineMonitoring();
        } else {
            makeUIChangesOnEngineNotMonitoring();
        }

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!Engine.IsInitialized()) {
                    Engine.Monitoring(true);
                }
                makeUIChangesOnEngineMonitoring();
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isEngineRunning = Engine.IsInitialized();
                if (isEngineRunning) {
                    Engine.Monitoring( false );
                }
                makeUIChangesOnEngineNotMonitoring();
            }
        });

        Button locationsButton = (Button) findViewById(R.id.locations_button);
        locationsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CKExampleActivity.this, LocationsActivity.class);
                startActivity(intent);
            }
        });

        Button drivesButton = (Button) findViewById(R.id.drives_button);
        drivesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CKExampleActivity.this, DrivesActivity.class);
                startActivity(intent);
            }
        });

        Log.d( TAG, "DrawActivity::Enter");
        int googlePlayStat =
            GoogleApiAvailability.getInstance()
                                 .isGooglePlayServicesAvailable(this);

        String[] missingPerms = Engine.MissingPermissions(this);

        //Log.i( TAG, "Google play status is " + googlePlayStat );
        if (googlePlayStat != ConnectionResult.SUCCESS ) {
            Log.w( TAG, "Google play status is " + googlePlayStat );
            try {
                GooglePlayServicesUtil.getErrorDialog( googlePlayStat, this, 0 )
                                      .show();
            } catch ( Exception e ) {
                e.printStackTrace();
            }
        }
        else if ( missingPerms.length > 0 ){
            ActivityCompat.requestPermissions( this,
                                               missingPerms,
                                               PERMISSIONS_REQUEST);

        }
    }

    private void makeUIChangesOnEngineNotMonitoring() {
        apiLayout.setVisibility(View.GONE);
        engStateTextView.setText(getResources().getString(R.string.not_monitoring ));
        stopButton.setEnabled(false);
        startButton.setEnabled(true);
    }

    private void makeUIChangesOnEngineMonitoring() {
        apiLayout.setVisibility(View.VISIBLE);
        engStateTextView.setText(getResources().getString(R.string.monitoring ));
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_ckexample, menu);
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
}
