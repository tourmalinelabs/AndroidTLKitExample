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
it consists to modify your `build.gradle` file as follows.

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
    compile ("com.tourmalinelabs.android:TLKit:7.0.17032801@aar") { transitive=true }
}
```
*The transitive directive allows your project to automatically add the TLKIT own dependencies.*

## 2 / Add user permissions 

Add the following the following permissions to the `Manifest.xml`.

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
...
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
    <uses-permission android:name="android.permission.WAKE_LOCK"/>
....
</manifest>
```

# Using TLKit

The heart of the TLKit is the Context Engine. The engine needs to 
be initialized with a registered user in order to use any of its 
features. 

## Registering and authenticating users.

TLKit needs to be initialized in order to use any of its features and
starting TLKit requires passing an `AuthMgr` instance to the engine
which handles authenticating against the TL Server.

In a production environment authentication should be done between the
Application Server and the TLKit server. This will prevent the API
key from being leaked out as part of SSL proxying attack on the mobile 
device. See the Data Services api on how to register and authenticate a 
user.
 
For initial integration and evaluation purposes or for applications that do not 
have a server component we the `DefaultAuthMgr` class which will provide 
registration and authentication services for the TL Server.

Initialization with the `DefaultAuthMgr` is covered in the next section.

## Initializing and destroying the engine

An example of initializing the engine with the `DefaultAuthMgr` is provided here:
    
```java
Engine.Init(getApplicationContext(),
            ApiKey,
            new DefaultAuthMgr("example@tourmalinelabs.com",
                               "password"),
            new CompletionListener() {
                @Override
                public void OnSuccess() { Engine.Monitoring(true); }
                @Override
                public void OnFail( int i, String s ) {}
            });

```

Once initialized there is no reason to destroy the `Engine` unless you need to 
set a new `AuthMgr` for a different user or password. In those cases, the 
engine can be destroyed as follows:

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
### Listening for Engine state changes

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
                        if( state == Engine.INIT_SUCCESS) {
                            Log.i(TAG, "ENGINE INIT SUCCESS");
                        } else if (state == Engine.INIT_REQUIRED) {
                            Log.i( TAG,"ENGINE INIT REQUIRED: Engine needs to restart in background...");
                            Engine.Init( getApplicationContext(),
                                         ApiKey,
                                         new DefaultAuthMgr("example@tourmalinelabs.com", "password"),
                                         new CompletionListener() {
                                            @Override
                                            public void OnSuccess() { Engine.Monitoring(true); }
                                            @Override
                                            public void OnFail( int i, String s ) {}
                                         });
                        } else if (state == Engine.INIT_FAILURE) {
                            final String msg = i.getStringExtra("message");
                            final int reason = i.getIntExtra("reason", 0);
                            Log.e(TAG, "ENGINE INIT FAILURE" + reason + ": " + msg);
                        }
                    }
                },
                new IntentFilter(Engine.ACTION_LIFECYCLE));
    }
```

## Monitoring API

By default monitoring is disabled when the Engine is initialized. It needs to be
explicitly enabled to track drives and locations. Enabling is done as follows:
                    
```java
Engine.Monitoring( true );
```

Monitoring can be disabled at any time as follows

```java
Engine.Monitoring( false );
```

If monitoring is enabled at any point during a drive that drive will be recorded.

## Drive monitoring API

Listeners can be registered to receive Drive events. Note: They 
will only receive these events when monitoring is enabled. There is also an API for querying past drives.

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

Note: They will only receive these events when monitoring is enabled. There is also an API for querying past locations.

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

Note: This will only include locations that were recorded when monitoring was enabled.

