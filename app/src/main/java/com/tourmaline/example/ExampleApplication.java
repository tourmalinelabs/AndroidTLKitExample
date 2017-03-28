/* ******************************************************************************
 * Copyright 2016,2017 Tourmaline Labs, Inc. All rights reserved.
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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.tourmaline.context.CompletionListener;
import com.tourmaline.context.DefaultAuthMgr;
import com.tourmaline.context.Engine;

public class ExampleApplication extends Application {
    private static final String TAG = "ExampleApplication";

    private static final String ApiKey    = "bdf760a8dbf64e35832c47d8d8dffcc0";
    private static final String user      = "example@tourmalinelabs.com";
    private static final String password  = "password";

    // startEngine() is invoked in 2 cases:
    // - The first time the Start Monitoring Button in the MainActivity is clicked, to launch the engine,
    // - When Engine.INIT_REQUIRED is triggered by the LocalBroadcastManager. This situation
    // corresponds to the case where the application has quit (force quit, device reboot...)
    // and the Engine need to restart so it must be initialized again.
    public void startEngine(final CompletionListener completionListener) {

        final Engine.AuthMgr authManager = new DefaultAuthMgr(this, ApiKey, user, password);

        final CompletionListener initListener = new CompletionListener() {
            @Override
            public void OnSuccess() {
                Log.i(TAG, "startEngine OnSuccess -> monitoring:" + Engine.Monitoring());
                if(completionListener!=null) { completionListener.OnSuccess(); }
            }
            @Override
            public void OnFail( int i, String s ) {
                if(completionListener!=null) { completionListener.OnFail(i, s); }
            }
        };

        Engine.Init(this, ApiKey, authManager, initListener);
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
                            Log.i(TAG, "ENGINE INIT SUCCESS");
                        } else if (state == Engine.INIT_REQUIRED) {
                            Log.i( TAG,"ENGINE INIT REQUIRED");
                            startEngine(null);
                        } else if (state == Engine.INIT_FAILURE) {
                            final String msg = i.getStringExtra("message");
                            final int reason = i.getIntExtra("reason", 0);
                            Log.e(TAG, "ENGINE INIT FAILURE" + reason + ": " + msg);
                        }
                    }
                },
                new IntentFilter(Engine.ACTION_LIFECYCLE));
    }
}
