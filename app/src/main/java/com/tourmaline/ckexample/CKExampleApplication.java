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
package com.tourmaline.ckexample;

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

public class CKExampleApplication extends Application {
    private final static String TAG = "CKExampleApp";

    private final static String ApiKey   = "bdf760a8dbf64e35832c47d8d8dffcc0";

    public void StartEngine() {
        Engine.Init( this,
                     ApiKey,
                     new DefaultAuthMgr( this,
                                        ApiKey,
                                        "example@tourmalinelabs.com",
                                        "password"),
                     new CompletionListener() {
                         @Override
                         public void OnSuccess() { Engine.Monitoring(true); }

                         @Override
                         public void OnFail( int i, String s ) {}
                     } );
    }
    @Override
    public void onCreate() {
        super.onCreate();
        // Want to attach the lifecycle broadcast listener to the application
        // context since that is the only context guaranteed to last for full
        // application lifetime
        LocalBroadcastManager mgr = LocalBroadcastManager.getInstance(this);
        mgr.registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent i) {
                        int state = i.getIntExtra("state", Engine.INIT_SUCCESS);
                        if( state == Engine.INIT_SUCCESS) {
                            Log.w(TAG, "Registering listeners on eng start");
                        } else if (state == Engine.INIT_REQUIRED) {
                            Log.i( TAG,"Engine is trying to restart.");
                            StartEngine( );
                        } else if (state == Engine.INIT_FAILURE) {
                            final String msg = i.getStringExtra("message");
                            final int reason = i.getIntExtra("reason", 0);
                            Log.w(TAG, "Eng start failed eng w/ reason " + reason + ": " + msg);
                        }
                    }
                },
                new IntentFilter(Engine.ACTION_LIFECYCLE));
        StartEngine(  );
    }


}
