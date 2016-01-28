package pl.appnode.roy;

import android.animation.ObjectAnimator;
import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import static pl.appnode.roy.Constants.BATTERY_CHARGING;
import static pl.appnode.roy.Constants.BATTERY_CHECK_ERROR;
import static pl.appnode.roy.Constants.BATTERY_DISCHARGING;
import static pl.appnode.roy.Constants.BATTERY_NOT_PLUGGED;
import static pl.appnode.roy.Constants.BATTERY_PLUGGED_AC;
import static pl.appnode.roy.Constants.BATTERY_PLUGGED_USB;
import static pl.appnode.roy.Constants.BATTERY_PLUGGED_WIRELESS;
import static pl.appnode.roy.PreferencesSetupHelper.isDarkTheme;
import static pl.appnode.roy.PreferencesSetupHelper.isTransitionsOn;
import static pl.appnode.roy.PreferencesSetupHelper.orientationSetup;
import static pl.appnode.roy.PreferencesSetupHelper.themeSetup;


public class MainActivity extends AppCompatActivity {

    private static final String LOGTAG = "MainActivity";
    private int mBatteryIndicatorAnimationCounter;
    private BatteryItem localBattery = new BatteryItem();
    private static boolean sThemeChangeFlag;

    private final BroadcastReceiver mPowerConnectionBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            showBatteryLevel();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        themeSetup(this);
        sThemeChangeFlag = isDarkTheme(this);
        setContentView(R.layout.activity_main);
        if (isDarkTheme(this)) {
            getWindow().getDecorView().setBackgroundColor(ContextCompat.getColor(this, R.color.colorBlack));
        } else {getWindow().getDecorView().setBackgroundColor(ContextCompat.getColor(this, R.color.colorWhite));}
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setIcon(R.mipmap.ic_launcher);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        orientationSetup(this);
        checkThemeChange();
        mBatteryIndicatorAnimationCounter = 0;
        showBatteryLevel();
        IntentFilter screenStatusIntentFilter = new IntentFilter();
        screenStatusIntentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(mPowerConnectionBroadcastReceiver, new IntentFilter(screenStatusIntentFilter));
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mPowerConnectionBroadcastReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, PreferencesActivity.class);
            this.startActivity(settingsIntent);
        }
        if (id == R.id.action_about) {
            AboutDialog.showDialog(MainActivity.this);
        }
        return super.onOptionsItemSelected(item);
    }

    public void showBatteryLevelButton(View button) {
        showBatteryLevel();
    }

    public void showBatteryLevel() {
        readLocalBatteryStatus();
        int batteryLevelValue = localBattery.batteryLevel;
        int indicatorColor;
        int indicatorBackgroundColor;
        int iconDefaultColor;
        if (batteryLevelValue > 49) {
            indicatorColor = argbColor(ContextCompat.getColor(this,R.color.colorLightBlue));
        } else
        if (batteryLevelValue > 25) {
            indicatorColor = argbColor(ContextCompat.getColor(this, R.color.colorWarning));
        }
        else
            indicatorColor = argbColor(ContextCompat.getColor(this, R.color.colorAccent));
        if (isDarkTheme(this)) {
            iconDefaultColor = indicatorBackgroundColor = argbColor(ContextCompat
                    .getColor(this, R.color.colorBatteryLevelProgressBarInitialDark));
        } else {
            iconDefaultColor = indicatorBackgroundColor = argbColor(ContextCompat
                    .getColor(this, R.color.colorBatteryLevelProgressBarInitialLight));
        }
        ProgressBar batteryLevelIndicator = (ProgressBar) findViewById(R.id.battery_progress_bar);
        batteryLevelIndicator.getProgressDrawable().setColorFilter(indicatorColor, PorterDuff.Mode.SRC_IN);
        batteryLevelIndicator.getBackground().setColorFilter(indicatorBackgroundColor, PorterDuff.Mode.SRC_IN);
        if (isTransitionsOn(this) && mBatteryIndicatorAnimationCounter == 0) {
            mBatteryIndicatorAnimationCounter = 1;
            batteryLevelIndicatorAnimation(batteryLevelIndicator, batteryLevelValue);
        } else batteryLevelIndicator.setProgress(batteryLevelValue);
        TextView batteryLevel = (TextView) findViewById(R.id.text_battery_level);
        batteryLevel.setText(batteryLevelValue
                + getString(R.string.percent));
        ImageView batteryCharging = (ImageView) findViewById(R.id.icon_battery_charging);
        batteryCharging.getDrawable().setColorFilter(iconDefaultColor, PorterDuff.Mode.SRC_IN);
        ImageView batteryPluggedWireless = (ImageView) findViewById(R.id.icon_plugged_wireless);
        batteryPluggedWireless.getDrawable().setColorFilter(iconDefaultColor, PorterDuff.Mode.SRC_IN);
        ImageView batteryPluggedPower = (ImageView) findViewById(R.id.icon_plugged_ac);
        batteryPluggedPower.getDrawable().setColorFilter(iconDefaultColor, PorterDuff.Mode.SRC_IN);
        ImageView batteryPluggedUSB = (ImageView) findViewById(R.id.icon_plugged_usb);
        batteryPluggedUSB.getDrawable().setColorFilter(iconDefaultColor, PorterDuff.Mode.SRC_IN);
        TextView batteryPlugged = (TextView) findViewById(R.id.text_battery_plugged);
        switch (localBattery.batteryPluggedStatus) {
            case BATTERY_PLUGGED_AC:
                batteryPlugged.setText(getString(R.string.battery_plugged_ac));
                batteryPluggedPower.getDrawable().setColorFilter(indicatorColor, PorterDuff.Mode.SRC_IN);
                break;
            case BATTERY_PLUGGED_USB:
                batteryPlugged.setText(getString(R.string.battery_plugged_usb));
                batteryPluggedUSB.getDrawable().setColorFilter(indicatorColor, PorterDuff.Mode.SRC_IN);
                break;
            case BATTERY_PLUGGED_WIRELESS:
                batteryPlugged.setText(getString(R.string.battery_plugged_wireless));
                batteryPluggedWireless.getDrawable().setColorFilter(indicatorColor, PorterDuff.Mode.SRC_IN);
                break;
            case BATTERY_CHECK_ERROR:
                batteryPlugged.setText(getString(R.string.error_battery_check));
                break;
            default:
                batteryPlugged.setText(getString(R.string.battery_not_plugged));
        }
        TextView batteryCharge = (TextView) findViewById(R.id.text_battery_charge_status);
        switch (localBattery.batteryChargingStatus) {
            case BATTERY_CHARGING:
                batteryCharge.setText(getString(R.string.battery_charging));
                batteryCharging.getDrawable().setColorFilter(indicatorColor, PorterDuff.Mode.SRC_IN);
                break;
            case BATTERY_DISCHARGING:
                batteryCharge.setText(getString(R.string.battery_discharging));
                batteryCharging.getDrawable().setColorFilter(iconDefaultColor, PorterDuff.Mode.SRC_IN);
                break;
            default:
                 batteryCharge.setText(getString(R.string.not_available));
        }
    }

    private void readLocalBatteryStatus() {
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
    }

    private void batteryLevelIndicatorAnimation(final ProgressBar progressBar, int batteryLevel) {
            ObjectAnimator animation = ObjectAnimator.ofInt(progressBar, "progress", 0, batteryLevel);
            animation.setInterpolator(new AccelerateInterpolator());
            animation.setDuration(700);
            animation.start();
    }

    private int argbColor(int colorResource) {
        int color = Color.argb(Color.alpha(colorResource),
                Color.red(colorResource),
                Color.green(colorResource),
                Color.blue(colorResource));
        return color;
    }

    /**
     * Checks if device has available network connection.
     *
     * @return true if device has available network connection
     */
    private boolean isConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if ((networkInfo == null) || (!networkInfo.isConnected())) {
            Toast toast = Toast.makeText(this,
                    R.string.error_network_access, Toast.LENGTH_SHORT);
            toast.show();
            return false;
        }
        return true;
    }

    private void checkThemeChange() { // Restarts activity if user changed theme
        if (sThemeChangeFlag != isDarkTheme(this)) {
            finish();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

}
