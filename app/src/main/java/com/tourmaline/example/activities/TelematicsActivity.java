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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.tourmaline.context.ActivityManager;
import com.tourmaline.context.Drive;
import com.tourmaline.context.QueryHandler;
import com.tourmaline.context.TelematicsEvent;
import com.tourmaline.context.TelematicsEventListener;
import com.tourmaline.example.R;
import com.tourmaline.example.adapters.DisplayableTelematics;
import com.tourmaline.example.adapters.TelematicsAdapter;
import com.tourmaline.example.helpers.Progress;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class TelematicsActivity extends Activity {
    private static final String LOG_AREA = "TelematicsActivity";

    private TelematicsEventListener telematicsEventListener;
    private TelematicsAdapter telematicsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_telematics);
        telematicsAdapter = new TelematicsAdapter(TelematicsActivity.this, new ArrayList<DisplayableTelematics>());
        updateTelematics();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerTelematicsListener();
    }

    @Override
    protected void onPause() {
        unregisterTelematicsListener();
        super.onPause();
    }

    private void registerTelematicsListener() {
        if(telematicsEventListener==null) {
            telematicsEventListener = new TelematicsEventListener() {
                @Override
                public void OnEvent(TelematicsEvent event) {
                    Log.i(LOG_AREA, "Telematics Listener: new event");
                    addEvent(event);
                }

                @Override
                public void RegisterSucceeded() {
                    Log.i(LOG_AREA, "Telematics Listener: register success");
                }

                @Override
                public void RegisterFailed(int i) {
                    Log.e(LOG_AREA, "Telematics Listener: register failure");
                }
            };
        }
        ActivityManager.RegisterTelematicsEventListener(telematicsEventListener);
    }

    private void unregisterTelematicsListener() {
        if(telematicsEventListener!=null) {
            ActivityManager.UnregisterTelematicsEventListener(telematicsEventListener);
            telematicsEventListener = null;
        }
    }

    private void updateTelematics() {
        Progress.show(this);

        final Calendar calendar = Calendar.getInstance();
        final Date endTime = calendar.getTime();
        calendar.add(Calendar.DATE, -30);
        final Date startTime = calendar.getTime();

        ActivityManager.GetDrives(startTime, endTime, 1, new QueryHandler<ArrayList<Drive>>() {
            @Override
            public void Result(ArrayList<Drive> drives) {

                if(drives.isEmpty()) {
                    showNoData();
                    Progress.dismiss(TelematicsActivity.this);
                } else {
                    ActivityManager.GetTripTelematicsEvents(drives.get(0).Id().toString(), new QueryHandler<ArrayList<TelematicsEvent>>() {
                        @Override
                        public void Result(ArrayList<TelematicsEvent> events) {
                            if (events != null && !events.isEmpty()) {
                                Log.i(LOG_AREA, events.size() + " recorded telematics: ");
                                showData(events);
                            } else {
                                Log.i(LOG_AREA, "No recorded telematics:");
                                showNoData();
                            }
                            Progress.dismiss(TelematicsActivity.this);
                        }

                        @Override
                        public void OnFail(int i, String s) {
                            final String error = "Query failed with err: " + i + " -> " + s;
                            Log.e(LOG_AREA, error);
                            showError(error);
                            showNoData();
                            Progress.dismiss(TelematicsActivity.this);
                        }
                    });

                }
            }

            @Override
            public void OnFail(int i, String s) {
                final String error = "Query failed with err: " + i + " -> " + s;
                Log.e(LOG_AREA, error);
                showError(error);
                showNoData();
                Progress.dismiss(TelematicsActivity.this);
            }
        });

    }

    private void showData(final ArrayList<TelematicsEvent> events) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if(events!=null) {
                    telematicsAdapter.clear();
                    for(TelematicsEvent event : events) {
                        telematicsAdapter.add(new DisplayableTelematics(getApplicationContext(), event));
                    }
                    telematicsAdapter.sort(DisplayableTelematics.COMPARATOR_REVERSED);
                }
                final ListView locationsListView = findViewById(R.id.telematics_list);
                final TextView noDataTextView = findViewById(R.id.no_data_text_view);
                locationsListView.setAdapter(telematicsAdapter);
                noDataTextView.setVisibility(View.GONE);
                locationsListView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void showNoData() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final ListView locationsListView = findViewById(R.id.telematics_list);
                final TextView noDataTextView = findViewById(R.id.no_data_text_view);
                noDataTextView.setText(getResources().getString(R.string.no_data));
                noDataTextView.setVisibility(View.VISIBLE);
                locationsListView.setVisibility(View.GONE);
            }
        });
    }

    private void showError(final String error) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(TelematicsActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void addEvent(final TelematicsEvent event) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(event==null) return;
                final DisplayableTelematics displayable = new DisplayableTelematics(getApplicationContext(), event);
                telematicsAdapter.add(displayable);
                telematicsAdapter.sort(DisplayableTelematics.COMPARATOR_REVERSED);
                telematicsAdapter.notifyDataSetChanged();
                showData(null);
            }
        });
    }
}