# TLKit

This document contains a quick start guide for integrating TLKit
into your Android application.

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
    implementation("com.tourmalinelabs.android:TLKit:23.2.25040300")
}
```

## 2 / Add user permissions

### Manifest Permissions
The following permissions will be automatically imported into your project by the TLKit library.
```xml
    <uses-permission android:name="android.permission.WAKE_LOCK"/>
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
    <uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION"/>
    <uses-permission android:name="android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS"/>
    <uses-permission android:name="android.permission.INTERNET"/>
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED"/>
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION" />
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
You have to deal with the system to request 4 permissions:
- Location precise 
- Location in background 
- Activity Recognition 
- Battery optimization, you should ensure the user has switched off battery optimisation settings for tour app, you can request that directly within your application.
  
Please refer to the ```MonitoringPermissionsActivity``` class of the AndroidTLKitExample to learn more about it.

# Using TLKit

TLKit needs to be initialized with some user information and a drive detection mode in order to
use any of its features.

## User information

There are two types of user information that can be used to initialize the
TLKit:
  1. A SHA-256 hash of some user id.
  2. An application specific username and password.

The first type of user info is appropriate for cases where the TLKit is used only
for data collection. The second type is useful in cases where the application
wants to access the per user information and provide password protected access to it's users.

## Automatic and manual modes

The TLKit can be initialized for either automatic drive detection where the TLKit
will automatically detect and monitor trips or a manual drive detection where
the TLKit will only monitor trips when explicitly told to by the application.

The first mode is useful in cases where the user is not interacting with the
application to start and end trips. While the second mode is useful when the
user will explicitly start and end trips in the application.

## TLKit is a foreground service

The TLKit is an Android foreground service. All foreground services are
permanently displayed in the device notification area. TLKit embbed 3 default resources:
- the notification icon
- the notification text
- the notification channel title

It is still possible to override those resources:

For overriding the icon you have to provide in your application 6 png files named tlkit_foreground_service_notif_icon.png in the following folders with the correct resolution:
- Drawable         18x18
- Drawable-hdpi    24x24 
- Drawable-mdpi    36x36
- Drawable-xhdpi   48x48
- Drawable-xxhdpi  72x72
- Drawable-xxxhdpi 96x96

For overriding the texts you have to provide the strings in your resource files for the following keys:

```
<string name="tlkit_foreground_service_notif_channel_name">Background monitoring</string>
<string name="tlkit_foreground_service_notif_text">Monitoring your activity</string>
```
Starting Android 13 the foreground service notification will not be displayed unless your app request notification permission to the user and the user allow it.

## TLKit initialization
The below examples demonstrate initialization with a SHA-256 hash.

Once started in the following automatic mode the TLKit is able to automatically detect and record
all trips.

You should create such a ```initTLKit()```method for starting the TLKit because you will need to call it in 2 cases:
- Whenever you need to start TLKit depending on your own app logic (rigth after the app launch, after your user is logged-in to your backend...)
- In the Application ```onCreate()``` after the app has automatically restarted in background (after a reboot, an update, a crash...)

```java

public void initTLKit() {

    if (TLKit.IsInitialized()) {
        return;
    }

     TLKit.Init(getApplicationContext(),
                apiKey,
                TLCloudArea.US,
                TLDigest.Sha256("androidexample@tourmalinelabs.com"),
                new TLAuthenticationListener() {...},
                TLMonitoringMode.AUTOMATIC,
                options,
                new TLDeviceCapabilityListener() {...},
                new TLKitSyncListener() {...},
                new TLCompletionListener() {...});
	}
```

In Automatic mode your app will run all the time and will automatically restart in background in different circumstances: a device reboot, an app update, a crash... 
When the app starts the Application class the ```onCreate()``` method is called. It is the integrator responsability to know if TLKit was previously running.
If that was the case you must call ```TLKit.Init(...)``` to reauthenticate the user.
That is very important to call ```TLKit.Init(...)``` as quickly as possible to ensure that the keep alive mechanism is effectively set before Android OS decides to kill the app. 
You must not defer the initialization because you need a network response or you wait another thread to dispatch some information.
You must call ```TLKit.Init(...)``` synchronously in the ```onCreate()``` method of the Application class.

For being able to do that you will probably have to store locally all the starting parameters you need like:
- was TLKit running?
- the user identifier
- the group identifier to join in case you use the TLLaunchOptions
- ...

### Launch Options

During the Initialization of the TLKit you can set some values which will be stored within your account:
- your first name
- your last name
- your title
- a user externalId of your choice that will be tied to a unique user on your side
- a vehicle made of an externalId (tied to a unique vehicle on your side) and an optional licence plate

You can also automatically join a group by providing:
- an external identifier for that group
- the identifier of the group
- or a token for that group

You only need to instantiate a ```TLLaunchOptions``` object, set the desired options and then pass it to the ```TLKit.Init()``` method.

### Destroying the TLKit

Once initialized there is no reason to destroy the `TLKit` unless you need to stop the monitoring or set a different user. In those cases, the TLKit can be destroyed as follows:

```java
TLKit.Destroy(new TLKitDestroyListener(){...});
```

