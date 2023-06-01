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

import android.content.Context;
import android.text.format.DateUtils;

import com.tourmaline.apis.objects.TLPoint;
import com.tourmaline.apis.objects.TLTrip;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.UUID;

public class DisplayableTrip {

    private final String activityEventType;
    private final String distance;
    private final String analysisState;
    private final String tripState;
    private String endAddress;
    private String endLocation;
    private final String endTime;
    private final UUID id;
    private String startAddress;
    private String startLocation;
    private final String startTime;
    private final long startTimestamp;

    public DisplayableTrip(final Context context, final TLTrip trip, final String activityEventType) {
        this.id = trip.Id();
        this.activityEventType = activityEventType;
        this.tripState = trip.StateStr();
        this.analysisState = trip.AnalysisStateStr();
        this.startTimestamp = trip.StartTime();
        final DecimalFormat numberFormat = new DecimalFormat("0.000");
        this.distance = numberFormat.format(trip.Distance()/1000.0f) + " km";
        final int formatFlags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_ABBREV_MONTH| DateUtils.FORMAT_NO_YEAR;
        this.startTime = DateUtils.formatDateTime(context, trip.StartTime(), formatFlags);
        this.endTime = DateUtils.formatDateTime(context, trip.EndTime(), formatFlags);
        final ArrayList<TLPoint> locations = trip.Locations();
        this.startLocation = this.endLocation = "empty";
        final DecimalFormat positionFormat = new DecimalFormat("0.0000");
        if(locations!=null && locations.size()>1) {
            this.startLocation = "(" + positionFormat.format(locations.get(0).Latitude()) + "|" + positionFormat.format(locations.get(0).Longitude()) + ")";
            this.endLocation = "(" + positionFormat.format(locations.get(locations.size()-1).Latitude()) + "|" + positionFormat.format(locations.get(locations.size()-1).Longitude()) + ")";
        }

        this.startAddress = this.endAddress = "empty";
        if(trip.StartAddress()!=null) {
            this.startAddress = trip.StartAddress();
        }
        if(trip.EndAddress()!=null) {
            this.endAddress = trip.EndAddress();
        }
    }

    //getters
    public UUID getId() {
        return id;
    }

    String getActivityEventType() {
        return activityEventType;
    }

    String getTripState() {
        return tripState;
    }

    String getAnalysisState() {
        return analysisState;
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
    public static final Comparator<DisplayableTrip> COMPARATOR_REVERSED = (o1, o2) -> {
        final long diff = o2.startTimestamp-o1.startTimestamp;
        return (diff==0)?0:((diff>0)?1:-1);
    };
}
