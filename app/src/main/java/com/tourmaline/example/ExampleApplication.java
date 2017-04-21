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
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.tourmaline.context.ActivityEvent;
import com.tourmaline.context.ActivityListener;
import com.tourmaline.context.ActivityManager;
import com.tourmaline.context.CompletionListener;
import com.tourmaline.context.DefaultAuthMgr;
import com.tourmaline.context.Engine;
import com.tourmaline.context.Location;
import com.tourmaline.context.LocationListener;
import com.tourmaline.context.LocationManager;
import com.tourmaline.example.helpers.Monitoring;

public class ExampleApplication extends Application {
    private static final String LOG_AREA = "ExampleApplication";

    private static final String ApiKey    = "bdf760a8dbf64e35832c47d8d8dffcc0";
    private static final String user      = "example@tourmalinelabs.com";
    private static final String password  = "password";

    private ActivityListener activityListener;
    private LocationListener locationListener;

    // initEngine() is invoked in 2 cases:
    // - When the Start Monitoring Button in the MainActivity is clicked for the first time,
    // to launch the engine,
    // - When Engine.INIT_REQUIRED is triggered by the LocalBroadcastManager. This situation
    // corresponds to the case where the application has quit (force quit, device reboot...)
    // and the Engine need to restart so it must be initialized again.
    public void initEngine(final boolean automaticMonitoring, final CompletionListener completionListener) {

        //TLKit is a foreground service: here we set what is displayed into the device notification area
        final Intent notificationIntent = new Intent(this, ExampleApplication.class);
        final PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);
        final Notification note = new Notification.Builder(this)
                .setContentTitle(getText(R.string.app_name))
                .setContentText(getText(R.string.foreground_notification_content_text))
                .setSmallIcon(R.mipmap.ic_foreground_notification)
                .setContentIntent(pendingIntent)
                .build();

        //Authentication
        final Engine.AuthMgr authManager = new DefaultAuthMgr(user, password);

        // Engine Start
        if(automaticMonitoring) {
            Engine.InitAutomatic(getApplicationContext(), note, ApiKey, authManager, completionListener);
        } else {
            Engine.InitManual(getApplicationContext(), note, ApiKey, authManager, completionListener);
        }
    }

    public void destroyEngine(final CompletionListener completionListener) {
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
                        if( state == Engine.INIT_SUCCESS) {
                            Log.i(LOG_AREA, "ENGINE INIT SUCCESS");
                            registerActivityListener();
                            registerLocationListener();
                        } else if (state == Engine.INIT_REQUIRED) {
                            Log.i(LOG_AREA, "ENGINE INIT REQUIRED: Engine needs to restart in background...");
                            final Monitoring.State monitoringState = Monitoring.getState(getApplicationContext());
                            switch (monitoringState) {
                                case AUTOMATIC: initEngine(true, null); break;
                                case MANUAL: initEngine(false, null); break;
                                default: break;
                            }
                        } else if (state == Engine.INIT_FAILURE) {
                            final String msg = i.getStringExtra("message");
                            final int reason = i.getIntExtra("reason", 0);
                            Log.e(LOG_AREA, "ENGINE INIT FAILURE" + reason + ": " + msg);
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

}