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

import com.tourmaline.apis.TLLocationManager;
import com.tourmaline.apis.listeners.TLLocationListener;
import com.tourmaline.apis.listeners.TLQueryListener;
import com.tourmaline.apis.objects.TLLocation;
import com.tourmaline.example.R;
import com.tourmaline.example.adapters.DisplayableLocation;
import com.tourmaline.example.adapters.LocationAdapter;
import com.tourmaline.example.helpers.Progress;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class LocationsActivity extends Activity {
    private static final String LOG_AREA = "LocationsActivity";

    private TLLocationListener locationListener;
    private LocationAdapter locationAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_locations);
        locationAdapter = new LocationAdapter(LocationsActivity.this, new ArrayList<>());
        updateLocations();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerLocationListener();
    }

    @Override
    protected void onPause() {
        unregisterLocationListener();
        super.onPause();
    }

    private void registerLocationListener() {
        if(locationListener==null) {
            locationListener = new TLLocationListener() {
                @Override
                public void OnLocationUpdated(TLLocation location) {
                    Log.i(LOG_AREA, "Location Listener: new location");
                    addLocation(location);
                }

                @Override
                public void RegisterSucceeded() {
                    Log.i(LOG_AREA, "Location Listener: register success");
                }

                @Override
                public void RegisterFailed(int i) {
                    Log.e(LOG_AREA, "Location Listener: register failure");
                }
            };
        }
        TLLocationManager.ListenForLocationEvents(locationListener);
    }

    private void unregisterLocationListener() {
        if(locationListener!=null) {
            TLLocationManager.StopListeningForLocationEvents(locationListener);
            locationListener = null;
        }
    }

    private void updateLocations() {
        Progress.show(this);

        final TLQueryListener<ArrayList<TLLocation>> queryHandler = new TLQueryListener<ArrayList<TLLocation>>() {
            @Override
            public void Result(ArrayList<TLLocation> locations) {
                if (locations != null && !locations.isEmpty()) {
                    Log.i(LOG_AREA, locations.size() + " recorded locations: ");
                    showData(locations);
                } else {
                    Log.i(LOG_AREA, "No recorded locations:");
                    showNoData();
                }
                Progress.dismiss(LocationsActivity.this);
            }
            @Override
            public void OnFail( int i, String s ) {
                final String error = "Query failed with err: " + i + " -> " + s;
                Log.e(LOG_AREA, error);
                showError(error);
                showNoData();
                Progress.dismiss(LocationsActivity.this);
            }
        };

        final Calendar calendar = Calendar.getInstance();
        final Date endTime = calendar.getTime();
        calendar.add(Calendar.DATE, -7);
        final Date startTime = calendar.getTime();

        TLLocationManager.QueryLocations(startTime.getTime(), endTime.getTime(), 50, queryHandler);
    }

    private void showData(final ArrayList<TLLocation> locations) {
        runOnUiThread(() -> {

            if(locations!=null) {
                locationAdapter.clear();
                for(TLLocation location : locations) {
                    locationAdapter.add(new DisplayableLocation(getApplicationContext(), location));
                }
                locationAdapter.sort(DisplayableLocation.COMPARATOR_REVERSED);
            }
            final ListView locationsListView = findViewById(R.id.locations_list);
            final TextView noDataTextView = findViewById(R.id.no_data_text_view);
            locationsListView.setAdapter(locationAdapter);
            noDataTextView.setVisibility(View.GONE);
            locationsListView.setVisibility(View.VISIBLE);
        });
    }

    private void showNoData() {
        runOnUiThread(() -> {
            final ListView locationsListView = findViewById(R.id.locations_list);
            final TextView noDataTextView = findViewById(R.id.no_data_text_view);
            noDataTextView.setText(getResources().getString(R.string.no_data));
            noDataTextView.setVisibility(View.VISIBLE);
            locationsListView.setVisibility(View.GONE);
        });
    }

    private void showError(final String error) {
        runOnUiThread(() -> Toast.makeText(LocationsActivity.this, error, Toast.LENGTH_LONG).show());
    }

    private void addLocation(final TLLocation location) {
        runOnUiThread(() -> {
            if(location==null) return;
            final DisplayableLocation displayableLocation = new DisplayableLocation(getApplicationContext(), location);
            locationAdapter.add(displayableLocation);
            locationAdapter.sort(DisplayableLocation.COMPARATOR_REVERSED);
            locationAdapter.notifyDataSetChanged();
            showData(null);
        });
    }
}
