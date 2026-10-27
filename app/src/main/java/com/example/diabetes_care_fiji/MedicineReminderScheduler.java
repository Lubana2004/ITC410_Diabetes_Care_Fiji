package com.example.diabetes_care_fiji;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MedicineReminderScheduler {

    public static void scheduleDaily(
            Context context,
            long medicineId,
            String medicineName,
            String dosage,
            String reminderTime) {

        if (reminderTime == null || reminderTime.trim().isEmpty()) {
            return;
        }

        SimpleDateFormat format = new SimpleDateFormat(
                "hh:mm a",
                Locale.getDefault()
        );

        format.setLenient(false);

        Date parsedTime;

        try {
            parsedTime = format.parse(reminderTime.trim());
        } catch (ParseException e) {
            return;
        }

        if (parsedTime == null) {
            return;
        }

        Calendar selectedTime = Calendar.getInstance();
        Calendar parsedCalendar = Calendar.getInstance();

        parsedCalendar.setTime(parsedTime);

        selectedTime.set(
                Calendar.HOUR_OF_DAY,
                parsedCalendar.get(Calendar.HOUR_OF_DAY)
        );

        selectedTime.set(
                Calendar.MINUTE,
                parsedCalendar.get(Calendar.MINUTE)
        );

        selectedTime.set(Calendar.SECOND, 0);
        selectedTime.set(Calendar.MILLISECOND, 0);

        // If today's time has passed, schedule for tomorrow.
        if (!selectedTime.after(Calendar.getInstance())) {
            selectedTime.add(Calendar.DAY_OF_YEAR, 1);
        }

        Intent intent = new Intent(
                context,
                MedicineReminderReceiver.class
        );

        intent.putExtra("medicineId", medicineId);
        intent.putExtra("medicineName", medicineName);
        intent.putExtra("dosage", dosage);
        intent.putExtra("reminderTime", reminderTime);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) medicineId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
                        | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(
                        Context.ALARM_SERVICE
                );

        if (alarmManager == null) {
            return;
        }

        long triggerTime = selectedTime.getTimeInMillis();

        try {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

                if (alarmManager.canScheduleExactAlarms()) {

                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent
                    );

                } else {

                    // Fallback when exact-alarm access is unavailable.
                    alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent
                    );
                }

            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                );

            } else {

                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                );
            }

        } catch (SecurityException e) {

            // Fall back to an inexact alarm if exact scheduling
            // is not permitted.
            try {
                alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                );
            } catch (SecurityException ignored) {
                // The reminder could not be scheduled.
            }
        }
    }

    public static void cancel(Context context, long medicineId) {

        Intent intent = new Intent(
                context,
                MedicineReminderReceiver.class
        );

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) medicineId,
                intent,
                PendingIntent.FLAG_NO_CREATE
                        | PendingIntent.FLAG_IMMUTABLE
        );

        if (pendingIntent != null) {

            AlarmManager alarmManager =
                    (AlarmManager) context.getSystemService(
                            Context.ALARM_SERVICE
                    );

            if (alarmManager != null) {
                alarmManager.cancel(pendingIntent);
            }

            pendingIntent.cancel();
        }
    }
}