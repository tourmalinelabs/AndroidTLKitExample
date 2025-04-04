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
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.tourmaline.apis.TLKit;
import com.tourmaline.apis.listeners.TLAuthenticationListener;
import com.tourmaline.apis.listeners.TLCompletionListener;
import com.tourmaline.apis.listeners.TLDeviceCapabilityListener;
import com.tourmaline.apis.listeners.TLKitDestroyListener;
import com.tourmaline.apis.listeners.TLKitSyncListener;
import com.tourmaline.apis.listeners.TLLocationListener;
import com.tourmaline.apis.objects.TLCloudArea;
import com.tourmaline.apis.objects.TLDeviceCapability;
import com.tourmaline.apis.objects.TLLaunchOptions;
import com.tourmaline.apis.objects.TLLocation;
import com.tourmaline.apis.objects.TLMonitoringMode;
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

    private TLCompletionListener tlCompletionListener;

    @Override
    public void onCreate() {
        super.onCreate();

        preferences = getSharedPreferences("tourmo", Context.MODE_PRIVATE);
        tlKitInitialized.postValue(false);
        authenticationResult.postValue(new TLAuthenticationResult(TLAuthenticationResult.State.none, ""));

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

        tlCompletionListener = new TLCompletionListener() {
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

        initTLKitWithHashedIdJoinGroup();
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
                tlAuthListener,
                TLMonitoringMode.AUTOMATIC,
                options,
                tlDeviceCapListener,
                tlSyncListener,
                tlCompletionListener);
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
                tlAuthListener,
                TLMonitoringMode.AUTOMATIC,
                options,
                tlDeviceCapListener,
                tlSyncListener,
                tlCompletionListener);
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
                tlAuthListener,
                TLMonitoringMode.AUTOMATIC,
                options,
                tlDeviceCapListener,
                tlSyncListener,
                tlCompletionListener);
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
                tlAuthListener,
                TLMonitoringMode.AUTOMATIC,
                options,
                tlDeviceCapListener,
                tlSyncListener,
                tlCompletionListener);
    }

    public void destroyTLKit() {
        stopLocationListener();
        TLKit.Destroy(new TLKitDestroyListener() {
            @Override
            public void OnDestroyed() {
                Log.i(LOG_AREA, "TLKit destroyed");
                setShouldRestartTLKitAtLaunch(false);
                tlKitInitialized.postValue(false);
                authenticationResult.postValue(new TLAuthenticationResult(TLAuthenticationResult.State.none, ""));
            }
        });
    }

    private TLLocationListener locationListener;

    private void startLocationListener() {
        stopLocationListener();
        locationListener = new TLLocationListener() {
            @Override public void OnLocationUpdated(TLLocation location) {}
            @Override public void RegisterSucceeded() {}
            @Override public void RegisterFailed(int reason, String message) {}
        };
        TLKit.TLLocationManager().ListenForLocationEvents(locationListener);
    }

    private void stopLocationListener() {
        if(locationListener!=null) {
            TLKit.TLLocationManager().StopListeningForLocationEvents(locationListener);
            locationListener = null;
        }
    }

    private final TLAuthenticationListener tlAuthListener = new TLAuthenticationListener() {
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

    private final TLDeviceCapabilityListener tlDeviceCapListener = new TLDeviceCapabilityListener() {
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
    };

    private final TLKitSyncListener tlSyncListener = new TLKitSyncListener() {
        @Override public void onEngineSynchronized() {
            //All records have been processed and sent to our infrastructure
            Log.i(LOG_AREA, "TLKit is synchronized");
        }
    };
}
