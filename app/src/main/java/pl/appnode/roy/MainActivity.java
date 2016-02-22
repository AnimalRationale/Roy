package pl.appnode.roy;

import android.accounts.AccountManager;
import android.animation.ObjectAnimator;
import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static pl.appnode.roy.Constants.ACCOUNT_HINT_TIME;
import static pl.appnode.roy.Constants.BATTERY_CHARGING;
import static pl.appnode.roy.Constants.BATTERY_CHECK_ERROR;
import static pl.appnode.roy.Constants.BATTERY_DISCHARGING;
import static pl.appnode.roy.Constants.BATTERY_NOT_PLUGGED;
import static pl.appnode.roy.Constants.BATTERY_PLUGGED_AC;
import static pl.appnode.roy.Constants.BATTERY_PLUGGED_USB;
import static pl.appnode.roy.Constants.BATTERY_PLUGGED_WIRELESS;
import static pl.appnode.roy.Constants.DAY_IN_MILLIS;
import static pl.appnode.roy.Constants.HOUR_IN_MILLIS;
import static pl.appnode.roy.Constants.MINUTE_IN_MILLIS;
import static pl.appnode.roy.Constants.PREF_ACCOUNT_NAME;
import static pl.appnode.roy.Constants.REQUEST_ACCOUNT_PICKER;
import static pl.appnode.roy.Constants.REQUEST_AUTHORIZATION;
import static pl.appnode.roy.Constants.REQUEST_GOOGLE_PLAY_SERVICES;
import static pl.appnode.roy.PreferencesSetupHelper.isDarkTheme;
import static pl.appnode.roy.PreferencesSetupHelper.isTransitionsOn;
import static pl.appnode.roy.PreferencesSetupHelper.orientationSetup;
import static pl.appnode.roy.PreferencesSetupHelper.themeSetup;


public class MainActivity extends AppCompatActivity {

    private static final String LOGTAG = "MainActivity";
    private static final String[] SCOPES = { DriveScopes.DRIVE_METADATA_READONLY };

    int mBatteryIndicatorAnimationCounter;
    boolean mShowAccountInfoSnackbar = true;
    BatteryItem localBattery = new BatteryItem();
    static boolean sThemeChangeFlag;
    static GoogleAccountCredential sCredential;
    ProgressDialog mProgress;
    Menu mMenu;

