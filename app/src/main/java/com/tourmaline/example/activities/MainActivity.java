/* ******************************************************************************
 * Copyright 2017 Tourmaline Labs, Inc. All rights reserved.
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

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

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
import com.tourmaline.example.helpers.Monitoring;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";

    private static final int PERMISSIONS_REQUEST = 210;
    private LinearLayout apiLayout;
    private TextView engStateTextView;
    private Button startAutomaticButton;
    private Button startManualButton;
    private Button stopButton;
    private LinearLayout alertLayout;
    private TextView alertGpsTextView;
    private TextView alertLocationTextView;
    private TextView alertMotionTextView;
    private TextView alertPowerTextView;
    private TextView alertSdkUpToDateTextView;
    private Monitoring.State targetMonitoringState;

    private boolean paused = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_main);

        apiLayout = findViewById(R.id.api_layout);
        engStateTextView = findViewById(R.id.engine_State);
        startAutomaticButton = findViewById(R.id.start_button_automatic);
        startManualButton = findViewById(R.id.start_button_manual);
        stopButton = findViewById(R.id.stop_button);
        alertLayout = findViewById(R.id.alert_layout);
        alertGpsTextView = findViewById(R.id.alert_gps);
        alertLocationTextView = findViewById(R.id.alert_location);
        alertMotionTextView = findViewById(R.id.alert_motion);
        alertPowerTextView = findViewById(R.id.alert_power);
        alertSdkUpToDateTextView = findViewById(R.id.alert_sdk);
        startAutomaticButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tryToStartMonitoring(Monitoring.State.AUTOMATIC);
            }
        });

        startManualButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tryToStartMonitoring(Monitoring.State.MANUAL);
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopMonitoring();
            }
        });

        final Button locationsButton = findViewById(R.id.locations_button);
        locationsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent intent = new Intent(MainActivity.this, LocationsActivity.class);
                startActivity(intent);
            }
        });

        final Button drivesButton = findViewById(R.id.drives_button);
        drivesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent intent = new Intent(MainActivity.this, DrivesActivity.class);
                startActivity(intent);
            }
        });

        final Button telematicsButton = findViewById(R.id.telematics_button);
        telematicsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent intent = new Intent(MainActivity.this, TelematicsActivity.class);
                startActivity(intent);
            }
        });

        registerEngineAlerts();

        final Monitoring.State monitoring = Monitoring.getState(getApplicationContext());
        makeUIChangesOnEngineMonitoring(monitoring);
        tryToStartMonitoring(monitoring);
    }

    @Override
    protected void onResume() {
        super.onResume();
        paused = false;
        setAlerts();
    }

    @Override
    protected void onPause() {
        paused = true;
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        unregisterEngineAlerts();
        super.onDestroy();
    }

    private void tryToStartMonitoring(final Monitoring.State monitoring) {

        if(monitoring == Monitoring.State.STOPPED) {
            stopMonitoring();
            return;
        }

        final int googlePlayStat = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if (googlePlayStat == ConnectionResult.SUCCESS) { //check GooglePlayServices
            targetMonitoringState = monitoring;
            String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION, Manifest.permission.ACTIVITY_RECOGNITION};
            }
            ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_REQUEST);
        } else {
            Log.i(TAG, "Google play status is " + googlePlayStat);
            stopMonitoring();
            GooglePlayServicesUtil.showErrorDialogFragment(googlePlayStat, this, null, 0, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {}
            });
        }
    }

    private void startMonitoring(final Monitoring.State monitoring) {

        if(monitoring == Monitoring.State.STOPPED) {
            stopMonitoring();
            return;
        }

        if (!Engine.IsInitialized()) { //check Engine State
            ((ExampleApplication)getApplication()).initEngine((monitoring==Monitoring.State.AUTOMATIC), new CompletionListener() {
                @Override
                public void OnSuccess() {
                    makeUIChangesOnEngineMonitoring(monitoring);
                    Monitoring.setState(getApplicationContext(), monitoring);
                }

                @Override
                public void OnFail(int i, String s) {
                    Toast.makeText(MainActivity.this, "Error starting the Engine: " + i + " -> " + s, Toast.LENGTH_LONG).show();
                    stopMonitoring();
                }
            });
        } else {
            final Monitoring.State currentMonitoring = Monitoring.getState(getApplicationContext());
            if(currentMonitoring==monitoring) {
                makeUIChangesOnEngineMonitoring(monitoring);
            } else {
                makeUIChangesOnEngineMonitoring(currentMonitoring);
                Toast.makeText(MainActivity.this, "Error can't switch monitoring state without stopping ", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void stopMonitoring() {
        ((ExampleApplication)getApplication()).destroyEngine(new CompletionListener() {
            @Override
            public void OnSuccess() {
                makeUIChangesOnEngineMonitoring(Monitoring.State.STOPPED);
                Monitoring.setState(getApplicationContext(), Monitoring.State.STOPPED);
            }
            @Override
            public void OnFail(int i, String s) {
                Toast.makeText(MainActivity.this, "Error destroying the Engine: " + i + " -> " + s, Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean permissionGranted(@NonNull String[] permissions, @NonNull int[] grantResults) {
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == PERMISSIONS_REQUEST) {
            if(permissionGranted(permissions, grantResults) ) {
                Log.i( TAG, "Permissions granted");
            } else {
                Log.i( TAG, "Permissions missing");
            }
            startMonitoring(targetMonitoringState);
        }
    }

    private void makeUIChangesOnEngineMonitoring(final Monitoring.State monitoring) {
        runOnUiThread( new Runnable() {
            @Override
            public void run() {
                switch (monitoring) {
                case STOPPED: {
                    apiLayout.setVisibility(View.GONE);
                    alertLayout.setVisibility(View.GONE);
                    engStateTextView.setText(getResources().getString(R.string.not_monitoring));
                    startAutomaticButton.setEnabled(true);
                    startManualButton.setEnabled(true);
                    stopButton.setEnabled(false);
                    break;
                }
                case AUTOMATIC: {
                    apiLayout.setVisibility(View.VISIBLE);
                    alertLayout.setVisibility(View.VISIBLE);
                    engStateTextView.setText(getResources().getString(R.string.automatic_monitoring));
                    startAutomaticButton.setEnabled(false);
                    startManualButton.setEnabled(false);
                    stopButton.setEnabled(true);
                    break;
                }
                case MANUAL: {
                    apiLayout.setVisibility(View.VISIBLE);
                    alertLayout.setVisibility(View.VISIBLE);
                    engStateTextView.setText(getResources().getString(R.string.manual_monitoring));
                    startAutomaticButton.setEnabled(false);
                    startManualButton.setEnabled(false);
                    stopButton.setEnabled(true);
                    Toast.makeText(MainActivity.this,
                                   "No drive will be detected until started by you! (click on DRIVES)", Toast.LENGTH_LONG).show();
                    break;
                }
                }
            }
        } );

    }

    private BroadcastReceiver receiver;

    private void setAlerts() {
        if(paused) return;
        final ExampleApplication app = (ExampleApplication) getApplication();
        showAlertGps(!app.isGpsEnable());
        showAlertLocation(!app.isLocationPermissionGranted());
        showAlertMotion(!app.isActivityRecognitionPermissionGranted());
        showAlertPower(app.isPowerSavingEnable());
        showAlertSdkUpToDate(!app.isSdkUpToDate());
    }

    private void registerEngineAlerts() {
        final LocalBroadcastManager mgr = LocalBroadcastManager.getInstance(this);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent i) {
                int state = i.getIntExtra("state", Engine.INIT_SUCCESS);
                switch (state) {
                    case Engine.GPS_ENABLED:
                    case Engine.GPS_DISABLED:
                    case Engine.LOCATION_PERMISSION_GRANTED:
                    case Engine.LOCATION_PERMISSION_DENIED:
                    case Engine.ACTIVITY_RECOGNITION_PERMISSION_GRANTED:
                    case Engine.ACTIVITY_RECOGNITION_PERMISSION_DENIED:
                    case Engine.POWER_SAVE_MODE_DISABLED:
                    case Engine.POWER_SAVE_MODE_ENABLED:
                    case Engine.SDK_UP_TO_DATE:
                    case Engine.SDK_UPDATE_AVAILABLE:
                    case Engine.SDK_UPDATE_MANDATORY:
                        { setAlerts();  break; }
                    default: break;
                }
                setAlerts();
            }
        };
        mgr.registerReceiver(receiver, new IntentFilter(Engine.ACTION_LIFECYCLE));
    }

    private void unregisterEngineAlerts() {
        if(receiver!=null) {
            final LocalBroadcastManager mgr = LocalBroadcastManager.getInstance(this);
            mgr.unregisterReceiver(receiver);
        }
    }

    private void showAlertGps(boolean show) {
        if(show) {
            alertGpsTextView.setText("GPS *** OFF");
            alertGpsTextView.setTextColor(getResources().getColor(R.color.red));
        } else {
            alertGpsTextView.setText("GPS *** ON");
            alertGpsTextView.setTextColor(getResources().getColor(R.color.blue));
        }
    }

    private void showAlertLocation(boolean show) {
        if(show) {
            alertLocationTextView.setText("Location permission *** OFF");
            alertLocationTextView.setTextColor(getResources().getColor(R.color.red));
        } else {
            alertLocationTextView.setText("Location permission *** ON");
            alertLocationTextView.setTextColor(getResources().getColor(R.color.blue));
        }
    }

    private void showAlertMotion(boolean show) {
        if(show) {
            alertMotionTextView.setText("Motion permission *** OFF");
            alertMotionTextView.setTextColor(getResources().getColor(R.color.red));
        } else {
            alertMotionTextView.setText("Motion permission *** ON");
            alertMotionTextView.setTextColor(getResources().getColor(R.color.blue));
        }
    }

    private void showAlertPower(boolean show) {
        if(show) {
            alertPowerTextView.setText("Power saving mode *** ON");
            alertPowerTextView.setTextColor(getResources().getColor(R.color.red));
        } else {
            alertPowerTextView.setText("Power saving mode *** OFF");
            alertPowerTextView.setTextColor(getResources().getColor(R.color.blue));
        }
    }

    private void showAlertSdkUpToDate(boolean show) {
        if(show) {
            alertSdkUpToDateTextView.setText("SDK up to date *** NO");
            alertSdkUpToDateTextView.setTextColor(getResources().getColor(R.color.red));
        } else {
            alertSdkUpToDateTextView.setText("SDK up to date *** YES");
            alertSdkUpToDateTextView.setTextColor(getResources().getColor(R.color.blue));
        }
    }
}
