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
package com.tourmaline.example;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.tourmaline.context.ActivityEvent;
import com.tourmaline.context.ActivityListener;
import com.tourmaline.context.ActivityManager;
import com.tourmaline.context.CompletionListener;
import com.tourmaline.context.Engine;
import com.tourmaline.context.Location;
import com.tourmaline.context.LocationListener;
import com.tourmaline.context.LocationManager;
import com.tourmaline.context.TelematicsEvent;
import com.tourmaline.context.TelematicsEventListener;
import com.tourmaline.example.helpers.Alerts;
import com.tourmaline.example.helpers.Monitoring;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class ExampleApplication extends Application {
    private static final String LOG_AREA = "ExampleApplication";

    private static final String ApiKey    = "bdf760a8dbf64e35832c47d8d8dffcc0";
    private static final String user      = "androidexample@tourmalinelabs.com";

    private ActivityListener activityListener;
    private LocationListener locationListener;
    private TelematicsEventListener telematicsListener;

    private boolean gpsEnable=true;
    public boolean isGpsEnable() {
        return gpsEnable;
    }
    private boolean locationPermissionGranted=true;
    public boolean isLocationPermissionGranted() {
        return locationPermissionGranted;
    }
    private boolean powerSavingEnable=false;
    public boolean isPowerSavingEnable() {
        return powerSavingEnable;
    }
    private boolean sdkUpToDate=true;
    public boolean isSdkUpToDate() {
        return sdkUpToDate;
    }


    // initEngine() is invoked in 2 cases:
    // - When the Start Monitoring Button in the MainActivity is clicked for the
    // first time, to launch the engine,
    // - When Engine.INIT_REQUIRED is triggered by the LocalBroadcastManager.
    // This situation corresponds to the case where the application has quit
    // (force quit, device reboot...) and the Engine need to restart so it must
    // be initialized again.
    public void initEngine(final boolean automaticMonitoring,
                           final CompletionListener completionListener) {

        //TLKit is a foreground service: here we set what is displayed into the
        // device notification area

        final String NOTIF_CHANNEL_ID = "background-run-notif-channel-id";
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            final NotificationChannel channel = new NotificationChannel(NOTIF_CHANNEL_ID, getText(R.string.foreground_notification_content_text), NotificationManager.IMPORTANCE_NONE);
            channel.setShowBadge(false);
            if(notificationManager!=null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        final Notification note = new NotificationCompat.Builder(this, NOTIF_CHANNEL_ID)
                .setContentTitle(getText(R.string.app_name))
                .setContentText(getText(R.string.foreground_notification_content_text))
                .setSmallIcon(R.mipmap.ic_foreground_notification)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .build();


        String hashedUserId = HashId( user );
        Engine.Init( getApplicationContext(),
                     ApiKey,
                     hashedUserId,
                     automaticMonitoring,
                     note,
                     completionListener );

    }

    public void destroyEngine(final CompletionListener completionListener) {
        if(activityListener!=null)  {
            ActivityManager.UnregisterDriveListener(activityListener);
            activityListener = null;
        }
        if(telematicsListener!=null)  {
            ActivityManager.UnregisterTelematicsEventListener(telematicsListener);
            telematicsListener = null;
        }
        if(locationListener!=null)  {
            LocationManager.UnregisterLocationListener(locationListener);
            locationListener = null;
        }
        Engine.Destroy(getApplicationContext(), completionListener);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Want to attach the lifecycle broadcast listener to the application
        // context since that is the only context guaranteed to last for full
        // application lifetime
        final LocalBroadcastManager mgr = LocalBroadcastManager.getInstance(this);
        mgr.registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent i) {
                        int state = i.getIntExtra("state", Engine.INIT_SUCCESS);
                        switch (state) {
                            case Engine.INIT_SUCCESS: {
                                Log.i(LOG_AREA, "ENGINE INIT SUCCESS");
                                registerActivityListener();
                                registerLocationListener();
                                registerTelematicsListener();
                                break;
                            }
                            case Engine.INIT_REQUIRED: {
                                Log.i(LOG_AREA, "ENGINE INIT REQUIRED: Engine " +
                                        "needs to restart in background...");
                                final Monitoring.State monitoringState =
                                        Monitoring.getState(getApplicationContext());
                                final CompletionListener listener = new CompletionListener() {
                                    @Override
                                    public void OnSuccess() {
                                    }

                                    @Override
                                    public void OnFail(int i, String s) {
                                    }
                                };
                                switch (monitoringState) {
                                    case AUTOMATIC:
                                        initEngine(true, listener);
                                        break;
                                    case MANUAL:
                                        initEngine(false, listener);
                                        break;
                                    default:
                                        break;
                                }
                                break;
                            }
                            case Engine.INIT_FAILURE: {
                                final String msg = i.getStringExtra("message");
                                final int reason = i.getIntExtra("reason", 0);
                                Log.e(LOG_AREA, "ENGINE INIT FAILURE" + reason + ": " + msg);
                                break;
                            }
                            case Engine.GPS_ENABLED: {
                                Log.i(LOG_AREA, "GPS_ENABLED");
                                gpsEnable = true;
                                Alerts.hide(getApplicationContext(), Alerts.Type.GPS);
                                break;
                            }
                            case Engine.GPS_DISABLED: {
                                Log.i(LOG_AREA, "GPS_DISABLED");
                                gpsEnable = false;
                                Alerts.show(getApplicationContext(), Alerts.Type.GPS);
                                break;
                            }
                            case Engine.LOCATION_PERMISSION_GRANTED: {
                                Log.i(LOG_AREA, "LOCATION_PERMISSION_GRANTED");
                                //since there is no Android callback for this state, the SDK will be informed
                                //only when it needs to get a new location, then it can take several
                                //minutes for the notification to disappear
                                locationPermissionGranted = true;
                                Alerts.hide(getApplicationContext(), Alerts.Type.PERMISSION);
                                break;
                            }
                            case Engine.LOCATION_PERMISSION_DENIED: {
                                Log.i(LOG_AREA, "LOCATION_PERMISSION_DENIED");
                                //In that case Android OS restart the app automatically
                                locationPermissionGranted = false;
                                Alerts.show(getApplicationContext(), Alerts.Type.PERMISSION);
                                break;
                            }
                            case Engine.POWER_SAVE_MODE_DISABLED: {
                                Log.i(LOG_AREA, "POWER_SAVE_MODE_DISABLED");
                                powerSavingEnable=false;
                                Alerts.hide(getApplicationContext(), Alerts.Type.POWER);
                                break;
                            }
                            case Engine.POWER_SAVE_MODE_ENABLED: {
                                Log.i(LOG_AREA, "POWER_SAVE_MODE_ENABLED");
                                powerSavingEnable = true;
                                Alerts.show(getApplicationContext(), Alerts.Type.POWER);
                                break;
                            }
                            case Engine.SDK_UP_TO_DATE: {
                                Log.i(LOG_AREA, "SDK_UP_TO_DATE");
                                sdkUpToDate = true;
                                break;
                            }
                            case Engine.SDK_UPDATE_MANDATORY: {
                                Log.i(LOG_AREA, "SDK_UPDATE_MANDATORY");
                                sdkUpToDate = false;
                                break;
                            }
                            case Engine.SDK_UPDATE_AVAILABLE: {
                                Log.i(LOG_AREA, "SDK_UPDATE_AVAILABLE");
                                sdkUpToDate = false;
                                break;
                            }
                        }
                    }
                },
                new IntentFilter(Engine.ACTION_LIFECYCLE));
    }

    //Drive monitoring
    private void registerActivityListener() {
        activityListener = new ActivityListener() {
            @Override
            public void OnEvent(ActivityEvent activityEvent) {
                Log.i(LOG_AREA, "Activity Listener: new event");
            }

            @Override
            public void RegisterSucceeded() {
                Log.i(LOG_AREA, "Activity Listener: register success");
            }

            @Override
            public void RegisterFailed(int i) {
                Log.e(LOG_AREA, "Activity Listener: register failure");
            }
        };
        ActivityManager.RegisterDriveListener(activityListener);
    }

    //Telematics monitoring
    private void registerTelematicsListener() {
        telematicsListener = new TelematicsEventListener() {
            @Override
            public void OnEvent(TelematicsEvent e) {
                Log.d( LOG_AREA, "Got telematics event: " + e.getTripId() +
                        ", " + e.getTime() + ", " + e.getDuration() );
            }

            @Override
            public void RegisterSucceeded() {
                Log.d(LOG_AREA, "startTelematicsListener OK");
            }

            @Override
            public void RegisterFailed(int i) {
                Log.d(LOG_AREA, "startTelematicsListener KO: " + i);
            }
        };
        ActivityManager.RegisterTelematicsEventListener(telematicsListener);
    }

    //Location monitoring
    private void registerLocationListener() {
        locationListener = new LocationListener() {
            @Override
            public void OnLocationUpdated(Location location) {
                Log.i(LOG_AREA, "Location Listener: new location");
            }

            @Override
            public void RegisterSucceeded() {
                Log.i(LOG_AREA, "Location Listener: register success");
            }

            @Override
            public void RegisterFailed(int i) {
                Log.e(LOG_AREA, "Location Listener: register failure");
            }
        };
        LocationManager.RegisterLocationListener(locationListener);
    }

    /**
     * Calculate the SHA256 digest of a string and return hexadecimal string
     * representation of digest.
     *
     * @param str String to be digested.
     * @return String digest as a hexadecimal string
     */
    private String HashId(String str){
        String result = "";
        try {
            final MessageDigest digester = MessageDigest.getInstance("SHA-256");
            digester.reset();
            byte[] dig = digester.digest(str.getBytes());
            result = String.format("%0" + (dig.length*2) + "X", new BigInteger( 1, dig) ).toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            Log.e( LOG_AREA, "No SHA 256 wtf");
        }
        return  result;
    }

}
