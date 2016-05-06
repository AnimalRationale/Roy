package pl.appnode.roy;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import static pl.appnode.roy.Constants.SET_WAKE_UP_ALARM;
import static pl.appnode.roy.Constants.CANCEL_WAKE_UP_ALARM;
import static pl.appnode.roy.Constants.ACTION_BATTERY_STATUS_UPLOAD;

/**
 * Sets or cancels WakeUpAlarm for local battery status upload 
 */

public class WakeUpAlarmHelper {

    private static final String LOGTAG = "WakeUpAlarmHelper";

    public static void alarmManager(int uploadRepeatTime, int command) {
        Intent alarmIntent = new Intent(AppContextHelper.getContext(), WakeUpAlarmReceiver.class);
        alarmIntent.setAction(ACTION_BATTERY_STATUS_UPLOAD);
        PendingIntent alarmWakeIntent = PendingIntent.getBroadcast(
                AppContextHelper.getContext(), 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) AppContextHelper.getContext()
                .getSystemService(Context.ALARM_SERVICE);
        if (command == SET_WAKE_UP_ALARM) {
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + (uploadRepeatTime * 60 * 1000),
                    (uploadRepeatTime * 60 * 1000), alarmWakeIntent);
            Log.d(LOGTAG, "Setting repeating WakeUp alarm for time in minutes: "
                    + uploadRepeatTime);
        } else if (command == CANCEL_WAKE_UP_ALARM) {
            alarmManager.cancel(alarmWakeIntent);
            Log.d(LOGTAG, "Cancelled WakeUp alarm for local battery status upload.");
        }
    }
}

