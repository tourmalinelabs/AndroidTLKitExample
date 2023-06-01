/* ******************************************************************************
 * Copyright 2023 Tourmaline Labs, Inc. All rights reserved.
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

import android.content.Intent;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tourmaline.apis.TLKit;
import com.tourmaline.example.ExampleApplication;
import com.tourmaline.example.R;
import com.tourmaline.example.helpers.Progress;

public class MainActivity extends FragmentActivity {

    private TextView tlkitMonitoringTextView;
    private TextView tlkitAuthenticationTextView;
    private TextView alertLocationTextView;
    private TextView alertActivityRecognitionTextView;
    private TextView alertBatteryTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_main);

        // permissions
        Button permissionButton = findViewById(R.id.permissions_button);
        alertLocationTextView = findViewById(R.id.alert_location);
        alertActivityRecognitionTextView = findViewById(R.id.alert_activity_recognition);
        alertBatteryTextView = findViewById(R.id.alert_battery);

        // tlkit
        Button startStopButton = findViewById(R.id.start_stop_button);
        TextView tlkitVersionTextView = findViewById(R.id.tlkit_version);
        tlkitMonitoringTextView = findViewById(R.id.tlkit_monitoring_mode);
        tlkitAuthenticationTextView = findViewById(R.id.tlkit_authentication_status);

        //apis
        LinearLayout apiLayout = findViewById(R.id.api_layout);

        permissionButton.setOnClickListener(v -> {
            final Intent intent = new Intent(MainActivity.this, MonitoringPermissionsActivity.class);
            startActivity(intent);
        });

        startStopButton.setOnClickListener(view -> {
            Progress.show(this);
            if(TLKit.IsInitialized()) {
                ((ExampleApplication)getApplication()).destroyTLKit();
            } else {
                ((ExampleApplication)getApplication()).initTLKit();
            }
        });

        tlkitVersionTextView.setText(getResources().getString(R.string.tlkit_version, TLKit.Version()));

        Button locationsButton = findViewById(R.id.locations_button);
        locationsButton.setOnClickListener(view -> {
            final Intent intent = new Intent(MainActivity.this, LocationsActivity.class);
            startActivity(intent);
        });

        Button tripsButton = findViewById(R.id.trips_button);
        tripsButton.setOnClickListener(view -> {
            final Intent intent = new Intent(MainActivity.this, TripsActivity.class);
            startActivity(intent);
        });

        Button telematicsButton = findViewById(R.id.telematics_button);
        telematicsButton.setOnClickListener(view -> {
            final Intent intent = new Intent(MainActivity.this, TelematicsActivity.class);
            startActivity(intent);
        });

        ((ExampleApplication)getApplication()).isTLKitInitialized().observe(this, initialized -> {
            Progress.dismiss(MainActivity.this);
            if(initialized) {
                apiLayout.setVisibility(View.VISIBLE);
                tlkitMonitoringTextView.setVisibility(View.VISIBLE);
                tlkitAuthenticationTextView.setVisibility(View.VISIBLE);
                tlkitMonitoringTextView.setText(getResources().getString(R.string.monitoring_mode, getResources().getString(R.string.automatic_monitoring)));
                startStopButton.setText(R.string.stop_monitoring);
            } else {
                apiLayout.setVisibility(View.GONE);
                tlkitMonitoringTextView.setVisibility(View.GONE);
                tlkitAuthenticationTextView.setVisibility(View.GONE);
                startStopButton.setText(R.string.start_monitoring);
            }
        });

        ((ExampleApplication)getApplication()).getAuthenticationResult().observe(this, result -> {
            int color;
            switch (result.state) {
                case none:
                    color = R.color.black;
                    break;
                case authenticated:
                    color = R.color.green;
                    break;
                default:
                    color = R.color.red;
                    break;
            }
            tlkitAuthenticationTextView.setTextColor(ContextCompat.getColor(this, color));
            tlkitAuthenticationTextView.setText(getResources().getString(R.string.authentication_status, result.state));
        });

    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        showAlertLocation(MonitoringPermissionsActivity.needsLocationAccess(this));
        showAlertActivityRecognition(MonitoringPermissionsActivity.needsPermissionMActivityRecognition(this));
        showAlertBattery(MonitoringPermissionsActivity.needsRemoveBatteryOptimization(this));
    }

    private void showAlertLocation(boolean show) {
        if(show) {
            alertLocationTextView.setText(getResources().getString(R.string.monitoring_permission_location_title) + ": " + getResources().getString(R.string.monitoring_permission_not_granted));
            alertLocationTextView.setTextColor(getResources().getColor(R.color.red));
        } else {
            alertLocationTextView.setText(getResources().getString(R.string.monitoring_permission_location_title) + ": " + getResources().getString(R.string.monitoring_permission_granted));
            alertLocationTextView.setTextColor(getResources().getColor(R.color.green));
        }
    }

    private void showAlertActivityRecognition(boolean show) {
        if(show) {
            alertActivityRecognitionTextView.setText(getResources().getString(R.string.monitoring_permission_recognition_title) + ": " + getResources().getString(R.string.monitoring_permission_not_granted));
            alertActivityRecognitionTextView.setTextColor(getResources().getColor(R.color.red));
        } else {
            alertActivityRecognitionTextView.setText(getResources().getString(R.string.monitoring_permission_recognition_title) + ": " + getResources().getString(R.string.monitoring_permission_granted));
            alertActivityRecognitionTextView.setTextColor(getResources().getColor(R.color.green));
        }
    }

    private void showAlertBattery(boolean show) {
        if(show) {
            alertBatteryTextView.setText(getResources().getString(R.string.monitoring_permission_battery_title) + ": " + getResources().getString(R.string.monitoring_permission_not_granted));
            alertBatteryTextView.setTextColor(getResources().getColor(R.color.red));
        } else {
            alertBatteryTextView.setText(getResources().getString(R.string.monitoring_permission_battery_title) + ": " + getResources().getString(R.string.monitoring_permission_granted));
            alertBatteryTextView.setTextColor(getResources().getColor(R.color.green));
        }
    }
}