## Dealing with Permissions and device settings

The TLKit will not be able to monitor your trips or your location updates in different cases:
- the GPS is off
- the location permissions are not fully granted (Location Precise and Location Background)
- activity recognition permissions are not granted
- the battery optimization is enabled for your app (default behaviour)
- the power saving mode is enabled on that device

In order to handle it correctly in your specific integration we provide the ```TLDeviceCapabilityListener``` within the ```TLKit.Init()```.
The ```TLDeviceCapability``` returned by this callback should facilitate the work to inform your user he must act on his device settings. This object contains the status of each permission / settings.

The transition from Location permission denied to location permission granted is the only transition which is not driven by an instantaneous Android OS callback. That's why you will observe a few minutes delay for this specific transition after restoring the location permission from the device settings.

## Manual mode
When the TLKit is started in manual mode you have to start and stop trip monitoring manually, here are the usefull functions:

### Starting a new trip

In manual mode you can start a new trip by calling:

```java
final UUID uuid = TLKit.TLActivityManager().StartManualTrip();
```

When starting a new trip you receive an id in return. You can call this function multiple times to record interleaved trips.

If you use alternatively the following method:

```java
final UUID uuid = TLKit.TLActivityManager().StartSingleManualTrip();
```
to start a manual trip and if a trip is already recording you will get the same former uuid (no interleaved trips).


### Stoping a trip

For stopping a trip you need to call StopManualTrip with the right id.

```java
TLKit.TLActivityManager().StopManualTrip(uuid);
```

Note that until a trip is explicitly stopped it will continue to record data.
Even if the SDK is restarted, it will continue to record data for any trips that
it was recording.

If you use alternatively the following method:

```java
TLKit.TLActivityManager().StopAllManualTrips();
```
all trips that are currently recording are stopped in a raw.

### Current manual trips

The application can query the list of any trips being required as follows.

```java
final ArrayList<TLTrip> trips = TLKit.TLActivityManager().ActiveManualTrips();
```

## Trip monitoring API

Listeners can be registered to receive Trip events.

###  Registering for trip events

Register a trip listener can be done as follows:

```java
TLActivityListener listener = new TLActivityListener(){...};
TLKit.TLActivityManager().ListenForTripEvent(listener);
```        

Multiple listeners can be registered via this API and all listeners will
received the same events.

_Note:_ Multiple events may be received for the same trip as the trip
progresses and the trip processing updates the trip with more accurate
map points.

To stop receiving trip monitoring unregister the listener as follows:

```java
TLKit.TLActivityManager().StopListeningForTripEvent(listener);
```      

### Querying trip history

Past trips are available for querying as follows.

```java
TLKit.TLActivityManager().QueryTrips(new Date(0L),
                        	     new Date(),
                        	     20,
                        	     new TLQueryListener<ArrayList<TLTrip>>() {...});
```

## Location monotioring API

Listeners can be registered to receive Location events.

### Registering for location updates

A listener can be registered as follows.

```java
TLLocationListener listener = new TLLocationListener(){...}
TLKit.TLLocationManager().ListenForLocationEvents(listener);
```

A listener can be unregistered as follows:

```java
TLKit.TLLocationManager().StopListeningForLocationEvents(listener);
```


### Querying location history

TLKit provides the ability to query past locations via
`QueryLocations` method. These can be used as follows:

```java
TLKit.TLLocationManager().QueryLocations(0L, Long.MAX_VALUE, 20, new TLQueryListener<ArrayList<TLLocation>>(){...});
```

## Telematics monotoring API

TLKit provides the possibility to monitor telematics events occuring during a trip.

### Registering for telematics updates

A listener can be registered as follows.

```java
TLTelematicsEventListener listener = new TLTelematicsEventListener() {...};
TLKit.TLActivityManager().ListenForTelematicsEvents(listener);
```

A listener can be unregistered as follows:

```java
TLKit.TLActivityManager().StopListeningForTelematicsEvents(listener);
```


### Querying telematics for a specific trip

TLKit provides the ability to query trip related telematics via  
`QueryTripTelematicsEvents` method of the TLKit. You just need to provide the trip Id. These can be used as follows:

```java
TLKit.TLActivityManager().QueryTripTelematicsEvents("acae3146-1f64-4f0d-9cfa-3c56f0c0cf68", new TLQueryListener<ArrayList<TLTrip>>() {...});
```

# Troubleshooting

As crashes may happen and disturb the activity recording, it is important to monitor the app health by using a crash reporter. To give integrators the choice of tools as well as to avoid dependency conflicts, TLKit does not embed any crash reporting tool into the sdk. We highly recommend that integrators install a third-party crash reporting library such as Firebase Crashlytics, Sentry, etc.

At Tourmaline Labs we are using Firebase Crashlytics in our own apps:
https://firebase.google.com/docs/crashlytics/get-started

Whatever tool you choose you will need to provide symbols files, you can download them for TLKit version xx.y.zzz at https://s3.amazonaws.com/tlkit-android-symbols/TLKit-xx.y.zzz-symbols.zip
(for example https://s3.amazonaws.com/tlkit-android-symbols/TLKit-17.4.21122400-symbols.zip)
