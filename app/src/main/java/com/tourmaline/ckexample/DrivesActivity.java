/*******************************************************************************
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
package com.tourmaline.ckexample;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.tourmaline.context.ActivityEvent;
import com.tourmaline.context.ActivityListener;
import com.tourmaline.context.ActivityManager;
import com.tourmaline.context.Drive;
import com.tourmaline.context.QueryHandler;

import java.util.ArrayList;
import java.util.Date;

public class DrivesActivity extends Activity implements ActivityListener {


    private final static String LOG_AREA = "DrivesActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_drives);
        ActivityManager.RegisterDriveListener( this );
        final Button stopDriveDetectButton = (Button) findViewById(R.id.stop_drive_detect_button);
        stopDriveDetectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityManager.UnregisterDriveListener(DrivesActivity.this);
                Log.e(LOG_AREA, "Unregistered Drive Event Listener");
                Toast.makeText(DrivesActivity.this, "Unregistered Drive Event Listener",
                        Toast.LENGTH_LONG).show();
                stopDriveDetectButton.setEnabled(false);
            }
        });

        updateDrives();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ActivityManager.UnregisterDriveListener(DrivesActivity.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ActivityListener dummy = new ActivityListener() {
            @Override
            public void OnEvent( ActivityEvent activityEvent ) {

            }

            @Override
            public void RegisterSucceeded() {

            }

            @Override
            public void RegisterFailed( int i ) {

            }
        };
        // Start drive monitoring
        ActivityManager.RegisterDriveListener( dummy );

        // Stopping drive monitoring
        ActivityManager.UnregisterDriveListener( dummy );



    }

    private void updateDrives() {
        final ListView drivesListView =
            (ListView) findViewById(R.id.drives_list);
        final TextView noDataTextView =
            (TextView) findViewById(R.id.no_data_text_view);

        ActivityManager.GetDrives( new Date( 0L),
                                   new Date(),
                                   new QueryHandler<ArrayList<Drive>>() {
            @Override
            public void Result( ArrayList<Drive> drives ) {
                if (drives != null && !drives.isEmpty()) {
                    Log.d(LOG_AREA, "Recorded drives:");
                    final ArrayList<String> list = new ArrayList<>();
                    for (Drive d : drives) {
                        Log.d(LOG_AREA, d.toString());
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
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_drives, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void OnEvent( ActivityEvent activityEvent ) {
        updateDrives();
    }

    @Override
    public void RegisterSucceeded() {

    }

    @Override
    public void RegisterFailed( int i ) {

    }

}
