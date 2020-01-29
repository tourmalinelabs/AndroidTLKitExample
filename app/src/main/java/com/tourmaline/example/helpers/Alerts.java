/* ******************************************************************************
 * Copyright 2018 Tourmaline Labs, Inc. All rights reserved.
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

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.tourmaline.example.R;

import static androidx.core.app.NotificationCompat.VISIBILITY_SECRET;


public class Alerts {

    private static final String NOTIF_CHANNEL_ID_GPS = "notif-channel-id-gps";
    private static final String NOTIF_CHANNEL_ID_PERMISSION = "notif-channel-id-permission";
    private static final String NOTIF_CHANNEL_ID_POWER = "notif-channel-id-power";

    private static final int NOTIF_ID_GPS = 24352;
    private static final int NOTIF_ID_PERMISSION = 24354;
    private static final int NOTIF_ID_POWER = 24356;

    public enum Type { GPS, PERMISSION, POWER };

    public static void show(final Context context, final Type type) {
        final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        String channelId    = "";
        String channelTitle = "";
        String notifTitle   = "";
        String notifText    = context.getString(R.string.app_name) + " " +  context.getString(R.string.permission_notif_text);
        int    smallIconRes = R.mipmap.ic_warning_white;
        int    largeIconRes = 0;
        int    notifId      = 0;

        switch (type) {
            case GPS: {
                channelId = NOTIF_CHANNEL_ID_GPS;
                channelTitle = context.getString(R.string.gps_notif_title);
                notifTitle = context.getString(R.string.gps_notif_title);
                largeIconRes = R.mipmap.ic_location_off_black;
                notifId = NOTIF_ID_GPS;
                break;
            }
            case PERMISSION: {
                channelId = NOTIF_CHANNEL_ID_PERMISSION;
                channelTitle = context.getString(R.string.permission_notif_title);
                notifTitle = context.getString(R.string.permission_notif_title);
                largeIconRes = R.mipmap.ic_location_off_black;
                notifId = NOTIF_ID_PERMISSION;
                break;
            }
            case POWER: {
                channelId = NOTIF_CHANNEL_ID_POWER;
                channelTitle = context.getString(R.string.power_notif_title);
                notifTitle = context.getString(R.string.power_notif_title);
                largeIconRes = R.mipmap.ic_battery_alert_black;
                notifId = NOTIF_ID_POWER;
                break;
            }
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final NotificationChannel channel = new NotificationChannel(channelId, channelTitle, NotificationManager.IMPORTANCE_HIGH);
            channel.setShowBadge(true);
            notificationManager.createNotificationChannel(channel);
        }

        final Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), largeIconRes);

        final Notification note = new NotificationCompat.Builder(context, channelId)
                .setContentTitle(notifTitle)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(notifText))
                .setSmallIcon(smallIconRes)
                .setLargeIcon(largeIcon)
                .setVisibility(VISIBILITY_SECRET)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .build();

        notificationManager.notify(notifId, note);

    }

    public static void hide(final Context context, final Type type) {
        final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        switch (type) {
            case GPS: notificationManager.cancel(NOTIF_ID_GPS); break;
            case PERMISSION: notificationManager.cancel(NOTIF_ID_PERMISSION); break;
            case POWER: notificationManager.cancel(NOTIF_ID_POWER); break;
        }
    }
}
