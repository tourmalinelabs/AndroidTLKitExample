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
package com.tourmaline.example.adapters;

import android.app.Activity;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.tourmaline.example.R;

import java.util.List;

public class TripAdapter extends ArrayAdapter<DisplayableTrip> {

    public TripAdapter(Activity activity, List<DisplayableTrip> objects) {
        super(activity, 0, objects);
    }

    private static class ViewHolder {
        TextView idTextView;
        TextView eventTypeTextView;
        TextView stateTextView;
        TextView analysisStateTextView;
        TextView distanceTextView;
        TextView startTimeTextView;
        TextView endTimeTextView;
        TextView startLocationTextView;
        TextView endLocationTextView;
        TextView startAddressTextView;
        TextView endAddressTextView;
    }

    @Override
    public @NonNull View getView(int position, View convertView, @NonNull ViewGroup parent) {
        DisplayableTrip displayableTrip = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.trip_item, parent, false);
            initTripItemView(convertView);
        }
        updateTripItemView(convertView, displayableTrip);
        return convertView;
    }

    private void initTripItemView(final View convertView) {
        final ViewHolder viewHolder = new ViewHolder();
        viewHolder.idTextView = convertView.findViewById(R.id.id_text_view);
        viewHolder.eventTypeTextView= convertView.findViewById(R.id.event_type_text_view);
        viewHolder.stateTextView = convertView.findViewById(R.id.state_text_view);
        viewHolder.analysisStateTextView = convertView.findViewById(R.id.analysis_state_text_view);
        viewHolder.distanceTextView = convertView.findViewById(R.id.distance_text_view);
        viewHolder.startTimeTextView = convertView.findViewById(R.id.start_time_text_view);
        viewHolder.endTimeTextView = convertView.findViewById(R.id.end_time_text_view);
        viewHolder.startLocationTextView = convertView.findViewById(R.id.start_location_text_view);
        viewHolder.endLocationTextView = convertView.findViewById(R.id.end_location_text_view);
        viewHolder.startAddressTextView = convertView.findViewById(R.id.start_address_text_view);
        viewHolder.endAddressTextView = convertView.findViewById(R.id.end_address_time_text_view);
        convertView.setTag(viewHolder);
    }

    private void updateTripItemView(final View view, final DisplayableTrip displayableTrip) {
        final ViewHolder viewHolder = (ViewHolder) view.getTag();
        if(displayableTrip == null) {
            return;
        }
        viewHolder.idTextView.setText(String.format(getContext().getResources().getString(R.string.trip_item_id), displayableTrip.getId().toString()));
        viewHolder.eventTypeTextView.setText(String.format(getContext().getResources().getString(R.string.trip_item_event_type), displayableTrip.getActivityEventType()));
        viewHolder.stateTextView.setText(String.format(getContext().getResources().getString(R.string.trip_item_state), displayableTrip.getTripState()));
        viewHolder.analysisStateTextView.setText(String.format(getContext().getResources().getString(R.string.trip_item_analysis_state), displayableTrip.getAnalysisState()));
        viewHolder.distanceTextView.setText(String.format(getContext().getResources().getString(R.string.trip_item_distance), displayableTrip.getDistance()));
        viewHolder.startTimeTextView.setText(String.format(getContext().getResources().getString(R.string.trip_item_start_time), displayableTrip.getStartTime()));
        viewHolder.endTimeTextView.setText(String.format(getContext().getResources().getString(R.string.trip_item_end_time), displayableTrip.getEndTime()));
        viewHolder.startLocationTextView.setText(String.format(getContext().getResources().getString(R.string.trip_item_start_loc), displayableTrip.getStartLocation()));
        viewHolder.endLocationTextView.setText(String.format(getContext().getResources().getString(R.string.trip_item_end_loc), displayableTrip.getEndLocation()));
        viewHolder.startAddressTextView.setText(String.format(getContext().getResources().getString(R.string.trip_item_start_add), displayableTrip.getStartAddress()));
        viewHolder.endAddressTextView.setText(String.format(getContext().getResources().getString(R.string.trip_item_end_add), displayableTrip.getEndAddress()));
    }
}
