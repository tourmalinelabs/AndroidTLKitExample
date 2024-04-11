# Migration guide from TLKit 22.x.xyz to TLKit 23.x.xyz

## Init TLKit after an app relaunch
It is now up to the integrator know if TLKit was initialized when the app is launch.
TLKitInitListener has been removed consequently if the app has been killed by the system or after a crash it is the responsibility of the integrator to call ```TlKit.Init()``` in the ```onCreate()``` callback of the application class.

## Remove TLKit.OnApplicationCreate()
The ```TLKit.OnApplicationCreate()``` has been removed and you have to pass the ```TLDeviceCapabilityListener``` and the ```TLKitSyncListener``` in the ```TLKit.Init()``` function.
Those listeners are now optional.

## Foreground service notification
There is no need anymore to provide the notification parameters in the ```TLKit.Init()``` TLKit embbed 3 resources:
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

## Thread safe callbacks
Every time you will receive a callback from the SDK it will be in the same thread you made the request.
Examples: 
- ```TLKit.TLActivityManger().QueryTrips()```
- ```TLKit.TLLocationmanger().ListenForLocationUpdates()```

## Accces to the managers
TLLocationManager, TLActivityManager and TLFleetManger do not contain static method anymore. You will get access to the instances through the TLKit class ```TLKit.TLLocationManager()```, ```TlKit.TLActivityManager()``` and ```TlKit.TLFleetManger()```



# Migration guide from TLKit 17.x.xyz to TLKit 22.x.xyz

## TLKit renaming

The Engine is now called TLKit.
You still have access to the previous functions. For example:

- ```TLKit.IsInitialize();```
- ```TLKit.Init(...);```

## Initialization

Because of TLKit integration, your app will be able to automatically restart in background in different circumstances: a device reboot, an app update, a crash... 
When the app starts the Application class the ```onCreate()``` method is called. It is the integrator responsability to know if TLKit was previously running.
If that was the case you must call ```TLKit.Init(...)``` to reauthenticate the user.
That is very important to call ```TLKit.Init(...)``` as quickly as possible to ensure that the keep alive mechanism is effectively set before Android OS decides to kill the app. 
You must not defer the initialization because you need a network response or you wait another thread to dispatch some information.
You must call ```TLKit.Init(...)``` synchronously in the ```onCreate()``` method of the Application class.

For being able to do that you will probably have to store locally all the starting parameters you need like:
- the user identifier
- the group identifier to join in case you use the TLLaunchOptions
- ...

Here is the new initialization method:

```java
TLKit.Init(getApplicationContext(),
            ApiKey,
            TLCloudArea.US,
            TLDigect.Sha256("androidexample@tourmalinelabs.com"),
            new TLAuthenticationListener() {...},
            TLMonitoringMode.AUTOMATIC,
            notificationInfo,
            tlLaunchOptions,
            new TLCompletionListener() {...});
```

The 2 main differences are:

- You have to specify the TLCloudArea, the default one should always be the US area ```TLCloudArea.US```. If your manifest contains
```xml
<meta-data android:name="com.tourmalinelabs.area" android:value="EU" /> 
```
or if you have been told to do so use the european area ```TLCloudArea.EU``` instead.

- The new ```TLAuthenticationListener``` will allow you to get a feedback about the authentication status. For example if you choose to Initialize TLKit with a username and a password and you provide a wrong password this listener will return an error.

## TL Prefix

All the classes are now prefixed with TL.
For example:

- ```TLNotificationInfo```
- ```TLCompletionListener```
- ```TLLaunchOptions```


## 'Drive' has been replaced by 'Trip'

The word Drive has been banished and replaced by the word Trip in all places.

## Suscribing to specific events

Event subscriptions were made by a call to functions starting with ```RegisterXYZ(Listener l)``` now you have to use ```ListenForXYZ(TLListener l)```. For example:

```java
ActivityManager.RegisterDriveListener(new ActivityListener(){...});
```

is now

```java
TLActivityManager.ListenForTripEvents(new TLActivityListener() {});
```

## Listening for Engine states

LocalBroadcastManager has been deprecated on Android we have then moved the mechanism to receive different Engine States. In the application ```onCreate()``` overrride you have to replace:

```java
@Override
public void onCreate() {
    super.onCreate();

    final LocalBroadcastManager mgr = LocalBroadcastManager.getInstance((getApplicationContext());
    mgr.registerReceiver(
        new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent i) {
               int state = i.getIntExtra("state", Engine.INIT_SUCCESS);
               switch (state) {
                 case Engine.INIT_SUCCESS: 
                 case Engine.INIT_FAILURE: 
                 case Engine.GPS_ENABLED: 
                 case Engine.GPS_DISABLED: 
                 //...
               }
        },
        new IntentFilter(Engine.ACTION_LIFECYCLE));
}
```

by:

```java
public void onCreate() {
    super.onCreate();

    TLKit.OnApplicationCreate(getApplicationContext(),
                new TLKitInitListener() {
                    @Override public void onEngineInit(TLKitInitResult result) {
                        // Here you will get the initialization state after calling TLKit.Init(...)
                        // You can also monitor this state from the TLCompletionListener you pass to the TLKit.Init()
                    }
                }, new TLDeviceCapabilityListener() {
                    @Override public void onCapabilityUpdate(TLDeviceCapability capability) {
                        // Here you will be informed if a permission is missing
                    }
                }, new TLKitSyncListener() {
                    @Override public void onEngineSynchronized() {
                        //All records have been processed and sent to our infrastructure
                    }
                });
}
```


