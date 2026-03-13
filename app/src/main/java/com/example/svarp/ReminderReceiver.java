package com.example.svarp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.SharedPreferences;
import java.util.Calendar;
import android.content.BroadcastReceiver;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import java.util.Random;

public class ReminderReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "health_reminder_channel";

    private final String[] tips = {
            "💧 Drink 8 glasses of water today — dehydration causes more headaches than you think.",
            "🚶 A 10-minute walk after meals improves digestion and keeps your energy stable.",
            "🧘 Take 5 deep breaths right now. Inhale for 4 seconds, hold for 4, exhale for 4.",
            "🥗 Try to add one fruit or vegetable to your next meal — small changes add up.",
            "😴 Aim for 7–9 hours of sleep tonight. Your immune system repairs itself while you rest.",
            "🧼 Wash your hands before your next meal — it's the simplest way to avoid infections.",
            "📵 Take a 20-minute break from your screen today. Your eyes and mind will thank you.",
            "☀️ Step outside for 15 minutes today — sunlight boosts your vitamin D and mood.",
            "🍵 Swap one tea or coffee today for warm water with lemon — great for digestion.",
            "🧍 Check your posture right now. Sit straight, relax your shoulders, unclench your jaw.",
            "🫁 If you've had a cough or cold this week, stay warm and avoid cold drinks today.",
            "🩺 Haven't had a health check-up in a while? Consider booking one soon.",
            "🥣 Don't skip breakfast — it stabilises your blood sugar and keeps you focused.",
            "💤 Try to sleep and wake at the same time every day — consistency improves sleep quality.",
            "🏃 Even 15 minutes of movement a day significantly reduces your risk of heart disease."
    };

    private final Random random = new Random();

    @Override
    public void onReceive(Context context, Intent intent) {
        createChannel(context);
        String tip = tips[random.nextInt(tips.length)];

        Intent openApp = new Intent(context, Main_Screen.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, openApp,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.logo)
                        .setContentTitle("Daily Health Tip 💊")
                        .setContentText(tip)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);

        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1, builder.build());

        // Reschedule for next day
        SharedPreferences prefs = context.getSharedPreferences(
                Main_Screen.PREFS_NAME, Context.MODE_PRIVATE);
        boolean enabled = prefs.getBoolean(Main_Screen.KEY_TOGGLE, false);
        if (enabled) {
            int hour = prefs.getInt(Main_Screen.KEY_HOUR, 8);
            int minute = prefs.getInt(Main_Screen.KEY_MINUTE, 0);

            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent alarmIntent = new Intent(context, ReminderReceiver.class);
            PendingIntent pi = PendingIntent.getBroadcast(
                    context, 0, alarmIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, 1);
            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.MINUTE, minute);
            cal.set(Calendar.SECOND, 0);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (am.canScheduleExactAlarms()) {
                    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
                } else {
                    am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
                }
            } else {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
            }
        }
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