    public static String getCredentialsAccountName() {
        return sCredential.getSelectedAccountName();
    }

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
        localBattery.batteryDeviceName = getDeviceName();
        localBattery.batteryDeviceId = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        Log.d(LOGTAG, "Device ID: " + localBattery.batteryDeviceId);
        localBattery.batteryCheckTime = System.currentTimeMillis();
        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Calling Drive API ...");
        // Initialize credentials and service object.
        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        sCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));
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
        setMenuCloudIcon();
        showAccountName();
    }

    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    isGooglePlayServicesAvailable();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        sCredential.setSelectedAccountName(accountName);
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        String info = getString(R.string.logged_to_account) + " " + accountName;
                        Toast toast = Toast.makeText(this, info, Toast.LENGTH_SHORT);
                        toast.show();
                        Log.d(LOGTAG, "Account name: " + accountName);
                    }
                } else if (resultCode == RESULT_CANCELED) {
                    Log.d(LOGTAG, "Account unspecified.");
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode != RESULT_OK) {
                    chooseAccount();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Attempt to get a set of data from the Drive API to display. If the
     * email address isn't known yet, then call chooseAccount() method so the
     * user can pick an account.
     */
    private void connectToDrive() {
        if (sCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else {
            sCredential.setSelectedAccountName(null);
            SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(PREF_ACCOUNT_NAME, null);
            editor.apply();
            setMenuCloudIcon();
            if (isConnection()) {
                Log.d(LOGTAG, "Ready to connect.");
            } else {
                Log.d(LOGTAG, "No internet connection.");
            }
        }
    }


    private void chooseAccount() {
        startActivityForResult(
                sCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mPowerConnectionBroadcastReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mMenu = menu;
        setMenuCloudIcon();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_gdrive) {
            if (isGooglePlayServicesAvailable()) {
                Log.d(LOGTAG, "Google Play Services available");
                connectToDrive();
            } else {
                Log.d(LOGTAG, "Google Play Services NOT available");
            }
        }
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
        new MakeRequestTask(sCredential).execute();
    }

    public void showBatteryLevel() {
        readLocalBatteryStatus();
        int batteryLevelValue = localBattery.batteryLevel;
        int indicatorColor;
        int indicatorBackgroundColor;
        int iconDefaultColor;
        int textColor;
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
            textColor = argbColor(ContextCompat
                    .getColor(this, R.color.colorWhite));
        } else {
            iconDefaultColor = indicatorBackgroundColor = argbColor(ContextCompat
                    .getColor(this, R.color.colorBatteryLevelProgressBarInitialLight));
            textColor = argbColor(ContextCompat
                    .getColor(this, R.color.colorBlack));
        }
        TextView deviceName = (TextView) findViewById(R.id.text_battery_level_description);
        deviceName.setTextColor(textColor);
        deviceName.setText(localBattery.batteryDeviceName);
        ProgressBar batteryLevelIndicator = (ProgressBar) findViewById(R.id.battery_progress_bar);
        batteryLevelIndicator.getProgressDrawable().setColorFilter(indicatorColor, PorterDuff.Mode.SRC_IN);
        batteryLevelIndicator.getBackground().setColorFilter(indicatorBackgroundColor, PorterDuff.Mode.SRC_IN);
        if (isTransitionsOn(this) && mBatteryIndicatorAnimationCounter == 0) {
            mBatteryIndicatorAnimationCounter = 1;
            batteryLevelIndicatorAnimation(batteryLevelIndicator, batteryLevelValue);
        } else batteryLevelIndicator.setProgress(batteryLevelValue);
        TextView batteryLevel = (TextView) findViewById(R.id.text_battery_level);
        batteryLevel.setTextColor(textColor);
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
        TextView batteryCheckTimeText = (TextView) findViewById(R.id.text_battery_level_check_time);
        batteryCheckTimeText.setText(batteryStatusCheckTime(localBattery));
    }

    private String batteryStatusCheckTime(BatteryItem batteryItem) {
        long batteryCheckTime = batteryItem.batteryCheckTime;
        final long now = System.currentTimeMillis();
        if (batteryCheckTime > now || batteryCheckTime <= 0) return getString(R.string.not_available);
        final long time_difference = now - batteryCheckTime;
        if (time_difference < MINUTE_IN_MILLIS)
            return getString(R.string.battery_check_time_current);
        else if (time_difference < 50 * MINUTE_IN_MILLIS)
            return getString(R.string.battery_check_time_ago,
                    getResources().getQuantityString(R.plurals.minutes, (int) time_difference / MINUTE_IN_MILLIS, time_difference / MINUTE_IN_MILLIS));
        else if (time_difference < 24 * HOUR_IN_MILLIS)
            return getString(R.string.battery_check_time_ago,
                    getResources().getQuantityString(R.plurals.hours, (int) time_difference / HOUR_IN_MILLIS, time_difference / HOUR_IN_MILLIS));
        else if (time_difference < 48 * HOUR_IN_MILLIS)
            return getString(R.string.battery_check_time_yesterday);
        else
            return getString(R.string.battery_check_time_ago,
                    getResources().getQuantityString(R.plurals.days, (int) time_difference / DAY_IN_MILLIS, time_difference / DAY_IN_MILLIS));
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

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        }
        return capitalize(manufacturer) + " " + model;
    }

    private static String capitalize(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        char[] arr = str.toCharArray();
        boolean capitalizeNext = true;
        String phrase = "";
        for (char c : arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase += Character.toUpperCase(c);
                capitalizeNext = false;
                continue;
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true;
            }
            phrase += c;
        }
        return phrase;
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

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        final int connectionStatusCode = googleAPI.isGooglePlayServicesAvailable(this);
        Log.d(LOGTAG, "GPSA status: " + connectionStatusCode + " - expected: " + ConnectionResult.SUCCESS);
        if (connectionStatusCode != ConnectionResult.SUCCESS ) {
            return false;
        }
        return true;
    }

    /**
     * An asynchronous task that handles the Drive API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {
        private com.google.api.services.drive.Drive mService = null;
        private Exception mLastError = null;

        public MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.drive.Drive.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Roy")
                    .build();
        }

        /**
         * Background task to call Drive API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<String> doInBackground(Void... params) {
            try {
                return getDataFromApi();
            } catch (Exception e) {
                Log.d(LOGTAG, "Exception getDataFromApi " + e);
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        /**
         * Fetch a list of up to 10 file names and IDs.
         * @return List of Strings describing files, or an empty list if no files
         *         found.
         * @throws IOException
         */
        private List<String> getDataFromApi() throws IOException {
            // Get a list of up to 10 files.
            List<String> fileInfo = new ArrayList<String>();
                FileList result = mService.files().list()
                        .setPageSize(10)
                        .setFields("nextPageToken, items(id, name)")
                        .execute();
                List<File> files = result.getFiles();
                if (files != null) {
                    for (File file : files) {
                        fileInfo.add(String.format("%s (%s)\n",
                                file.getName(), file.getId()));
                    }
                }
            return fileInfo;
        }


        @Override
        protected void onPreExecute() {
            mProgress.show();
        }

        @Override
        protected void onPostExecute(List<String> output) {
            mProgress.hide();
            if (output == null || output.size() == 0) {
                Log.d(LOGTAG, "No results returned.");
            } else {
                output.add(0, "Data retrieved using the Drive API:");
                Log.d(LOGTAG, TextUtils.join("\n", output));
            }
        }

        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError != null) {
                Log.d(LOGTAG, "Cancel, error occurred: " + mLastError.getMessage());
                if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(((UserRecoverableAuthIOException) mLastError)
                            .getIntent(), REQUEST_AUTHORIZATION);
                }
            } else {
                Log.d(LOGTAG, "Request cancelled.");
            }
        }
    }

    private void setMenuCloudIcon() {
        if (mMenu != null) {
            MenuItem cloudIcon = mMenu.findItem(R.id.action_gdrive);
            if (sCredential.getSelectedAccountName() != null) {
                if (isConnection()) {
                    cloudIcon.setIcon(R.drawable.ic_cloud_queue_white_24dp);
                    Log.d(LOGTAG, "Ready to connect.");
                } else {
                    cloudIcon.setIcon(R.drawable.ic_cloud_off_white_24dp);
                    Log.d(LOGTAG, "No internet connection.");
                }
            } else {
                cloudIcon.setIcon(R.drawable.ic_cloud_off_white_24dp);
                Log.d(LOGTAG, "Logging off.");
            }
        }
    }

    private void showAccountName() {
        if (mShowAccountInfoSnackbar) {
            View snackView;
            String hintText;
            snackView = findViewById(R.id.main);
            hintText = getString(R.string.logged_to_account) + " " + sCredential.getSelectedAccountName();
            if (snackView != null) {
                Snackbar.make(snackView, hintText, ACCOUNT_HINT_TIME)
                        .show();
            }
        }
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
