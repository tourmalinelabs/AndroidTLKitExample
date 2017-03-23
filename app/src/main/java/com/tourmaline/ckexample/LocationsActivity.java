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

import com.tourmaline.context.Location;
import com.tourmaline.context.LocationListener;
import com.tourmaline.context.LocationManager;
import com.tourmaline.context.QueryHandler;

import java.util.ArrayList;

public class LocationsActivity extends Activity implements LocationListener {
    private final static String LOG_AREA = "LocationsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_locations);
        final Button stopLocDetectButton = (Button) findViewById(R.id.stop_loc_detect_button);
        stopLocDetectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocationManager.UnregisterLocationListener(LocationsActivity.this);
                Log.e(LOG_AREA, "Unregistered Location Listener");
                Toast.makeText(LocationsActivity.this, "Unregistered Location Listener",
                        Toast.LENGTH_LONG).show();
                stopLocDetectButton.setEnabled(false);
            }
        });
        updateLocations();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocationManager.UnregisterLocationListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocationManager.RegisterLocationListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_locations, menu);
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


    private void updateLocations() {
        final ListView locationsListView = (ListView) findViewById(R.id.locations_list);
        final TextView noDataTextView = (TextView) findViewById(R.id.no_data_text_view);
        LocationManager.QueryLocations(
            0, Long.MAX_VALUE, 50,
            new QueryHandler<ArrayList<Location>>() {
                @Override
                public void Result( ArrayList<Location> locs ) {
                         if (locs != null && !locs.isEmpty()) {
                            Log.d(LOG_AREA, "Locations: ");
                            final ArrayList<String> list = new ArrayList<>();
                            for (Location l : locs) {
                                Log.d(LOG_AREA, "    " + l.toString());
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

                    Log.e(LOG_AREA, "Query failed w/err: " + i);
                }
            });
    }

    @Override
    public void OnLocationUpdated(Location location) {
        updateLocations();
    }

    @Override
    public void RegisterSucceeded() {
        Log.e(LOG_AREA, "Location listener register succeeded");
    }

    @Override
    public void RegisterFailed(int i) {
        Log.e(LOG_AREA, "Location listener register failed because " + i);
    }
}
