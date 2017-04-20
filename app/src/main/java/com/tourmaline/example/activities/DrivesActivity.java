/* ******************************************************************************
 * Copyright 2017 Tourmaline Labs, Inc. All rights reserved.
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
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.tourmaline.context.ActivityEvent;
import com.tourmaline.context.ActivityListener;
import com.tourmaline.context.ActivityManager;
import com.tourmaline.context.Drive;
import com.tourmaline.context.QueryHandler;
import com.tourmaline.example.adapters.DisplayableDrive;
import com.tourmaline.example.adapters.DriveAdapter;
import com.tourmaline.example.R;
import com.tourmaline.example.helpers.Monitoring;
import com.tourmaline.example.helpers.Progress;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;


public class DrivesActivity extends Activity {
    private static final String LOG_AREA = "DrivesActivity";

    private ActivityListener activityListener;
    private DriveAdapter driveAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drives);

        driveAdapter = new DriveAdapter(DrivesActivity.this, new ArrayList<DisplayableDrive>());

        final Button startMonitoringButton = (Button) findViewById(R.id.start_monitoring_button);
        if(Monitoring.getState(getApplicationContext())==Monitoring.State.MANUAL) {
            startMonitoringButton.setVisibility(View.VISIBLE);
            startMonitoringButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final UUID uuid = ActivityManager.StartManualTrip();
                    Toast.makeText(DrivesActivity.this, "New Drive with id " + uuid.toString() + " is now recording", Toast.LENGTH_LONG).show();
                }
            });
        } else {
            startMonitoringButton.setVisibility(View.GONE);
            startMonitoringButton.setOnClickListener(null);
        }
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

                    if(activityEvent.Type() == ActivityEvent.ACTIVITY_REMOVED) {
                        // remove the drive from the UI
                        removeDisplayableDrive(activityEvent.Activity().Id());
                    } else {
                        if(activityEvent.Activity() instanceof Drive) {
                            //check if the drive is monitored manually
                            boolean manualMonitoring = false;
                            final ArrayList<Drive> manualDrives = ActivityManager.ActiveManualDrives();
                            for(final Drive manualDrive : manualDrives) {
                                if(manualDrive.Id().equals(activityEvent.Activity().Id())) {
                                    manualMonitoring = true;
                                    break;
                                }
                            }
                            final DisplayableDrive displayableDrive = new DisplayableDrive(getApplicationContext(), (Drive)activityEvent.Activity(), manualMonitoring, activityEvent.TypeStr());
                            //add or update the drive from the UI
                            addOrUpdateDisplayableDrive(displayableDrive);
                        }
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

                final ArrayList<DisplayableDrive> list = new ArrayList<>();
                // completed drives
                for (final Drive drive : drives) {
                    final DisplayableDrive displayableDrive = new DisplayableDrive(getApplicationContext(), drive, false, "-");
                    list.add(displayableDrive);
                }

                //manual monitored drives
                final ArrayList<Drive> manualDrives = ActivityManager.ActiveManualDrives();
                for (final Drive manualDrive : manualDrives) {
                    final DisplayableDrive displayableDrive = new DisplayableDrive(getApplicationContext(), manualDrive, true, "-");
                    list.add(displayableDrive);
                }

                if (!list.isEmpty()) {
                    Log.i(LOG_AREA, drives.size() + " recorded drives | " + manualDrives.size() + " active manually monitored drives");
                    showData(list);
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

                // we only try to display manual monitored drives
                final ArrayList<DisplayableDrive> list = new ArrayList<>();
                final ArrayList<Drive> manualDrives = ActivityManager.ActiveManualDrives();
                for (final Drive manualDrive : manualDrives) {
                    final DisplayableDrive displayableDrive = new DisplayableDrive(getApplicationContext(), manualDrive, true, "-");
                    list.add(displayableDrive);
                }

                if (!list.isEmpty()) {
                    Log.i(LOG_AREA, 0 + " recorded drives | " + manualDrives.size() + " active manually monitored drives");
                    showData(list);
                } else {
                    Log.i(LOG_AREA, "No recorded drives:");
                    showNoData();
                }

                Progress.dismiss(DrivesActivity.this);
            }
        };

        final Calendar calendar = Calendar.getInstance();
        final Date endTime = calendar.getTime();
        calendar.add(Calendar.DATE, -7);
        final Date startTime = calendar.getTime();
        ActivityManager.GetDrives(startTime, endTime, 50, queryHandler);
    }

    private void showData(final ArrayList<DisplayableDrive> displayableDrives) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(displayableDrives!=null) {
                    driveAdapter.clear();
                    driveAdapter.addAll(displayableDrives);
                    driveAdapter.sort(DisplayableDrive.COMPARATOR_REVERSED);
                }
                final ListView drivesListView = (ListView) findViewById(R.id.drives_list);
                final TextView noDataTextView = (TextView) findViewById(R.id.no_data_text_view);
                drivesListView.setAdapter(driveAdapter);
                noDataTextView.setVisibility(View.GONE);
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
                drivesListView.setVisibility(View.GONE);
            }
        });
    }

    private void showError(final String error) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(DrivesActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void addOrUpdateDisplayableDrive(final DisplayableDrive displayableDrive) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(displayableDrive==null) return;
                int matchingIndex = -1;
                DisplayableDrive matchingDrive = null;
                for(int j = 0; j<driveAdapter.getCount();j++) {
                    DisplayableDrive drive = driveAdapter.getItem(j);
                    if((drive!=null) && drive.getId().equals(displayableDrive.getId())) {
                        matchingIndex = j;
                        matchingDrive = drive;
                        break;
                    }
                }

                if(matchingIndex<0) {
                    driveAdapter.add(displayableDrive);
                    driveAdapter.sort(DisplayableDrive.COMPARATOR_REVERSED);
                } else {
                    driveAdapter.remove(matchingDrive);
                    driveAdapter.insert(displayableDrive, matchingIndex);
                }
                driveAdapter.notifyDataSetChanged();
                showData(null);
            }
        });
    }

    private void removeDisplayableDrive(final UUID uuid) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                DisplayableDrive matchingDrive = null;
                for(int j = 0; j<driveAdapter.getCount();j++) {
                    DisplayableDrive drive = driveAdapter.getItem(j);
                    if((drive!=null) && drive.getId().equals(uuid)) {
                        matchingDrive = drive;
                        break;
                    }
                }
                driveAdapter.remove(matchingDrive);
                driveAdapter.notifyDataSetChanged();
                if(driveAdapter.getCount()==0) {
                    showNoData();
                }
            }
        });
    }
}
