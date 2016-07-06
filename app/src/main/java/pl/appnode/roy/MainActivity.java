package pl.appnode.roy;

import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
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

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;

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
import static pl.appnode.roy.PreferencesSetupHelper.getDeviceCustomName;
import static pl.appnode.roy.PreferencesSetupHelper.isDarkTheme;
import static pl.appnode.roy.PreferencesSetupHelper.isTransitionsOn;
import static pl.appnode.roy.PreferencesSetupHelper.orientationSetup;
import static pl.appnode.roy.PreferencesSetupHelper.themeSetup;
import static pl.appnode.roy.PreferencesSetupHelper.uploadAlarmSetup;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final String LOGTAG = "MainActivity";

    int mBatteryIndicatorAnimationCounter;
    boolean mShowAccountInfoSnackbar = true;
    BatteryItem mLocalBattery = new BatteryItem();
    Menu mMenu;
    DatabaseReference mFireRef;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private static String mUsername;
    private GoogleApiClient mGoogleApiClient;
    static boolean sThemeChangeFlag;

    public static String getUsername() {
        return mUsername;
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
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setIcon(R.mipmap.ic_launcher);
        }
        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        } else {
            mUsername = mFirebaseUser.getDisplayName();
            showAccountName();
            Log.d(LOGTAG, "Firebase login user name: " + mUsername);
        }
        // Initialise Firebase client, set reference to database, and data change listener
        mFireRef = FirebaseDatabase.getInstance()
                .getReferenceFromUrl(BuildConfig.FB_BASE_ADDRESS);
        mFireRef.child("devices").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Log.d(LOGTAG, "Data: "+ snapshot.getValue());
                Log.d(LOGTAG, "There are " + snapshot.getChildrenCount() + " entries");
                TextView remoteBatteriesData = (TextView) findViewById(R.id.text_remote_data);
                int i = 0;
                remoteBatteriesData.setText("");
                for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                    i++;
                    BatteryItem batteryItem = postSnapshot.getValue(BatteryItem.class);
                    String remoteList = remoteBatteriesData.getText().toString();
                    if (i != 1) {remoteList = remoteList + "\n\n";}
                    refreshRemoteBatteriesList(batteryItem, remoteBatteriesData, remoteList);
                }
            }
            @Override
            public void onCancelled(DatabaseError firebaseError) {
                Log.d(LOGTAG, "Firebase error: " + firebaseError.getMessage());
            }
        });
        // Set (or cancel) alarm for local battery status upload accordingly to preferences
        uploadAlarmSetup(this);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();
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
    public  void onDestroy() {
        AboutDialog.dismissDialog();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_fbase) {
            if (mFirebaseUser != null) {
                Log.d(LOGTAG, "Signing out.");
                mFirebaseAuth.signOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                startActivity(new Intent(this, SignInActivity.class));
                finish();
            } else {
                Log.d(LOGTAG, "Not sign in.");
            }
        }
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, PreferencesActivity.class);
            this.startActivity(settingsIntent);
        }
        if (id == R.id.action_clear_list) {
            clearRemoteBatteriesList();
        }
        if (id == R.id.action_about) {
            AboutDialog.showDialog(MainActivity.this);
        }
        return super.onOptionsItemSelected(item);
    }

    private void clearRemoteBatteriesList() {
        TextView remoteBatteriesData = (TextView) findViewById(R.id.text_remote_data);
        remoteBatteriesData.setText("");
    }

    public void uploadBatteryStatusButton(View button) {
        DatabaseReference localBatteryRef = mFireRef.child("devices").child(mLocalBattery.batteryDeviceId);
        localBatteryRef.setValue(mLocalBattery);
    }

    public void downloadBatteriesInfoButton(View button) {
        mFireRef.child("devices").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                TextView remoteBatteriesData = (TextView) findViewById(R.id.text_remote_data);
                remoteBatteriesData.setText("");
                int i = 0;
                for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                    i++;
                    BatteryItem batteryItem = postSnapshot.getValue(BatteryItem.class);
                    String remoteList = remoteBatteriesData.getText().toString();
                    if (i != 1) {remoteList = remoteList + "\n\n";}
                    refreshRemoteBatteriesList(batteryItem, remoteBatteriesData, remoteList);
                }
                Log.d(LOGTAG, "Download batteries info.");
            }
            @Override
            public void onCancelled(DatabaseError firebaseError) {
                Log.d(LOGTAG, "Firebase error: " + firebaseError.getMessage());
            }
        });
    }

    private void refreshRemoteBatteriesList(BatteryItem batteryItem, TextView remoteBatteriesData, String remoteList) {
        if (batteryItem.batteryDeviceCustomName.equals("")) {
            remoteList = remoteList + batteryItem.getBatteryDeviceName();
        } else {
            remoteList = remoteList + batteryItem.getBatteryDeviceCustomName();
        }
        remoteList = remoteList + ": " + batteryItem.getBatteryLevel() + "%"
                + " checked " + batteryStatusCheckTime(batteryItem);
        // Some discharging devices return "0", some "-1"
        if (batteryItem.batteryChargingStatus < BATTERY_CHARGING) {
            remoteList = remoteList + " - discharging";
        } else {
            remoteList = remoteList + " - charging";
        }
        remoteBatteriesData.setText(remoteList);
        Log.d(LOGTAG, batteryItem.getBatteryDeviceName());
        Log.d(LOGTAG, "--- battery level: " + batteryItem.getBatteryLevel() + "%");
    }

    public void showBatteryLevel() {
        readLocalBatteryStatus();
        int batteryLevelValue = mLocalBattery.batteryLevel;
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
        if (mLocalBattery.batteryDeviceCustomName.equals("")) {
            deviceName.setText(mLocalBattery.batteryDeviceName);
        } else deviceName.setText(mLocalBattery.batteryDeviceCustomName);
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
        switch (mLocalBattery.batteryPluggedStatus) {
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
        switch (mLocalBattery.batteryChargingStatus) {
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
        batteryCheckTimeText.setText(batteryStatusCheckTime(mLocalBattery));
    }

    private String batteryStatusCheckTime(BatteryItem batteryItem) {
        long batteryCheckTime = batteryItem.batteryCheckTime;
        final long now = System.currentTimeMillis();
        if (batteryCheckTime > now || batteryCheckTime <= 0) return getString(R.string.not_available);
        final long time_difference = now - batteryCheckTime;
        if (time_difference < MINUTE_IN_MILLIS)
            return getString(R.string.battery_check_time_current);
        else if (time_difference < 60 * MINUTE_IN_MILLIS)
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
        mLocalBattery.batteryDeviceName = getDeviceName();
        mLocalBattery.batteryDeviceCustomName = getDeviceCustomName(this);
        mLocalBattery.batteryDeviceId = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        Log.d(LOGTAG, "Device ID: " + mLocalBattery.batteryDeviceId);
        mLocalBattery.batteryCheckTime = System.currentTimeMillis();
        Intent getBattery = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        // Reads battery level data
        int level = getBattery.getIntExtra(BatteryManager.EXTRA_LEVEL, BATTERY_CHECK_ERROR);
        int scale = getBattery.getIntExtra(BatteryManager.EXTRA_SCALE, BATTERY_CHECK_ERROR);
        if (level == BATTERY_CHECK_ERROR || scale == BATTERY_CHECK_ERROR) {
            mLocalBattery.batteryLevel = BATTERY_CHECK_ERROR;
        } else mLocalBattery.batteryLevel = (int)((level / (float)scale) * 100);
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
        mLocalBattery.batteryPluggedStatus =  batteryPluggedStatus;
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
        mLocalBattery.batteryChargingStatus = batteryChargeStatus;
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

    /**
     * Shows snackbar with currently used account name
     */
    private void showAccountName() {
        if (mFirebaseUser != null && mShowAccountInfoSnackbar) {
            View snackView;
            String hintText;
            snackView = findViewById(R.id.main);
            hintText = getString(R.string.log_in_welcome) + mFirebaseUser.getDisplayName();
            if (snackView != null) {
                Snackbar.make(snackView, hintText, ACCOUNT_HINT_TIME)
                        .show();
            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(LOGTAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, R.string.google_play_services_error, Toast.LENGTH_SHORT).show();
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
