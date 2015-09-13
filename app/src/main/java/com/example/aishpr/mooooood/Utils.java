package com.example.aishpr.mooooood;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by ridhisurana on 9/13/15.
 */
public class Utils {

    public static void setAlarm(Context context) {
        Log.d("ALARMSET", "Setting alarm");
        Intent myIntent = new Intent(context, MyAlarmService.class);

        PendingIntent pendingIntent = PendingIntent.getService(context, 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        // With setInexactRepeating(), you have to use one of the AlarmManager interval
        // constants--in this case, AlarmManager.INTERVAL_DAY.
        alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis() + 5000,
                300000, pendingIntent);
        Log.d("ALARMSET", "Set alarm");
    }
}
