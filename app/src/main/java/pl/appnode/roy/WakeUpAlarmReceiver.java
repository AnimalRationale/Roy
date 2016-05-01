package pl.appnode.roy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class WakeUpAlarmReceiver extends BroadcastReceiver {

    private static final String LOGTAG = "WakeUpAlarmReceiver";

    @Override
    public void onReceive(Context context, Intent alarmIntent) {
        Log.d(LOGTAG, "Starting service on alarm");
    }
}