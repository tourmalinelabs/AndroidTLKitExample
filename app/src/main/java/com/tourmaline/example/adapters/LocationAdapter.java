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

public class LocationAdapter extends ArrayAdapter<DisplayableLocation> {

    public LocationAdapter(Activity activity, List<DisplayableLocation> objects) {
        super(activity, 0, objects);
    }

    private class ViewHolder {
        TextView textViewLocation;
        TextView textViewTime;
        TextView textViewAddress;
        TextView textViewState;

    }

    @Override
    public @NonNull
    View getView(int position, View convertView, @NonNull ViewGroup parent) {
        DisplayableLocation displayableLocation = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.location_item, parent, false);
            initLocationItemView(convertView);
        }
        updateLocationItemView(convertView, displayableLocation);
        return convertView;
    }

    private void initLocationItemView(final View convertView) {
        final ViewHolder viewHolder = new ViewHolder();
        viewHolder.textViewLocation = (TextView) convertView.findViewById(R.id.id_text_view_location);
        viewHolder.textViewTime = (TextView) convertView.findViewById(R.id.id_text_view_time);
        viewHolder.textViewAddress = (TextView) convertView.findViewById(R.id.id_text_view_address);
        viewHolder.textViewState = (TextView) convertView.findViewById(R.id.id_text_view_state);
        convertView.setTag(viewHolder);
    }

    private void updateLocationItemView(final View view, final DisplayableLocation displayableLocation) {
        final ViewHolder viewHolder = (ViewHolder) view.getTag();
        viewHolder.textViewLocation.setText(displayableLocation.getPosition());
        viewHolder.textViewTime.setText(displayableLocation.getTime());
        viewHolder.textViewAddress.setText(displayableLocation.getAddress());
        viewHolder.textViewState.setText(displayableLocation.getState());
    }
}
