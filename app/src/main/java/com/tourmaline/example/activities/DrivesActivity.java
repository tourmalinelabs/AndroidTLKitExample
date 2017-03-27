/* ******************************************************************************
 * Copyright 2016 Tourmaline Labs, Inc. All rights reserved.
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
package com.tourmaline.example.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.tourmaline.context.ActivityEvent;
import com.tourmaline.context.ActivityListener;
import com.tourmaline.context.ActivityManager;
import com.tourmaline.context.Drive;
import com.tourmaline.context.QueryHandler;
import com.tourmaline.example.adapters.ListAdapter;
import com.tourmaline.example.R;

import java.util.ArrayList;
import java.util.Date;

public class DrivesActivity extends Activity {
    private static final String LOG_AREA = "DrivesActivity";

    private ActivityListener activityListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drives);
        updateDrives();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerDriveListener();
    }

    @Override
    protected void onPause() {
        unregisterDriveListener();
        super.onPause();
    }

    private void registerDriveListener() {
        if(activityListener==null) {
            activityListener = new ActivityListener() {
                @Override
                public void OnEvent( ActivityEvent activityEvent ) {
                    Log.i(LOG_AREA, "Activity Listener: new event");
                    if(activityEvent.Type() == activityEvent.ACTIVITY_FINALIZED) {
                        updateDrives();
                    }
                }

                @Override
                public void RegisterSucceeded() {
                    Log.i(LOG_AREA, "Activity Listener: register success");
                }

                @Override
                public void RegisterFailed( int i ) {
                    Log.e(LOG_AREA, "Activity Listener: register failure");
                }
            };
        }
        ActivityManager.RegisterDriveListener(activityListener);
    }

    private void unregisterDriveListener() {
        if(activityListener!=null) {
            ActivityManager.UnregisterDriveListener(activityListener);
            activityListener = null;
        }
    }

    private void updateDrives() {
        final ListView drivesListView = (ListView) findViewById(R.id.drives_list);
        final TextView noDataTextView = (TextView) findViewById(R.id.no_data_text_view);

        final QueryHandler<ArrayList<Drive>> queryHandler = new QueryHandler<ArrayList<Drive>>() {
            @Override
            public void Result( ArrayList<Drive> drives ) {
                if (drives != null && !drives.isEmpty()) {
                    Log.i(LOG_AREA, "Recorded drives:");
                    final ArrayList<String> list = new ArrayList<>();
                    for (Drive d : drives) {
                        Log.i(LOG_AREA, d.toString());
                        list.add(d.toString());
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ListAdapter adapter =
                                    new ListAdapter(DrivesActivity.this,
                                            android.R.layout.simple_list_item_1,
                                            list);
                            drivesListView.setAdapter(adapter);
                            noDataTextView.setVisibility(View.GONE);
                            drivesListView.setVisibility(View.VISIBLE);
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            noDataTextView.setVisibility(View.VISIBLE);
                            drivesListView.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            }

            @Override
            public void OnFail( int i, String s ) {
                Log.e(LOG_AREA, "Query failed with err: " + i);

            }
        };

        ActivityManager.GetDrives(new Date( 0L), new Date(), queryHandler);
    }
}
