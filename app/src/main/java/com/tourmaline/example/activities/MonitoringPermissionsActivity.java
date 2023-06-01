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

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.tourmaline.example.R;

public class MonitoringPermissionsActivity extends FragmentActivity {

    //https://developer.android.com/training/location/permissions

    private static final String TAG = "PermissionsActivity";
    private static final int PERMISSIONS_LOCATION_REQUEST_ACCESS = 1027;
    private static final int PERMISSIONS_LOCATION_REQUEST_BACKGROUND = 1028;
    private static final int PERMISSIONS_ACTIVITY_RECOGNITION_REQUEST = 1029;

    private long requestStartTime = 0;

    public static boolean needsPermissions(@NonNull Context context) {
        return needsLocationAccess(context) ||
                needsLocationBackground(context) ||
                needsPermissionMActivityRecognition(context) ||
                needsRemoveBatteryOptimization(context);
    }

    public static boolean needsLocationAccess(@NonNull Context context) {
        boolean needsPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED;
        Log.d(TAG, "needsLocationAccess: " + needsPermission);
        return needsPermission;
    }

    public static boolean needsLocationBackground(@NonNull Context context) {
        boolean needsPermission;
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            needsPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED;
        } else {
            needsPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED;
        }
        Log.d(TAG, "needsLocationBackground: " + needsPermission);
        return needsPermission;
    }

    public static boolean needsPermissionMActivityRecognition(@NonNull Context context) {
        boolean needsPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED;
        Log.d(TAG, "needsPermissionMActivityRecognition: " + needsPermission);
        return needsPermission;
    }

    public static boolean needsRemoveBatteryOptimization(@NonNull Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        boolean needsPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                !powerManager.isIgnoringBatteryOptimizations(context.getPackageName());
        Log.d(TAG, "needsRemoveBatteryOptimization: " + needsPermission);
        return needsPermission;
    }

    public static void presentBatteryOptimisationSettings(Context context) {
        if(context == null || !needsRemoveBatteryOptimization(context) ) return;

        Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:" + context.getPackageName()));
        if(intent.resolveActivity(context.getPackageManager()) == null) {
            intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
            if(intent.resolveActivity(context.getPackageManager()) == null) {
                return;
            }
        }
        context.startActivity(intent);
    }

    TextView title;
    TextView subtitle;
    TextView locationTitle;
    TextView locationSubtitle;
    Button locationButton;
    TextView backgroundTitle;
    TextView backgroundSubtitle;
    Button backgroundButton;
    TextView recognitionTitle;
    TextView recognitionSubtitle;
    Button recognitionButton;
    TextView batteryTitle;
    TextView batterySubtitle;
    Button batteryButton;
    Button skipButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_monitoring_permissions);

        title = findViewById(R.id.title);
        subtitle = findViewById(R.id.subtitle);
        locationTitle = findViewById(R.id.location_title);
        locationSubtitle = findViewById(R.id.location_subtitle);
        locationButton = findViewById(R.id.location_button);
        backgroundTitle = findViewById(R.id.background_title);
        backgroundSubtitle = findViewById(R.id.background_subtitle);
        backgroundButton = findViewById(R.id.background_button);
        recognitionTitle = findViewById(R.id.recognition_title);
        recognitionSubtitle = findViewById(R.id.recognition_subtitle);
        recognitionButton = findViewById(R.id.recognition_button);
        batteryTitle = findViewById(R.id.battery_title);
        batterySubtitle = findViewById(R.id.battery_subtitle);
        batteryButton = findViewById(R.id.battery_button);
        skipButton = findViewById(R.id.skip_button);

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // No location permissions, No battery permissions
            locationTitle.setVisibility(View.GONE);
            locationSubtitle.setVisibility(View.GONE);
            locationButton.setVisibility(View.GONE);
            batteryTitle.setVisibility(View.GONE);
            batterySubtitle.setVisibility(View.GONE);
            batteryButton.setVisibility(View.GONE);
        } else {
            locationButton.setOnClickListener(v -> {
                requestStartTime = System.currentTimeMillis();
                ActivityCompat.requestPermissions(MonitoringPermissionsActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_LOCATION_REQUEST_ACCESS);
            });
            batteryButton.setOnClickListener(v -> presentBatteryOptimisationSettings(MonitoringPermissionsActivity.this));
        }

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            // No background permissions, No activity permissions
            backgroundTitle.setVisibility(View.GONE);
            backgroundSubtitle.setVisibility(View.GONE);
            backgroundButton.setVisibility(View.GONE);
            recognitionTitle.setVisibility(View.GONE);
            recognitionSubtitle.setVisibility(View.GONE);
            recognitionButton.setVisibility(View.GONE);
        } else {
            backgroundButton.setOnClickListener(v -> {
                requestStartTime = System.currentTimeMillis();
                ActivityCompat.requestPermissions(MonitoringPermissionsActivity.this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, PERMISSIONS_LOCATION_REQUEST_BACKGROUND);
            });
            recognitionButton.setOnClickListener(v -> {
                requestStartTime = System.currentTimeMillis();
                ActivityCompat.requestPermissions(MonitoringPermissionsActivity.this, new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, PERMISSIONS_ACTIVITY_RECOGNITION_REQUEST);
            });
        }

        skipButton.setOnClickListener(v -> closeActivity(RESULT_CANCELED));

        locationSubtitle.setVisibility(View.GONE);
        backgroundSubtitle.setVisibility(View.GONE);
        recognitionSubtitle.setVisibility(View.GONE);
        batterySubtitle.setVisibility(View.GONE);

        locationTitle.setOnClickListener(v -> locationSubtitle.setVisibility((locationSubtitle.getVisibility()==View.GONE)?View.VISIBLE:View.GONE));
        backgroundTitle.setOnClickListener(v -> backgroundSubtitle.setVisibility((backgroundSubtitle.getVisibility()==View.GONE)?View.VISIBLE:View.GONE));
        recognitionTitle.setOnClickListener(v -> recognitionSubtitle.setVisibility((recognitionSubtitle.getVisibility()==View.GONE)?View.VISIBLE:View.GONE));
        batteryTitle.setOnClickListener(v -> batterySubtitle.setVisibility((batterySubtitle.getVisibility()==View.GONE)?View.VISIBLE:View.GONE));
    }

    @Override
    protected void onResume() {
        super.onResume();

        int red = ContextCompat.getColor(this, R.color.red);
        int blue = ContextCompat.getColor(this, R.color.green);

        if(needsLocationAccess(this)) {
            locationButton.setText(R.string.monitoring_permission_location_button_grant);
            locationButton.setTextColor(red);
            locationButton.setClickable(true);
        } else {
            locationButton.setText(R.string.monitoring_permission_location_button_granted);
            locationButton.setTextColor(blue);
            locationButton.setClickable(false);
        }

        if(needsLocationBackground(this)) {
            backgroundButton.setText(R.string.monitoring_permission_background_button_grant);
            backgroundButton.setTextColor(red);
            backgroundButton.setClickable(true);
        } else {
            backgroundButton.setText(R.string.monitoring_permission_background_button_granted);
            backgroundButton.setTextColor(blue);
            backgroundButton.setClickable(false);
        }

        if(needsPermissionMActivityRecognition(this)) {
            recognitionButton.setText(R.string.monitoring_permission_recognition_button_grant);
            recognitionButton.setTextColor(red);
            recognitionButton.setClickable(true);
        } else {
            recognitionButton.setText(R.string.monitoring_permission_recognition_button_granted);
            recognitionButton.setTextColor(blue);
            recognitionButton.setClickable(false);
        }

        if(needsRemoveBatteryOptimization(this)) {
            batteryButton.setText(R.string.monitoring_permission_battery_button_unset);
            batteryButton.setTextColor(red);
            batteryButton.setClickable(true);
        } else {
            batteryButton.setText(R.string.monitoring_permission_battery_button_set);
            batteryButton.setTextColor(blue);
            batteryButton.setClickable(false);
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        boolean veryShortResponse = System.currentTimeMillis()-requestStartTime<1000;
        if(veryShortResponse) {
            // Very short response means no prompt has been presented to the user,
            // then open the app settings
            if(needsLocationAccess(this)) {
                openAppSettings();
                return;
            }
        }

        if(!needsPermissions(this)) {
            closeActivity(RESULT_OK);
        }
    }

    private void openAppSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setData(Uri.parse("package:" + getPackageName()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_NO_HISTORY|Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        startActivity(intent);
    }

    private void closeActivity(int resultCode) {
        setResult(resultCode);
        finish();
    }
}
