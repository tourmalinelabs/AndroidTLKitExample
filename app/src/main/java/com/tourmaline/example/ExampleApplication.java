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
package com.tourmaline.example;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.tourmaline.apis.TLKit;
import com.tourmaline.apis.TLLocationManager;
import com.tourmaline.apis.listeners.TLAuthenticationListener;
import com.tourmaline.apis.listeners.TLCompletionListener;
import com.tourmaline.apis.listeners.TLDeviceCapabilityListener;
import com.tourmaline.apis.listeners.TLKitDestroyListener;
import com.tourmaline.apis.listeners.TLKitInitListener;
import com.tourmaline.apis.listeners.TLKitSyncListener;
import com.tourmaline.apis.listeners.TLLocationListener;
import com.tourmaline.apis.objects.TLCloudArea;
import com.tourmaline.apis.objects.TLDeviceCapability;
import com.tourmaline.apis.objects.TLKitInitResult;
import com.tourmaline.apis.objects.TLLaunchOptions;
import com.tourmaline.apis.objects.TLLocation;
import com.tourmaline.apis.objects.TLMonitoringMode;
import com.tourmaline.apis.objects.TLNotificationInfo;
import com.tourmaline.apis.util.TLDigest;
import com.tourmaline.apis.util.auth.TLAuthenticationResult;

public class ExampleApplication extends Application {
    private static final String LOG_AREA = "ExampleApplication";

    // The apiKey provided to you by Tourmo
    private static final String apiKey = "bdf760a8dbf64e35832c47d8d8dffcc0";
    // Preferences to store TLKit initialization state in case of app restart (reboot, update, crash...)
    private SharedPreferences preferences;
    final private String SHOULD_RESTART_TLKIT_AT_LAUNCH_KEY = "SHOULD_RESTART_TLKIT_AT_LAUNCH_KEY";
    private boolean shouldRestartTLKitAtLaunch() { return preferences.getBoolean(SHOULD_RESTART_TLKIT_AT_LAUNCH_KEY, false); }
    private void setShouldRestartTLKitAtLaunch(boolean started) { preferences.edit().putBoolean(SHOULD_RESTART_TLKIT_AT_LAUNCH_KEY, started).apply(); }
    // Convenience for dispatching information into the main Activity
    private final MutableLiveData<Boolean> tlKitInitialized = new MutableLiveData<>();
    private final MutableLiveData<TLAuthenticationResult> authenticationResult = new MutableLiveData<>();
    public LiveData<Boolean> isTLKitInitialized()  { return tlKitInitialized; }
    public LiveData<TLAuthenticationResult> getAuthenticationResult() { return authenticationResult; }
    private TLLocationListener locationListener;

    private TLNotificationInfo tlkitNotificationInfo;
    private TLAuthenticationListener tlkitAuthListener;
    private TLCompletionListener tlkitCompletionListener;

