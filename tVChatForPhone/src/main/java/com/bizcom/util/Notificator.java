package com.bizcom.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

import com.shdx.tvchat.phone.R;

public class Notificator {

    public static long lastNotificatorTime = 0;
    public static long currentRmoteID = 0;

    public static void updateSystemNotification(Context context, String title, String content, boolean IsTone,
                                                Intent trigger, int notificationID, long remoteID) {
        currentRmoteID = remoteID;
        if (IsTone && ((System.currentTimeMillis() / 1000) - lastNotificatorTime) > 2) {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(context, notification);
            r.play();
            lastNotificatorTime = System.currentTimeMillis() / 1000;
        }

        NotificationManager mNotificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent notifyPendingIntent = PendingIntent.getActivities(context, 0, new Intent[]{trigger},
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Creates the PendingIntent
        Notification.Builder builder = new Notification.Builder(context)
                .setContentTitle(title).setContentText(content).setAutoCancel(true).
                        setSmallIcon(R.drawable.ic_launcher).
                        setOngoing(true).
                        setTicker(content).
                        setDefaults(Notification.DEFAULT_VIBRATE).
                        setWhen(System.currentTimeMillis()).setContentIntent(notifyPendingIntent);

        Notification build = builder.getNotification();
        // mId allows you to update the notification later on.
        mNotificationManager.cancelAll();
        mNotificationManager.notify(notificationID, build);
    }

    public static void cancelSystemNotification(Context context, int nId) {
        NotificationManager mNotificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.cancel(nId);
    }

    public static void cancelAllSystemNotification(Context context) {
        NotificationManager mNotificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();
    }

//    public static void udpateApplicationNotification(Context context, boolean flag, Intent intent) {
//        NotificationManager mNotificationManager = (NotificationManager) context
//                .getSystemService(Context.NOTIFICATION_SERVICE);
//        if (flag) {
//            PendingIntent notifyPendingIntent = PendingIntent.getActivities(context, 0, new Intent[]{intent},
//                    PendingIntent.FLAG_UPDATE_CURRENT);
//
//            NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
//                    .setSmallIcon(R.drawable.ic_launcher).setContentTitle(context.getText(R.string.app_name))
//                    .setContentText(context.getText(R.string.status_bar_title)).setAutoCancel(false)
//                    .setContentIntent(notifyPendingIntent);
//            Notification noti = builder.build();
//            noti.flags |= Notification.FLAG_NO_CLEAR;
//
//            mNotificationManager.notify(V2GlobalConstants.APPLICATION_STATUS_BAR_NOTIFICATION, noti);
//        } else {
//            mNotificationManager.cancel(V2GlobalConstants.APPLICATION_STATUS_BAR_NOTIFICATION);
//
//        }
//    }
}
