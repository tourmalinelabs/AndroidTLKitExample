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

import android.content.Context;
import android.text.format.DateUtils;

import com.tourmaline.context.Drive;
import com.tourmaline.context.Point;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.UUID;

public class DisplayableDrive {
    private UUID id;
    private String activityEventType;
    private String driveState;
    private String driveAnalysisState;
    private String distance;
    private String startTime;
    private String endTime;
    private String startLocation;
    private String endLocation;
    private String startAddress;
    private String endAddress;
    private boolean monitoring;
    private long startTimestamp;

    public DisplayableDrive(final Context context, final Drive drive, final boolean monitoring, final String activityEventType) {
        this.id = drive.Id();
        this.activityEventType = activityEventType;
        this.driveState = drive.StateStr();
        this.driveAnalysisState = drive.AnalysisStateStr();
        this.monitoring = monitoring;
        this.startTimestamp = drive.StartTime();
        final DecimalFormat numberFormat = new DecimalFormat("0.000");
        this.distance = numberFormat.format(drive.Distance()/1000.0f) + " km";
        final int formatFlags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_ABBREV_MONTH| DateUtils.FORMAT_NO_YEAR;
        this.startTime = DateUtils.formatDateTime(context, drive.StartTime(), formatFlags);
        this.endTime = DateUtils.formatDateTime(context, drive.EndTime(), formatFlags);
        final ArrayList<Point> locations = drive.Locations();
        this.startLocation = this.endLocation = "empty";
        final DecimalFormat positionFormat = new DecimalFormat("0.0000");
        if(locations!=null && locations.size()>1) {
            this.startLocation = "(" + positionFormat.format(locations.get(0).Latitude()) + "|" + positionFormat.format(locations.get(0).Longitude()) + ")";
            this.endLocation = "(" + positionFormat.format(locations.get(locations.size()-1).Latitude()) + "|" + positionFormat.format(locations.get(locations.size()-1).Longitude()) + ")";
        }

        this.startAddress = this.endAddress = "empty";
        if(drive.StartAddress()!=null) {
            this.startAddress = drive.StartAddress();
        }
        if(drive.EndAddress()!=null) {
            this.endAddress = drive.EndAddress();
        }
    }

    //getters
    public UUID getId() {
        return id;
    }

    String getActivityEventType() {
        return activityEventType;
    }

    String getDriveState() {
        return driveState;
    }

    String getDriveAnalysisState() {
        return driveAnalysisState;
    }

    boolean isMonitoring() {
        return monitoring;
    }

    String getDistance() {
        return distance;
    }

    String getStartTime() {
        return startTime;
    }

    String getEndTime() {
        return endTime;
    }

    String getStartLocation() {
        return startLocation;
    }

    String getEndLocation() {
        return endLocation;
    }

    String getStartAddress() {
        return startAddress;
    }

    String getEndAddress() {
        return endAddress;
    }

    //Utility
    public static final Comparator<DisplayableDrive> COMPARATOR_REVERSED = new Comparator<DisplayableDrive>() {
        @Override
        public int compare(DisplayableDrive o1, DisplayableDrive o2) {
            final long diff = o2.startTimestamp-o1.startTimestamp;
            return (diff==0)?0:((diff>0)?1:-1);
        }
    };
}
