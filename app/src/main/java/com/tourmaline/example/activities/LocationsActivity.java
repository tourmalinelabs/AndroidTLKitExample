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
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.tourmaline.context.Location;
import com.tourmaline.context.LocationListener;
import com.tourmaline.context.LocationManager;
import com.tourmaline.context.QueryHandler;
import com.tourmaline.example.adapters.ListAdapter;
import com.tourmaline.example.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class LocationsActivity extends Activity {
    private static final String LOG_AREA = "LocationsActivity";

    private LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_locations);
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
            locationListener = new LocationListener() {
                @Override
                public void OnLocationUpdated(Location location) {
                    Log.i(LOG_AREA, "Location Listener: new location");
                    updateLocations();
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
        LocationManager.RegisterLocationListener(locationListener);
    }

    private void unregisterLocationListener() {
        if(locationListener!=null) {
            LocationManager.UnregisterLocationListener(locationListener);
            locationListener = null;
        }
    }

    private void updateLocations() {
        final ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage("Please wait...");
        progress.show();

        final ListView locationsListView = (ListView) findViewById(R.id.locations_list);
        final TextView noDataTextView = (TextView) findViewById(R.id.no_data_text_view);

        final QueryHandler<ArrayList<Location>> queryHandler = new QueryHandler<ArrayList<Location>>() {
            @Override
            public void Result( ArrayList<Location> locs ) {
                if (locs != null && !locs.isEmpty()) {
                    Log.i(LOG_AREA, locs.size() + " recorded locations: ");
                    final ArrayList<String> list = new ArrayList<>();
                    for (Location location : locs) {
                        final String locationDescription = locationDescription(location);
                        Log.i(LOG_AREA, locationDescription);
                        list.add(locationDescription);
                    }
                    final ListAdapter adapter = new ListAdapter(LocationsActivity.this, android.R.layout.simple_list_item_1, list);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            locationsListView.setAdapter(adapter);
                            noDataTextView.setVisibility(View.GONE);
                            locationsListView.setVisibility(View.VISIBLE);
                            progress.dismiss();
                        }
                    });
                } else {
                    Log.i(LOG_AREA, "No recorded drives:");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            noDataTextView.setVisibility(View.VISIBLE);
                            locationsListView.setVisibility(View.INVISIBLE);
                            progress.dismiss();
                        }
                    });
                }
            }
            @Override
            public void OnFail( int i, String s ) {
                Log.e(LOG_AREA, "Query failed with err: " + i);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progress.dismiss();
                    }
                });
            }
        };

        final Calendar calendar = Calendar.getInstance();
        final Date endTime = calendar.getTime();
        calendar.add(Calendar.DATE, -7);
        final Date startTime = calendar.getTime();

        LocationManager.QueryLocations(startTime.getTime(),endTime.getTime(), 20, queryHandler);
    }

    private String locationDescription(final Location location) {
        final String latLng = "Location: " + location.lat + ", " + location.lng;
        final int formatFlags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_ABBREV_MONTH| DateUtils.FORMAT_NO_YEAR;
        final String time = "Time: " + DateUtils.formatDateTime(this, location.ts, formatFlags);
        final String address = "Address: " + ((location.address!=null && location.address.length()>20)?location.address.substring(0, 20):"") + " ... ";
        final String state = "State: " + location.StateStr();
        return latLng + "\n" + time + "\n" + address + "\n" + state + "\n";
    }
}
