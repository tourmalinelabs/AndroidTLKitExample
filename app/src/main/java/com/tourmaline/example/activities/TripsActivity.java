/* ******************************************************************************
 * Copyright 2023 Tourmaline Labs, Inc. All rights reserved.
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

import com.tourmaline.apis.TLActivityManager;
import com.tourmaline.apis.TLKit;
import com.tourmaline.apis.listeners.TLActivityListener;
import com.tourmaline.apis.listeners.TLQueryListener;
import com.tourmaline.apis.objects.TLActivityEvent;
import com.tourmaline.apis.objects.TLTrip;
import com.tourmaline.example.adapters.DisplayableTrip;
import com.tourmaline.example.adapters.TripAdapter;
import com.tourmaline.example.R;
import com.tourmaline.example.helpers.Progress;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;


public class TripsActivity extends Activity {
    private static final String LOG_AREA = "TripsActivity";

    private TLActivityListener activityListener;
    private TripAdapter tripAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trips);
        tripAdapter = new TripAdapter(TripsActivity.this, new ArrayList<>());
        updateTrips();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerTripListener();
    }

    @Override
    protected void onPause() {
        unregisterTripListener();
        super.onPause();
    }

    private void registerTripListener() {
        if(activityListener==null) {
            activityListener = new TLActivityListener() {
                @Override
                public void OnEvent( TLActivityEvent activityEvent ) {
                    Log.i(LOG_AREA, "Activity Listener: new event");
                    if(activityEvent.Type() == TLActivityEvent.ACTIVITY_REMOVED) {
                        // remove the trip from the UI
                        removeDisplayableTrip(activityEvent.Activity().Id());
                    } else {
                        if(activityEvent.Activity() instanceof TLTrip) {
                            TLTrip trip = (TLTrip)activityEvent.Activity();
                            final DisplayableTrip displayableTrip = new DisplayableTrip(getApplicationContext(), trip, activityEvent.TypeStr());
                            //add or update the trip from the UI
                            addOrUpdateDisplayableTrip(displayableTrip);
                        }
                    }
                }

                @Override
                public void RegisterSucceeded() {
                    Log.i(LOG_AREA, "Activity Listener: register success");
                }

                @Override
                public void RegisterFailed(int reason, String message ) {
                    Log.e(LOG_AREA, "Activity Listener: register failure");
                }
            };
        }
        TLKit.TLActivityManager().ListenForTripEvents(activityListener);
    }

    private void unregisterTripListener() {
        if(activityListener!=null) {
            TLKit.TLActivityManager().StopListeningForTripEvents(activityListener);
            activityListener = null;
        }
    }

    private void updateTrips() {
        Progress.show(this);

        final TLQueryListener<ArrayList<TLTrip>> queryHandler = new TLQueryListener<ArrayList<TLTrip>>() {
            @Override
            public void Result( ArrayList<TLTrip> trips ) {
                final ArrayList<DisplayableTrip> list = new ArrayList<>();
                for (final TLTrip trip : trips) {
                    final DisplayableTrip displayableTrip = new DisplayableTrip(getApplicationContext(), trip, "-");
                    list.add(displayableTrip);
                }

                if (!list.isEmpty()) {
                    Log.i(LOG_AREA, trips.size() + " recorded trips");
                    showData(list);
                } else {
                    Log.i(LOG_AREA, "No recorded trips:");
                    showNoData();
                }
                Progress.dismiss(TripsActivity.this);
            }

            @Override
            public void OnFail( int i, String s ) {
                final String error = "Query failed with err: " + i + " -> " + s;
                Log.e(LOG_AREA, error);
                Progress.dismiss(TripsActivity.this);
                showError(error);
                showNoData();
            }
        };

        final Calendar calendar = Calendar.getInstance();
        final Date endTime = calendar.getTime();
        calendar.add(Calendar.DATE, -30);
        final Date startTime = calendar.getTime();
        TLKit.TLActivityManager().QueryTrips(startTime, endTime, 50, queryHandler);
    }

    private void showData(final ArrayList<DisplayableTrip> displayableTrips) {
        runOnUiThread(() -> {
            if(displayableTrips !=null) {
                tripAdapter.clear();
                tripAdapter.addAll(displayableTrips);
                tripAdapter.sort(DisplayableTrip.COMPARATOR_REVERSED);
            }
            final ListView listView = findViewById(R.id.trips_list);
            final TextView noDataTextView = findViewById(R.id.no_data_text_view);
            listView.setAdapter(tripAdapter);
            noDataTextView.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
        });
    }

    private void showNoData() {
        runOnUiThread(() -> {
            final ListView listView = findViewById(R.id.trips_list);
            final TextView noDataTextView = findViewById(R.id.no_data_text_view);
            noDataTextView.setText(getResources().getString(R.string.no_data));
            noDataTextView.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        });
    }

    private void showError(final String error) {
        runOnUiThread(() -> Toast.makeText(TripsActivity.this, error, Toast.LENGTH_LONG).show());
    }

    private void addOrUpdateDisplayableTrip(final DisplayableTrip displayableTrip) {
        runOnUiThread(() -> {
            if(displayableTrip ==null) return;
            int matchingIndex = -1;
            DisplayableTrip matchingTrip = null;
            for(int j = 0; j< tripAdapter.getCount(); j++) {
                DisplayableTrip trip = tripAdapter.getItem(j);
                if((trip!=null) && trip.getId().equals(displayableTrip.getId())) {
                    matchingIndex = j;
                    matchingTrip = trip;
                    break;
                }
            }

            if(matchingIndex<0) {
                tripAdapter.add(displayableTrip);
                tripAdapter.sort(DisplayableTrip.COMPARATOR_REVERSED);
            } else {
                tripAdapter.remove(matchingTrip);
                tripAdapter.insert(displayableTrip, matchingIndex);
            }
            tripAdapter.notifyDataSetChanged();
            showData(null);
        });
    }

    private void removeDisplayableTrip(final UUID uuid) {
        runOnUiThread(() -> {
            DisplayableTrip matchingTrip = null;
            for(int j = 0; j< tripAdapter.getCount(); j++) {
                DisplayableTrip trip = tripAdapter.getItem(j);
                if((trip!=null) && trip.getId().equals(uuid)) {
                    matchingTrip = trip;
                    break;
                }
            }
            tripAdapter.remove(matchingTrip);
            tripAdapter.notifyDataSetChanged();
            if(tripAdapter.getCount()==0) {
                showNoData();
            }
        });
    }
}
