# TLKit

This document contains a quick start guide for integrating TLKit
into your Android application. More detailed documentation can be found in the
[Online documentation](http://docs.api.tl/android/) and
[API docs](http://docs.api.tl/android/api/).

# Sample Project
Checkout our sample project
[AndroidTLKitExample](https://github.com/tourmalinelabs/AndroidTLKitExample) for
a simple working example of how developers can use TLKit.

# Integrating TLKit into a project

## 1 / Add the TLKit library
Modify the project  `build.gradle` file as follows.

##### Add the TLKIT artifact's repository

```groovy
repositories {
    maven{ url 'https://raw.githubusercontent.com/tourmalinelabs/AndroidTLKitSDK/master'}
}
```    
*Add this repository section directly at the top level of the `build.gradle` and not under the `buildscript` section.*

##### Add the TLKit as a dependency.

```groovy
dependencies {
    implementation("com.tourmalinelabs.android:TLKit:17.4.21122400")
}
```

## 2 / Add user permissions

### Manifest Permissions
The following permissions will be automatically imported into your project.
```xml
    <uses-permission android:name="android.permission.WAKE_LOCK"/>
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
    <uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION"/>
    <uses-permission android:name="android.permission.INTERNET"/>
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED"/>
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="com.google.android.gms.permission.ACTIVITY_RECOGNITION" />
    <uses-permission android:name="android.permission.ACTIVITY_RECOGNITION" />

```
Also the SDK uses the following device features:
```xml
    <uses-feature android:name="android.hardware.wifi"/>
    <uses-feature android:name="android.hardware.sensor.accelerometer"/>
    <uses-feature android:name="android.hardware.location.gps"/>
```

### Requesting permissions in app
You have to deal with the system to request permissions for Location and Activity Recognition. Moreover you should ensure the user has switched off battery optimisation settings on his Android device.

# Using TLKit

The heart of the TLKit is the Context Engine. The engine needs to
be initialized with some user information and a drive detection mode in order to
use any of its features.

## User information

There are two types of user information that can be used to initialize the
engine:
  1. A SHA-256 hash of some user id. Currently only hashes of emails are allowed
  but other TL approved ids could be used.
  2. An application specific username and password.

The first type of user info is appropriate for cases where the SDK is used only
for data collection. The second type is useful in cases where the application
wants to access the per user information and provide password protected access
to it to it's users.

## Automatic and manual modes

The engine can be initialized for either automatic drive detection where the SDK
will automatically detect and monitor drives or a manual drive detection where
the SDK will only monitor drives when explicitly told to by the application.

The first mode is useful in cases where the user is not interacting with the
application to start and end drives. While the second mode is useful when the
user will explicitly start and end drives in the application.

## Engine is a foreground service
The engine is an Android foreground service. All foreground services are
permanently displayed in the device notification area. For this reason it needs
to be initialized with a Notification object to inform the user about the
purpose of the application you are building. The notification is created with
the following code and must be given at the engine initialization:

```java
final String NOTIF_CHANNEL_ID = "background-run-notif-channel-id";
if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    final NotificationChannel channel = new NotificationChannel(NOTIF_CHANNEL_ID, getText(R.string.foreground_notification_content_text), NotificationManager.IMPORTANCE_NONE);
    channel.setShowBadge(false);
    if(notificationManager!=null) {
        notificationManager.createNotificationChannel(channel);
    }
}

NotificationInfo note = new NotificationInfo(NOTIF_CHANNEL_ID,
        getString(R.string.app_name),
        getString(R.string.foreground_notification_content_text),
        R.mipmap.ic_foreground_notification);
```

## Example initialization with SHA-256 hash in automatic mode
The below examples demonstrate initialization with just a SHA-256 hash. The
example application provides code for generating this hash.

Once started in this mode the engine is able to automatically detect and record
all drives.

```java
Engine.Init( getApplicationContext(),
             ApiKey,
             HashId( "androidexample@tourmalinelabs.com" ),
             Engine.MonitoringMode.AUTOMATIC,
             note,
             null,
             new CompletionListener() {
                @Override
                public void OnSuccess() {}
                @Override
                public void OnFail( int i, String s ) {}
             );
```

#### Starting a new drive

In manual mode you can start a new drive by calling:

```java
final UUID uuid = ActivityManager.StartManualTrip();
```

When starting a new drive you receive an id in return.

If you use alternatively the following method:

```java
final UUID uuid = ActivityManager.StartSingleManualTrip();
```
to start a manual drive and if a drive is already recording you will get the same former uuid because only one drive can be recorded at a time.

#### Stoping a drive

For stopping a drive you need to call StopManualTrip with the right id.

```java
ActivityManager.StopManualTrip(uuid);
```

Note that until a drive is explicitly stopped it will continue to record data.
Even if the SDK is restarted, it will continue to record data for any trips that
it was recording.

If you use alternatively the following method:

```java
ActivityManager.StopAllManualTrips();
```
all trips that are currently recording are stopped in a raw.

#### Current manual drives

The application can query the list of any drives being required as follows.

```java
final ArrayList<Drive> manualDrives = ActivityManager.ActiveManualDrives();
```

### Destroying the engine

Once initialized there is no reason to destroy the `Engine` unless you need to
set a new `AuthMgr` for a different user or need to switch between manual and
automatic modes. In those cases, the engine can be destroyed as follows:

```java
Engine.Destroy(getApplicationContext(),
               new CompletionListener() {
                    @Override
                    public void OnSuccess() {
                        Log.d(TAG, "Engine destroyed.");
                    }
                    @Override
                    public void OnFail( int i, String s ) {
                        Log.e(TAG, "Engine destruction failed.");
                    }
             });
```

## Listening for Engine state changes

In addition to the completion listeners the engine also locally
broadcasts lifecycle events which can be subscribed to via the
`Engine.ACTION_LIFECYCLE` action as shown below.

It is recommended to use this intent to register any listeners with the engine.
This is because in event of an unexpected application termination the lifecycle
registered listeners will be lost. When the engine automatically
restarts with the app the intent will be the only notification that the engine
is running.

```java
final LocalBroadcastManager mgr = LocalBroadcastManager.getInstance((getApplicationContext());
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
                     break;
                 }
                 case Engine.GPS_DISABLED: {
                     Log.i(LOG_AREA, "GPS_DISABLED");
                     break;
                 }
                 case Engine.LOCATION_PERMISSION_GRANTED: {
                     Log.i(LOG_AREA, "LOCATION_PERMISSION_GRANTED");
                     break;
                 }
                 case Engine.LOCATION_PERMISSION_DENIED: {
                     Log.i(LOG_AREA, "LOCATION_PERMISSION_DENIED");
                     break;
                 }
                 case Engine.ACTIVITY_RECOGNITION_PERMISSION_GRANTED: {
                      Log.i(LOG_AREA, "ACTIVITY_RECOGNITION_PERMISSION_GRANTED");
                      break;
                }
                case Engine.ACTIVITY_RECOGNITION_PERMISSION_DENIED: {
                      Log.i(LOG_AREA, "ACTIVITY_RECOGNITION_PERMISSION_DENIED");
                      break;
                }
                case Engine.POWER_SAVE_MODE_DISABLED: {
                     Log.i(LOG_AREA, "POWER_SAVE_MODE_DISABLED");
                     break;
                 }
                 case Engine.POWER_SAVE_MODE_ENABLED: {
                     Log.i(LOG_AREA, "POWER_SAVE_MODE_ENABLED");
                     break;
                 }
                 case Engine.BATTERY_OPTIMIZATION_DISABLED: {
                     Log.i(LOG_AREA, "BATTERY_OPTIMIZATION_DISABLED");
                     break;
                 }
                 case Engine.BATTERY_OPTIMIZATION_ENABLED: {
                     Log.i(LOG_AREA, "BATTERY_OPTIMIZATION_ENABLED");
                     break;
                 }
                 case Engine.BATTERY_OPTIMIZATION_UNKNOWN: {
                     Log.i(LOG_AREA, "BATTERY_OPTIMIZATION_UNKNOWN");
                     break;
                 }
                 case Engine.SDK_UP_TO_DATE: {
                     Log.i(LOG_AREA, "SDK_UP_TO_DATE");
                     break;
                 }
                 case Engine.SDK_UPDATE_MANDATORY: {
                     Log.i(LOG_AREA, "SDK_UPDATE_MANDATORY");
                     break;
                 }
                 case Engine.SDK_UPDATE_AVAILABLE: {
                   Log.i(LOG_AREA, "SDK_UPDATE_AVAILABLE");
                   break;
                }
                case Engine.SYNCHRONIZED: {
                   //All records have been processed and sent to the backend
                   Log.i(LOG_AREA, "SYNCHRONIZED");
                }
               }
        },
        new IntentFilter(Engine.ACTION_LIFECYCLE));
    }
```

The SDK will not be able to monitor your drives or your location updates in 3 cases:
- the GPS is off or,
- the location permissions are not granted or,
- the power saving mode is enabled

In order to handle it correctly in your specific integration we provide the following events: GPS_ENABLED, GPS_DISABLED, LOCATION_PERMISSION_GRANTED, LOCATION_PERMISSION_DENIED, POWER_SAVE_MODE_DISABLED, POWER_SAVE_MODE_ENABLED. Those events will only be received after the SDK starting.

The transition from LOCATION_PERMISSION_DENIED to LOCATION_PERMISSION_GRANTED is the only transition which is not driven by an instantaneous Android OS callback. That's why you will observe a few minutes delay for this specific transition after restoring the location permission from the device settings.

SDK_UP_TO_DATE, SDK_UPDATE_MANDATORY, SDK_UPDATE_AVAILABLE will inform the broadcast receiver about the update status of the running TLKit compared to the last releases.

## Drive monitoring API

Listeners can be registered to receive Drive events.

###  Registering for drive events

Register a drive listener can be done as follows:

```java
ActivityListener listener = new ActivityListener() {
    @Override
    public void OnEvent( ActivityEvent e ) {
    	Log.d("ActivityListener", "Activity event received: " + e );
    }

    @Override
    public void RegisterSucceeded() {
    	Log.d("ActivityListener", "registered!" );
    }

    @Override
    public void RegisterFailed( int e ) {
    	Log.e("ActivityListener", "register failed w/reason " + reason + " :)" );
    }
};
ActivityManager.RegisterDriveListener(listener);
```        

Multiple listeners can be registered via this API and all listeners will
received the same events.

_Note:_ Multiple events may be received for the same drive as the drive
progresses and the drive processing updates the drive with more accurate
map points.

To stop receiving drive monitoring unregister the listener as follows:

```java
ActivityManager.UnregisterDriveListener(listener);
```      

### Querying drive history

Some amount of drives are available for querying as follows.

```java
ActivityManager.GetDrives( new Date(0L),
                           new Date(),
                           20,
                           new QueryHandler<ArrayList<Drive>>() {
    @Override
    public void Result( ArrayList<Drive> drives ) {
        Log.d("DriveMonitor", "Recorded drives:");
            for (Drive drive  : drives) {
                Log.d("DriveMonitor", drive.toString());
            }
    }

    @Override
    public void OnFail( int i, String s ) {
        Log.e("DriveMonitor", "Query failed with err: " + i );
    }
});
```

## Location monotioring API

TLKit provides lower power location updates than traditional GPS
only solutions.

### Registering for location updates

A listener can be registered as follows.

```java
LocationListener listener = new LocationListener() {
    @Override
    public void OnLocationUpdated (Location l) {
        Log.d("Location listener", "Location received: " + l );
    }

    @Override
    public void RegisterSucceeded () {
        Log.d("Location listener", "registered! ");

    }

    @Override
    public void RegisterFailed (int reason) {
        Log.e("Location listener",
              "register failed w/reason " + reason + " :)"  );
    }
};
LocationManager.RegisterLocationListener(listener);
```

A listener can be unregistered as follows:

```java
LocationManager.UnregisterLocationListener(listener);
```


### Querying location history

TLKit provides the ability to query past locations via
`QueryLocations` method of the Engine. These can be used as follows:

```java
LocationManager.QueryLocations(0L, Long.MAX_VALUE, 20, new QueryHandler<ArrayList<Location>>() {
            @Override
            public void Result(ArrayList<Location> locations) {
              Log.d( "QueryLocations", "Recorded locations" );
        			for( Location location: locations ) {
            			Log.d("QueryLocations", location.toString() );
        			}
            }

            @Override
            public void OnFail(int i, String s) {
				          Log.e("QueryLocations", "Query failed with err: " + i );
            }
        });
```

## Telematics monotoring API

TLKit provides the possibility to monitor telematics events occuring during a drive.

### Registering for telematics updates

A listener can be registered as follows.

```java
TelematicsEventListener telematicsListener = new TelematicsEventListener() {
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
```

A listener can be unregistered as follows:

```java
ActivityManager.UregisterTelematicsEventListener(telematicsListener);
```


### Querying telematics for a specific drive

TLKit provides the ability to query drive related telematics via  
`GetTripTelematicsEvents` method of the Engine. You just need to provide the drive Id. These can be used as follows:

```java
ActivityManager.GetTripTelematicsEvents("acae3146-1f64-4f0d-9cfa-3c56f0c0cf68", new QueryHandler<ArrayList<TelematicsEvent>>() {
            @Override
            public void Result(ArrayList<TelematicsEvent> res) {}

            @Override
            public void OnFail(int code, String message) {}
        });
```