    @Override
    public void onCreate() {
        super.onCreate();

        //Convenience
        preferences = getSharedPreferences("tourmo", Context.MODE_PRIVATE);
        tlKitInitialized.postValue(false);
        authenticationResult.postValue(new TLAuthenticationResult(TLAuthenticationResult.State.none, ""));

        // Call this function whenever the app is created
        TLKit.OnApplicationCreate(getApplicationContext(),
                new TLKitInitListener() {
                    @Override public void onEngineInit(TLKitInitResult result) {
                        // Initialization is already handled in the TLCompletionListener of the TLKit.Init()
                    }
                }, new TLDeviceCapabilityListener() {
                    @Override public void onCapabilityUpdate(TLDeviceCapability capability) {
                        if (capability.locationPermissionGranted) {
                            Log.i(LOG_AREA, "locationPermissionGranted true");
                            if (capability.gpsEnabled) {
                                Log.i(LOG_AREA, "gpsEnabled true");
                            } else {
                                Log.e(LOG_AREA, "gpsEnabled false");
                            }
                        } else {
                            Log.e(LOG_AREA, "locationPermissionGranted false");
                        }
                        if (capability.activityPermissionGranted) {
                            Log.i(LOG_AREA, "activityPermissionGranted true");
                        } else {
                            Log.e(LOG_AREA, "activityPermissionGranted false");
                        }
                        if (capability.powerSavingEnabled) {
                            Log.e(LOG_AREA, "powerSavingEnabled true");
                        } else {
                            Log.i(LOG_AREA, "powerSavingEnabled false");
                        }
                        if (capability.batteryOptimisationEnabled) {
                            Log.e(LOG_AREA, "batteryOptimisationEnabled true");
                        } else {
                            Log.i(LOG_AREA, "batteryOptimisationEnabled false");
                        }
                    }
                }, new TLKitSyncListener() {
                    @Override public void onEngineSynchronized() {
                        //All records have been processed and sent to our infrastructure
                        Log.i(LOG_AREA, "TLKit is synchronized");
                    }
                });

        // That is very important to call TLKit.Init(...) as quickly as possible to ensure that the
        // keep alive mechanism is effectively set before Android OS decides to kill the app.
        // You must not defer the initialization because you need a network response or you wait another thread to dispatch some information.
        // You must call TLKit.Init(...) synchronously in the onCreate() method of the Application class.
        // It is the integrator responsibility to know if TLKit was previously running.
        if(shouldRestartTLKitAtLaunch()) {
            initTLKit();
        }
    }

