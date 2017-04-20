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
package com.tourmaline.example.helpers;

import android.content.Context;

public class Monitoring {

    private static final String MONITORING_STATE = "PrefMonitoringState";

    public enum State { STOPPED, AUTOMATIC, MANUAL }

    public static State getState(final Context context) {
        final int state = Preferences.getInstance(context).getInt(MONITORING_STATE, State.STOPPED.ordinal());
        return (State.values())[state];
    }

    public static void setState(final Context context, final State state) {
        Preferences.getInstance(context).putInt(MONITORING_STATE, state.ordinal());
    }

}
