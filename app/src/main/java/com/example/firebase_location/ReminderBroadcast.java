package com.example.firebase_location;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.content.ContextCompat;

public class ReminderBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Start the ForegroundService with an extra indicating that the location update is required
        Intent serviceIntent = new Intent(context, ForegroundService.class);
        serviceIntent.putExtra("update_location", true);
        ContextCompat.startForegroundService(context, serviceIntent);

        // Reschedule the alarm for the next firing
        rescheduleAlarm(context);

    }
    private void rescheduleAlarm(Context context) {
        Intent intent = new Intent(context, ReminderBroadcast.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager != null) {
            long timeInMillis = System.currentTimeMillis() + (long) (7 * 1000); // 7.5 minutes in milliseconds
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
        }
    }


}