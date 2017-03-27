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

import com.tourmaline.context.Location;
import com.tourmaline.context.LocationListener;
import com.tourmaline.context.LocationManager;
import com.tourmaline.context.QueryHandler;
import com.tourmaline.example.adapters.ListAdapter;
import com.tourmaline.example.R;

import java.util.ArrayList;

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
        final ListView locationsListView = (ListView) findViewById(R.id.locations_list);
        final TextView noDataTextView = (TextView) findViewById(R.id.no_data_text_view);

        final QueryHandler<ArrayList<Location>> queryHandler = new QueryHandler<ArrayList<Location>>() {
            @Override
            public void Result( ArrayList<Location> locs ) {
                if (locs != null && !locs.isEmpty()) {
                    Log.i(LOG_AREA, "Locations: ");
                    final ArrayList<String> list = new ArrayList<>();
                    for (Location l : locs) {
                        Log.i(LOG_AREA, "    " + l.toString());
                        list.add(l.toString());
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ListAdapter adapter =
                                    new ListAdapter(LocationsActivity.this,
                                            android.R.layout.simple_list_item_1,
                                            list);
                            locationsListView.setAdapter(adapter);
                            noDataTextView.setVisibility(View.GONE);
                            locationsListView.setVisibility(View.VISIBLE);
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            noDataTextView.setVisibility(View.VISIBLE);
                            locationsListView.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            }
            @Override
            public void OnFail( int i, String s ) {
                Log.e(LOG_AREA, "Query failed with err: " + i);
            }
        };

        LocationManager.QueryLocations(0, Long.MAX_VALUE, 50, queryHandler);
    }
}
