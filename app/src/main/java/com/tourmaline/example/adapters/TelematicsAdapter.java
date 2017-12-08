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
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.tourmaline.example.R;

import java.util.List;


public class TelematicsAdapter extends ArrayAdapter<DisplayableTelematics> {

    public TelematicsAdapter(Activity activity, List<DisplayableTelematics> objects) {
        super(activity, 0, objects);
    }

    private class ViewHolder {
        TextView textViewTripId;
        TextView textViewType;
        TextView textViewPosition;
        TextView textViewTime;
        TextView textViewDuration;
        TextView textViewSpeed;
        TextView textViewSeverity;
    }

    @Override
    public @NonNull
    View getView(int position, View convertView, @NonNull ViewGroup parent) {
        DisplayableTelematics displayable = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.telematics_item, parent, false);
            initLocationItemView(convertView);
        }
        updateLocationItemView(convertView, displayable);
        return convertView;
    }

    private void initLocationItemView(final View convertView) {
        final ViewHolder viewHolder = new ViewHolder();
        viewHolder.textViewTripId   = convertView.findViewById(R.id.id_text_view_tripid);
        viewHolder.textViewType     = convertView.findViewById(R.id.id_text_view_type);
        viewHolder.textViewPosition = convertView.findViewById(R.id.id_text_view_position);
        viewHolder.textViewTime     = convertView.findViewById(R.id.id_text_view_time);
        viewHolder.textViewDuration = convertView.findViewById(R.id.id_text_view_duration);
        viewHolder.textViewSpeed    = convertView.findViewById(R.id.id_text_view_speed);
        viewHolder.textViewSeverity =  convertView.findViewById(R.id.id_text_view_severity);
        convertView.setTag(viewHolder);
    }

    private void updateLocationItemView(final View view, final DisplayableTelematics displayable) {
        final ViewHolder viewHolder = (ViewHolder) view.getTag();
        viewHolder.textViewTripId.setText(displayable.getTripId());
        viewHolder.textViewType.setText(displayable.getType());
        viewHolder.textViewPosition.setText(displayable.getPosition());
        viewHolder.textViewTime.setText(displayable.getTime());
        viewHolder.textViewDuration.setText(displayable.getDuration());
        viewHolder.textViewSpeed.setText(displayable.getSpeed());
        viewHolder.textViewSeverity.setText(displayable.getSeverity());
    }
}