    public void initTLKit() {

        if (TLKit.IsInitialized()) {
            return;
        }

        // TLKit is a foreground service, it means there is a permanent notification displayed on the device,
        // here we set what is displayed
        final String NOTIF_CHANNEL_ID = "background-run-notif-channel-id";
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel(NOTIF_CHANNEL_ID,
                    getText(R.string.foreground_notification_channel_title),
                    NotificationManager.IMPORTANCE_LOW);
            channel.setShowBadge(false);
            if(notificationManager!=null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
        tlkitNotificationInfo = new TLNotificationInfo(NOTIF_CHANNEL_ID,
                getString(R.string.app_name),
                getString(R.string.foreground_notification_content_text),
                R.mipmap.ic_foreground_notification);

        tlkitAuthListener = new TLAuthenticationListener() {
            @Override public void OnUpdateState(TLAuthenticationResult result) {
                authenticationResult.postValue(result);
                switch (result.state) {
                    case none:
                        Log.i(LOG_AREA, "TLKit authentication none");
                        break;
                    case authenticated:
                        Log.i(LOG_AREA, "TLKit authentication authenticated");
                        break;
                    case pwd_expired:
                        Log.e(LOG_AREA, "TLKit authentication pwd_expired");
                        break;
                    case invalid_credentials:
                        Log.e(LOG_AREA, "TLKit authentication invalid_credentials");
                        break;
                    case user_disabled:
                        Log.e(LOG_AREA, "TLKit authentication user_disabled");
                        break;
                    case unactivated:
                        Log.e(LOG_AREA, "TLKit authentication unactivated");
                        break;
                    default:
                        Log.e(LOG_AREA, "TLKit authentication error: " + result.message);
                        break;
                }
            }
        };

        tlkitCompletionListener = new TLCompletionListener() {
            @Override public void OnSuccess() {
                Log.i(LOG_AREA, "TLKit Init() success");
                setShouldRestartTLKitAtLaunch(true);
                tlKitInitialized.postValue(true);
                startLocationListener();
            }
            @Override public void OnFail(int i, String s) {
                Log.e(LOG_AREA, "TLKit Init() error: " + i + " - "+ s);
            }
        };

        initTLKitWithHashedId();
        //initTLKitWithHashedIdJoinGroupAndSetVehicle();
        //initTLKitForDriverInstanceWithHashedIdJoinGroupAndSetVehicle();
        //initTLKitWithUsernameAndPassword();
    }

    public void initTLKitWithHashedIdJoinGroup() {
        String externalId = "BC-88329";
        String hashedId = TLDigest.Sha256(externalId);
        TLLaunchOptions options = new TLLaunchOptions();
        options.setExternalId(externalId);
        int orgId = 123;
        String groupExternalId = "team_blue";
        options.addGroupExternalIds(orgId, new String[]{groupExternalId});

        TLKit.Init(getApplicationContext(),
                apiKey,
                TLCloudArea.US,
                hashedId,
                tlkitAuthListener,
                TLMonitoringMode.AUTOMATIC,
                tlkitNotificationInfo,
                options,
                tlkitCompletionListener);
    }

    public void initTLKitWithHashedIdJoinGroupAndSetVehicle() {
        String externalId = "BC-88329";
        String hashedId = TLDigest.Sha256(externalId);
        TLLaunchOptions options = new TLLaunchOptions();
        options.setExternalId(externalId);
        int orgId = 123;
        String groupExternalId = "team_blue";
        options.addGroupExternalIds(orgId, new String[]{groupExternalId});
        options.addVehicle("vehicle-identifier-xyz", "231-4R-12");

        TLKit.Init(getApplicationContext(),
                apiKey,
                TLCloudArea.US,
                hashedId,
                tlkitAuthListener,
                TLMonitoringMode.AUTOMATIC,
                tlkitNotificationInfo,
                options,
                tlkitCompletionListener);
    }

    public void initTLKitForDriverInstanceWithHashedIdJoinGroupAndSetVehicle() {
        int orgId = 123;
        String groupExternalId = "B424";
        String externalId = "BC-" + groupExternalId + "-88329";
        String hashedId = TLDigest.Sha256(externalId);
        TLLaunchOptions options = new TLLaunchOptions();
        options.setExternalId(externalId);
        options.addGroupExternalIds(orgId, new String[]{groupExternalId});
        options.addVehicle("vehicle-identifier-xyz", "231-4R-12");

        TLKit.Init(getApplicationContext(),
                apiKey,
                TLCloudArea.US,
                hashedId,
                tlkitAuthListener,
                TLMonitoringMode.AUTOMATIC,
                tlkitNotificationInfo,
                options,
                tlkitCompletionListener);
    }

    public void initTLKitWithUsernameAndPassword() {
        String username = "bob.smith@tourmo.ai";
        String password = "qwerty123";
        TLLaunchOptions options = new TLLaunchOptions();
        options.setFirstName("Bob");
        options.setLastName("Smith");

        TLKit.Init(getApplicationContext(),
                apiKey,
                TLCloudArea.US,
                username,
                password,
                tlkitAuthListener,
                TLMonitoringMode.AUTOMATIC,
                tlkitNotificationInfo,
                options,
                tlkitCompletionListener);
    }

    public void destroyTLKit() {
        stopLocationListener();
        TLKit.Destroy(getApplicationContext(), new TLKitDestroyListener() {
            @Override
            public void OnDestroyed() {
                Log.i(LOG_AREA, "TLKit destroyed");
                setShouldRestartTLKitAtLaunch(false);
                tlKitInitialized.postValue(false);
                authenticationResult.postValue(new TLAuthenticationResult(TLAuthenticationResult.State.none, ""));
            }
        });
    }

    private void startLocationListener() {
        stopLocationListener();
        locationListener = new TLLocationListener() {
            @Override public void OnLocationUpdated(TLLocation location) {
                Log.d(LOG_AREA, "Got location: " + location );
            }
            @Override public void RegisterSucceeded() {
                Log.d(LOG_AREA, "startLocationListener OK");
            }
            @Override public void RegisterFailed(int i) {
                Log.d(LOG_AREA, "startLocationListener KO: " + i);
            }
        };
        TLLocationManager.ListenForLocationEvents(locationListener);
    }

    private void stopLocationListener() {
        if(locationListener!=null) {
            TLLocationManager.StopListeningForLocationEvents(locationListener);
            locationListener = null;
        }
    }
}
