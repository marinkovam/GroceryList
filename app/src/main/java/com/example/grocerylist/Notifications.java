package com.example.grocerylist;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import static android.content.Context.NOTIFICATION_SERVICE;

public class Notifications extends BroadcastReceiver {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("assert", "in onReceive");

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        NotificationChannel channel = createNotificationChannel(
                "channel_id",
                "Notification Channel",
                "channel for push notifications for grocery manager",
                notificationManager);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "channel_id");
        Bundle extras = intent.getExtras();
        if (extras == null) {
            return;
        }
        String itemName    = extras.getString("itemName");
        String expiryDate  = extras.getString("expiryDate");
        String whereAbouts = extras.getString("whereAbouts");
        builder.setContentTitle(itemName + " will expire on " + expiryDate );
        builder.setContentText("Its whereabouts are " + whereAbouts );
        builder.setSmallIcon(R.drawable.baseline_add_black_36);
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));
        builder.setAutoCancel(true);

        Intent mainActivityIntent = new Intent(context, MainActivity.class);
        mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, mainActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        Notification notification = builder.build();
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(101, notification);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public NotificationChannel createNotificationChannel(String id, String name,
                                                         String description, NotificationManager notificationManager) {

        Log.i("info", "Inside createNotificationChannel");
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(NotificationChannel.DEFAULT_CHANNEL_ID, "com.grocerymanager", importance);
            channel.setDescription(description);
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.enableVibration(true);
            channel.setVibrationPattern(
                    new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationManager.createNotificationChannel(channel);
            return channel;
        }
        return null;
    }
}

