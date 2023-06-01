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

import com.tourmaline.apis.objects.TLTelematicsEvent;

import java.text.DecimalFormat;
import java.util.Comparator;


public class DisplayableTelematics {
    final private String duration;
    final private String position;
    final private String severity;
    final private String speed;
    final private String time;
    final private long timestamp;
    final private String tripId;
    final private String type;

    public DisplayableTelematics(final Context context, final TLTelematicsEvent event) {
        this.tripId = event.TripId();

        switch (event.Type()) {
            case ACCEL: this.type = "Type: ACCEL"; break;
            case BRAKE: this.type = "Type: BRAKE"; break;
            case LEFT:  this.type = "Type: LEFT"; break;
            case RIGHT: this.type = "Type: RIGHT"; break;
            case PHONE: this.type = "Type: PHONE"; break;
            case SPEED: this.type = "Type: SPEED"; break;
            default:    this.type = "Type: UNKNOWN"; break;
        }
        final DecimalFormat posFormat = new DecimalFormat("0.0000");
        this.position = "Location: (" + posFormat.format(event.Latitude()) + "|" + posFormat.format(event.Longitude()) + ")";
        final int formatFlags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_ABBREV_MONTH | DateUtils.FORMAT_NO_YEAR;
        this.time = "Time: " + DateUtils.formatDateTime(context, event.Time(), formatFlags);
        this.duration = "Duration: " + event.Duration() + " ms";
        this.speed = "Speed: " + event.Speed();
        this.severity = "Severity: " + event.Severity();
        this.timestamp = event.Time();
    }


    public String getTripId() {
        return tripId;
    }

    public String getType() {
        return type;
    }

    String getPosition() {
        return position;
    }

    String getTime() {
        return time;
    }

    public String getDuration() {
        return duration;
    }

    public String getSpeed() {
        return speed;
    }

    public String getSeverity() {
        return severity;
    }

    //Utility
    public static final Comparator<DisplayableTelematics> COMPARATOR_REVERSED = (o1, o2) -> {
        final long diff = o2.timestamp - o1.timestamp;
        return (diff == 0) ? 0 : ((diff > 0) ? 1 : -1);
    };

}
