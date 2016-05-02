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

    public static void alarmManager(Long uploadRepeatTime, int command) {
        Intent alarmIntent = new Intent(AppContextHelper.getContext(), WakeUpAlarmReceiver.class);
        alarmIntent.setAction(ACTION_BATTERY_STATUS_UPLOAD);
        PendingIntent alarmWakeIntent = PendingIntent.getBroadcast(
                AppContextHelper.getContext(), 0, alarmIntent, 0);
        AlarmManager alarmManager = (AlarmManager) AppContextHelper.getContext()
                .getSystemService(Context.ALARM_SERVICE);
        if (command == SET_WAKE_UP_ALARM) {
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + uploadRepeatTime, uploadRepeatTime,
                    alarmWakeIntent);
            Log.d(LOGTAG, "Setting repeating WakeUp alarm for time in seconds: "
                    + uploadRepeatTime / 1000);
        } else if (command == CANCEL_WAKE_UP_ALARM) {
            alarmManager.cancel(alarmWakeIntent);
            Log.d(LOGTAG, "Cancelled WakeUp alarm for local battery status upload.");
        }
    }
}

