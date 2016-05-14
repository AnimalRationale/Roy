package pl.appnode.roy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import static pl.appnode.roy.PreferencesSetupHelper.uploadAlarmSetup;

public class BootCompletedReceiver extends BroadcastReceiver {

    private static final String LOGTAG = "BootCompletedReceiver";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.d(LOGTAG, "Boot completed, setting repeating alarm for upload.");
        uploadAlarmSetup(context);
        Log.d(LOGTAG, "Boot completed, starting service for battery status upload.");
        Intent serviceIntent = new Intent(context, RemoteUpdateService.class);
        context.startService(serviceIntent);
    }
}