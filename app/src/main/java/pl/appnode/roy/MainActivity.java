package pl.appnode.roy;

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
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static final String LOGTAG = "MainActivity";

    private static final int BATTERY_CHECK_ERROR = -1;

    private static final int BATTERY_NOT_PLUGGED = 0;
    private static final int BATTERY_PLUGGED_AC = 1;
    private static final int BATTERY_PLUGGED_USB = 2;
    private static final int BATTERY_PLUGGED_WIRELESS = 3;

    private static final int BATTERY_MAX_LEVEL = 100;

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
        // Set battery level indicator to full circle with "empty" color, indicator atm should show
        // actual bettery level only after button click
        ProgressBar batteryLevelIndicator = (ProgressBar) findViewById(R.id.battery_progress_bar);
        batteryLevelIndicator.setProgress(BATTERY_MAX_LEVEL);
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

    public void showBatteryLevel(View button) {
        int batteryLevelValue = getBatteryLevel();
        int indicatorColor = argbColor(ContextCompat.getColor(this,R.color.colorPrimary));
        ProgressBar batteryLevelIndicator = (ProgressBar) findViewById(R.id.battery_progress_bar);
        batteryLevelIndicator.getProgressDrawable().setColorFilter(indicatorColor, PorterDuff.Mode.SRC_IN);
        batteryLevelIndicator.setProgress(batteryLevelValue);
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
            default: BATTERY_NOT_PLUGGED:
                batteryPlugged.setText(getString(R.string.battery_not_plugged));
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

    private int argbColor(int colorResource) {
        int color = Color.argb(Color.alpha(colorResource),
                Color.red(colorResource),
                Color.green(colorResource),
                Color.blue(colorResource));
        return color;
    }
}
