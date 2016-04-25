package pl.appnode.roy;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import com.firebase.client.Firebase;

import static pl.appnode.roy.Constants.BATTERY_CHARGING;
import static pl.appnode.roy.Constants.BATTERY_CHECK_ERROR;
import static pl.appnode.roy.Constants.BATTERY_DISCHARGING;
import static pl.appnode.roy.Constants.BATTERY_NOT_PLUGGED;
import static pl.appnode.roy.Constants.BATTERY_PLUGGED_AC;
import static pl.appnode.roy.Constants.BATTERY_PLUGGED_USB;
import static pl.appnode.roy.Constants.BATTERY_PLUGGED_WIRELESS;


public class RemoteUpdateService extends Service {
    private static final String LOGTAG = "Service";

    @Override
    public void onCreate() {
        super.onCreate();
        Firebase mFireRef = new Firebase(BuildConfig.FB_BASE_ADDRESS);
        Firebase localBatteryRef = mFireRef.child("devices").child(readLocalBatteryStatus().batteryDeviceId);
        localBatteryRef.setValue(readLocalBatteryStatus());
    }

    private BatteryItem readLocalBatteryStatus() {
        BatteryItem localBattery = new BatteryItem();;
        localBattery.batteryDeviceName = MainActivity.getDeviceName();
        localBattery.batteryDeviceId = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        Log.d(LOGTAG, "Device ID: " + localBattery.batteryDeviceId);
        localBattery.batteryCheckTime = System.currentTimeMillis();
        Intent getBattery = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        // Reads battery level data
        int level = getBattery.getIntExtra(BatteryManager.EXTRA_LEVEL, BATTERY_CHECK_ERROR);
        int scale = getBattery.getIntExtra(BatteryManager.EXTRA_SCALE, BATTERY_CHECK_ERROR);
        if (level == BATTERY_CHECK_ERROR || scale == BATTERY_CHECK_ERROR) {
            localBattery.batteryLevel = BATTERY_CHECK_ERROR;
        } else localBattery.batteryLevel = (int)((level / (float)scale) * 100);
        // Checks if device is plugged
        int pluggedStatus = getBattery.getIntExtra(BatteryManager.EXTRA_PLUGGED, BATTERY_CHECK_ERROR);
        int batteryPluggedStatus;
        switch (pluggedStatus) {
            case BatteryManager.BATTERY_PLUGGED_USB:
                batteryPluggedStatus = BATTERY_PLUGGED_USB;
                break;
            case BatteryManager.BATTERY_PLUGGED_AC:
                batteryPluggedStatus = BATTERY_PLUGGED_AC;
                break;
            case BatteryManager.BATTERY_PLUGGED_WIRELESS:
                batteryPluggedStatus = BATTERY_PLUGGED_WIRELESS;
                break;
            case BATTERY_CHECK_ERROR:
                batteryPluggedStatus = BATTERY_CHECK_ERROR;
                break;
            default:
                batteryPluggedStatus = BATTERY_NOT_PLUGGED;
        }
        localBattery.batteryPluggedStatus =  batteryPluggedStatus;
        // Checks if battery is charging/discharging
        int chargingStatus = getBattery.getIntExtra(BatteryManager.EXTRA_STATUS, BATTERY_CHECK_ERROR);
        int batteryChargeStatus;
        switch (chargingStatus) {
            case BatteryManager.BATTERY_STATUS_CHARGING:
                batteryChargeStatus = BATTERY_CHARGING;
                break;
            case BatteryManager.BATTERY_STATUS_DISCHARGING:
                batteryChargeStatus = BATTERY_DISCHARGING;
                break;
            default:
                batteryChargeStatus = BATTERY_CHECK_ERROR;
        }
        localBattery.batteryChargingStatus = batteryChargeStatus;
        return localBattery;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}
