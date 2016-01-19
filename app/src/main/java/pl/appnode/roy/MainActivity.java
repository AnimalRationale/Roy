package pl.appnode.roy;

import android.animation.ObjectAnimator;
import android.app.ActionBar;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.BatteryManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;

import static pl.appnode.roy.Constants.BATTERY_CHARGING;
import static pl.appnode.roy.Constants.BATTERY_CHECK_ERROR;
import static pl.appnode.roy.Constants.BATTERY_DISCHARGING;
import static pl.appnode.roy.Constants.BATTERY_NOT_PLUGGED;
import static pl.appnode.roy.Constants.BATTERY_PLUGGED_AC;
import static pl.appnode.roy.Constants.BATTERY_PLUGGED_USB;
import static pl.appnode.roy.Constants.BATTERY_PLUGGED_WIRELESS;


public class MainActivity extends AppCompatActivity {

    private static final String LOGTAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setIcon(R.mipmap.ic_launcher);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        showBatteryLevel();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_about) {
            AboutDialog.showDialog(MainActivity.this);
        }
        return super.onOptionsItemSelected(item);
    }

    public void showBatteryLevelButton(View button) {
        showBatteryLevel();
    }

    public void showBatteryLevel() {
        int batteryLevelValue = getBatteryLevel();
        int indicatorColor;
        if (batteryLevelValue > 49) {
            indicatorColor = argbColor(ContextCompat.getColor(this,R.color.colorPrimary));
        } else
        if (batteryLevelValue > 25) {
            indicatorColor = argbColor(ContextCompat.getColor(this,R.color.colorWarning));
        }
        else
            indicatorColor = argbColor(ContextCompat.getColor(this,R.color.colorAccent));
        ProgressBar batteryLevelIndicator = (ProgressBar) findViewById(R.id.battery_progress_bar);
        batteryLevelIndicator.getProgressDrawable().setColorFilter(indicatorColor, PorterDuff.Mode.SRC_IN);
        batteryLevelIndicatorAnimation(batteryLevelIndicator, batteryLevelValue);
        TextView batteryLevel = (TextView) findViewById(R.id.text_battery_level);
        batteryLevel.setText(batteryLevelValue
                + getString(R.string.percent));
        TextView batteryPlugged = (TextView) findViewById(R.id.text_battery_plugged);
        switch (getBatteryPluggedStatus()) {
            case BATTERY_PLUGGED_AC:
                batteryPlugged.setText(getString(R.string.battery_plugged_ac));
                break;
            case BATTERY_PLUGGED_USB:
                batteryPlugged.setText(getString(R.string.battery_plugged_usb));
                break;
            case BATTERY_PLUGGED_WIRELESS:
                batteryPlugged.setText(getString(R.string.battery_plugged_wireless));
                break;
            case BATTERY_CHECK_ERROR:
                batteryPlugged.setText(getString(R.string.error_battery_check));
                break;
            default:
                batteryPlugged.setText(getString(R.string.battery_not_plugged));
        }
        TextView batteryCharge = (TextView) findViewById(R.id.text_battery_charge_status);
        switch (getBatteryChargingStatus()) {
            case BATTERY_CHARGING:
                batteryCharge.setText(getString(R.string.battery_charging));
                break;
            case BATTERY_DISCHARGING:
                batteryCharge.setText(getString(R.string.battery_discharging));
                break;
            default:
                 batteryCharge.setText(getString(R.string.error_battery_check));
        }
    }

    private Intent getBattery() {
        Intent batteryIntent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        return batteryIntent;
    }

    private int getBatteryLevel() {
        int level = getBattery().getIntExtra(BatteryManager.EXTRA_LEVEL, BATTERY_CHECK_ERROR);
        int scale = getBattery().getIntExtra(BatteryManager.EXTRA_SCALE, BATTERY_CHECK_ERROR);
        if (level == BATTERY_CHECK_ERROR || scale == BATTERY_CHECK_ERROR) {
            return BATTERY_CHECK_ERROR;
        }
        return (int)((level / (float)scale) * 100);
    }

    private int getBatteryPluggedStatus() {
        int status = getBattery().getIntExtra(BatteryManager.EXTRA_PLUGGED, BATTERY_CHECK_ERROR);
        int batteryPluggedStatus;
        switch (status) {
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
        return batteryPluggedStatus;
    }

    private int getBatteryChargingStatus() {
        int status = getBattery().getIntExtra(BatteryManager.EXTRA_STATUS, BATTERY_CHECK_ERROR);
        int batteryChargeStatus;
        switch (status) {
            case BatteryManager.BATTERY_STATUS_CHARGING:
                batteryChargeStatus = BATTERY_CHARGING;
                break;
            case BatteryManager.BATTERY_STATUS_DISCHARGING:
                batteryChargeStatus = BATTERY_DISCHARGING;
                break;
            default:
                batteryChargeStatus = BATTERY_CHECK_ERROR;
        }
        return batteryChargeStatus;
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
}
