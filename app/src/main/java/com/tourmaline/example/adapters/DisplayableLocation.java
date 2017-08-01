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

import com.tourmaline.context.Location;

import java.text.DecimalFormat;
import java.util.Comparator;

public class DisplayableLocation {
    private String position;
    private String time;
    private String address;
    private String state;
    private long timestamp;

    public DisplayableLocation(final Context context, final Location location) {
        final DecimalFormat posFormat = new DecimalFormat("0.0000");
        this.position = "Location: (" + posFormat.format(location.lat) + "|" + posFormat.format(location.lng) + ")";
        final int formatFlags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_ABBREV_MONTH| DateUtils.FORMAT_NO_YEAR;
        this.time = "Time: " + DateUtils.formatDateTime(context, location.ts, formatFlags);
        this.address = "Address: " + location.address;
        this.state = "State: " + location.StateStr();
        this.timestamp = location.ts;
    }

    String getPosition() {
        return position;
    }

    String getTime() {
        return time;
    }

    String getAddress() {
        return address;
    }

    String getState() {
        return state;
    }

    //Utility
    public static final Comparator<DisplayableLocation> COMPARATOR_REVERSED = new Comparator<DisplayableLocation>() {
        @Override
        public int compare(DisplayableLocation o1, DisplayableLocation o2) {
            final long diff = o2.timestamp-o1.timestamp;
            return (diff==0)?0:((diff>0)?1:-1);
        }
    };
}
