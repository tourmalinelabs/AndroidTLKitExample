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
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.tourmaline.context.ActivityEvent;
import com.tourmaline.context.ActivityListener;
import com.tourmaline.context.ActivityManager;
import com.tourmaline.context.Drive;
import com.tourmaline.context.Point;
import com.tourmaline.context.QueryHandler;
import com.tourmaline.example.adapters.ListAdapter;
import com.tourmaline.example.R;
import com.tourmaline.example.helpers.Progress;

import java.util.ArrayList;
import java.util.Calendar;
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
                    if(activityEvent.Type() == ActivityEvent.ACTIVITY_FINALIZED) {
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
        Progress.show(this);

        final QueryHandler<ArrayList<Drive>> queryHandler = new QueryHandler<ArrayList<Drive>>() {
            @Override
            public void Result( ArrayList<Drive> drives ) {
                if (drives != null && !drives.isEmpty()) {
                    Log.i(LOG_AREA, drives.size() + " recorded drives:");
                    showData(drives);
                } else {
                    Log.i(LOG_AREA, "No recorded drives:");
                    showNoData();
                }
                Progress.dismiss(DrivesActivity.this);
            }

            @Override
            public void OnFail( int i, String s ) {
                final String error = "Query failed with err: " + i + " -> " + s;
                Log.e(LOG_AREA, error);
                showError(error);
                Progress.dismiss(DrivesActivity.this);
            }
        };

        final Calendar calendar = Calendar.getInstance();
        final Date endTime = calendar.getTime();
        calendar.add(Calendar.DATE, -7);
        final Date startTime = calendar.getTime();
        ActivityManager.GetDrives(startTime, endTime, 20, queryHandler);
    }

    private String driveDescription(final Drive drive) {
        final String Id = "Id: " + drive.Id().toString().substring(0, 20) + " ... ";
        final String distance = "Distance: " + drive.Distance()/1000.0f + " km";
        final int formatFlags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_ABBREV_MONTH| DateUtils.FORMAT_NO_YEAR;
        final String startTime = "Start Time: " + DateUtils.formatDateTime(this, drive.StartTime(), formatFlags);
        final String endTime = "End Time: " + DateUtils.formatDateTime(this, drive.EndTime(), formatFlags);
        final String startAddress = "Start Address: " + ((drive.StartAddress()!=null && drive.StartAddress().length()>20)?drive.StartAddress().substring(0, 20):"") + " ... ";
        final String endAddress = "End Address: " + ((drive.EndAddress()!=null &&  drive.EndAddress().length()>20)?drive.EndAddress().substring(0, 20):"") + " ... ";
        final ArrayList<Point> locations = drive.Locations();
        String startPoint = "Start Location: ";
        String endPoint = "End Location: ";
        if(locations!=null && locations.size()>1) {
            startPoint += locations.get(0).Latitude() + ", " + locations.get(0).Longitude();
            endPoint += locations.get(locations.size()-1).Latitude() + ", " + locations.get(locations.size()-1).Longitude();
        }
        return Id + "\n" + distance + "\n" + startTime + "\n" + endTime + "\n" + startPoint + "\n" + startAddress + "\n" + endPoint + "\n" + endAddress + "\n";
    }

    private void showData(final ArrayList<Drive> drives) {
        final ArrayList<String> list = new ArrayList<>();
        for (Drive drive : drives) {
            final String driveDescription = driveDescription(drive);
            Log.i(LOG_AREA, driveDescription);
            list.add(driveDescription);
        }
        final ListAdapter adapter = new ListAdapter(DrivesActivity.this, android.R.layout.simple_list_item_1, list);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final ListView drivesListView = (ListView) findViewById(R.id.drives_list);
                final TextView noDataTextView = (TextView) findViewById(R.id.no_data_text_view);
                drivesListView.setAdapter(adapter);
                noDataTextView.setVisibility(View.INVISIBLE);
                drivesListView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void showNoData() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final ListView drivesListView = (ListView) findViewById(R.id.drives_list);
                final TextView noDataTextView = (TextView) findViewById(R.id.no_data_text_view);
                noDataTextView.setText(getResources().getString(R.string.no_data));
                noDataTextView.setVisibility(View.VISIBLE);
                drivesListView.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void showError(final String error) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final ListView drivesListView = (ListView) findViewById(R.id.drives_list);
                final TextView noDataTextView = (TextView) findViewById(R.id.no_data_text_view);
                noDataTextView.setText(error);
                noDataTextView.setVisibility(View.VISIBLE);
                drivesListView.setVisibility(View.INVISIBLE);
            }
        });
    }
}
