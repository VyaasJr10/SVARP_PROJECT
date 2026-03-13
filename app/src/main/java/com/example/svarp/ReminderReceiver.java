package com.example.svarp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import java.util.Random;

public class ReminderReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "health_reminder_channel";

    private final String[] tips = {
            "Drink enough water today.",
            "Take a short walk to refresh your mind.",
            "Stretch your body for a few minutes.",
            "Eat fruits and vegetables for better health.",
            "Take a deep breath and relax."
    };

    private final Random random = new Random();

    @Override
    public void onReceive(Context context, Intent intent) {

        createChannel(context);

        String tip = tips[random.nextInt(tips.length)];

        // When user taps notification → open Main Screen
        Intent openApp = new Intent(context, Main_Screen.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                openApp,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.logo)
                        .setContentTitle("Daily Health Tip")
                        .setContentText(tip)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);

        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        manager.notify(random.nextInt(1000), builder.build());
    }

    private void createChannel(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Health Reminder",
                    NotificationManager.IMPORTANCE_HIGH
            );

            NotificationManager manager =
                    context.getSystemService(NotificationManager.class);

            manager.createNotificationChannel(channel);
        }
    }
}