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
import android.widget.Button;
import android.widget.TextView;

import com.tourmaline.context.ActivityManager;
import com.tourmaline.example.R;

import java.util.List;
import java.util.UUID;

public class DriveAdapter extends ArrayAdapter<DisplayableDrive> {

    public DriveAdapter(Activity activity, List<DisplayableDrive> objects) {
        super(activity, 0, objects);
    }

    private static class ViewHolder {
        UUID uuid;
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
        Button stopMonitoringButton;
    }

    @Override
    public @NonNull View getView(int position, View convertView, @NonNull ViewGroup parent) {
        DisplayableDrive displayableDrive = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.drive_item, parent, false);
            initDriveItemView(convertView);
        }
        updateDriveItemView(convertView, displayableDrive);
        return convertView;
    }

    private void initDriveItemView(final View convertView) {
        final ViewHolder viewHolder = new ViewHolder();
        viewHolder.idTextView = (TextView) convertView.findViewById(R.id.id_text_view);
        viewHolder.eventTypeTextView= (TextView) convertView.findViewById(R.id.event_type_text_view);
        viewHolder.stateTextView = (TextView) convertView.findViewById(R.id.state_text_view);
        viewHolder.analysisStateTextView = (TextView) convertView.findViewById(R.id.analysis_state_text_view);
        viewHolder.distanceTextView = (TextView) convertView.findViewById(R.id.distance_text_view);
        viewHolder.startTimeTextView = (TextView) convertView.findViewById(R.id.start_time_text_view);
        viewHolder.endTimeTextView = (TextView) convertView.findViewById(R.id.end_time_text_view);
        viewHolder.startLocationTextView = (TextView) convertView.findViewById(R.id.start_location_text_view);
        viewHolder.endLocationTextView = (TextView) convertView.findViewById(R.id.end_location_text_view);
        viewHolder.startAddressTextView = (TextView) convertView.findViewById(R.id.start_address_text_view);
        viewHolder.endAddressTextView = (TextView) convertView.findViewById(R.id.end_address_time_text_view);
        viewHolder.stopMonitoringButton = (Button) convertView.findViewById(R.id.stop_monitoring_button);
        convertView.setTag(viewHolder);
    }

    private void updateDriveItemView(final View view, final DisplayableDrive displayableDrive) {
        final ViewHolder viewHolder = (ViewHolder) view.getTag();
        final UUID uuid = displayableDrive.getId();
        viewHolder.uuid = uuid;

        viewHolder.idTextView.setText(String.format(getContext().getResources().getString(R.string.drive_item_id), displayableDrive.getId().toString()));
        viewHolder.eventTypeTextView.setText(String.format(getContext().getResources().getString(R.string.drive_item_event_type), displayableDrive.getActivityEventType()));
        viewHolder.stateTextView.setText(String.format(getContext().getResources().getString(R.string.drive_item_state), displayableDrive.getDriveState()));
        viewHolder.analysisStateTextView.setText(String.format(getContext().getResources().getString(R.string.drive_item_analysis_state), displayableDrive.getDriveAnalysisState()));
        viewHolder.distanceTextView.setText(String.format(getContext().getResources().getString(R.string.drive_item_distance), displayableDrive.getDistance()));
        viewHolder.startTimeTextView.setText(String.format(getContext().getResources().getString(R.string.drive_item_start_time), displayableDrive.getStartTime()));
        viewHolder.endTimeTextView.setText(String.format(getContext().getResources().getString(R.string.drive_item_end_time), displayableDrive.getEndTime()));
        viewHolder.startLocationTextView.setText(String.format(getContext().getResources().getString(R.string.drive_item_start_loc), displayableDrive.getStartLocation()));
        viewHolder.endLocationTextView.setText(String.format(getContext().getResources().getString(R.string.drive_item_end_loc), displayableDrive.getEndLocation()));
        viewHolder.startAddressTextView.setText(String.format(getContext().getResources().getString(R.string.drive_item_start_add), displayableDrive.getStartAddress()));
        viewHolder.endAddressTextView.setText(String.format(getContext().getResources().getString(R.string.drive_item_end_add), displayableDrive.getEndAddress()));

        if(displayableDrive.isMonitoring()) {
            viewHolder.stopMonitoringButton.setVisibility(View.VISIBLE);
            viewHolder.stopMonitoringButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View buttonView) {
                    final UUID currentUuid = ((ViewHolder) view.getTag()).uuid;
                    if(!currentUuid.equals(uuid)) return;
                    ActivityManager.StopManualTrip(currentUuid);
                    viewHolder.stopMonitoringButton.setVisibility(View.INVISIBLE);
                    viewHolder.stopMonitoringButton.setOnClickListener(null);
                }
            });
        } else {
            viewHolder.stopMonitoringButton.setVisibility(View.INVISIBLE);
            viewHolder.stopMonitoringButton.setOnClickListener(null);
        }

    }
}
