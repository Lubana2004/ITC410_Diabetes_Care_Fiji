package com.example.diabetes_care_fiji;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class MedicineReminderReceiver extends BroadcastReceiver {

    public static final String CHANNEL_ID = "medicine_reminders";

    @Override
    public void onReceive(Context context, Intent intent) {

        long medicineId = intent.getLongExtra("medicineId", 0);
        String medicineName = intent.getStringExtra("medicineName");
        String dosage = intent.getStringExtra("dosage");
        String reminderTime = intent.getStringExtra("reminderTime");

        if (medicineName == null) {
            medicineName = "Your medicine";
        }

        if (dosage == null) {
            dosage = "";
        }

        if (reminderTime == null) {
            reminderTime = "";
        }

        createNotificationChannel(context);

        Intent openApp = new Intent(context, Medicine.class);
        openApp.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP
        );

        PendingIntent contentIntent = PendingIntent.getActivity(
                context,
                (int) medicineId,
                openApp,
                PendingIntent.FLAG_UPDATE_CURRENT
                        | PendingIntent.FLAG_IMMUTABLE
        );

        String message = "It's time to take " + medicineName;

        if (!dosage.isEmpty()) {
            message += " (" + dosage + ")";
        }

        if (!reminderTime.isEmpty()) {
            message += ". Scheduled time: " + reminderTime;
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(
                                android.R.drawable.ic_dialog_info
                        )
                        .setContentTitle("Medicine Reminder")
                        .setContentText(message)
                        .setStyle(
                                new NotificationCompat.BigTextStyle()
                                        .bigText(message)
                        )
                        .setPriority(
                                NotificationCompat.PRIORITY_HIGH
                        )
                        .setCategory(NotificationCompat.CATEGORY_REMINDER)
                        .setAutoCancel(true)
                        .setContentIntent(contentIntent);

        try {
            NotificationManagerCompat.from(context)
                    .notify((int) medicineId, builder.build());
        } catch (SecurityException e) {
            // Notifications may be disabled by the user.
        }

        // Schedule the next daily reminder.
        MedicineReminderScheduler.scheduleDaily(
                context,
                medicineId,
                medicineName,
                dosage,
                reminderTime
        );
    }

    private void createNotificationChannel(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Medicine Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );

            channel.setDescription(
                    "Notifications for scheduled medicine reminders"
            );

            NotificationManager manager =
                    context.getSystemService(NotificationManager.class);

            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}