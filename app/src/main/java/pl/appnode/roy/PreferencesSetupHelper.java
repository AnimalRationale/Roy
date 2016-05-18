package pl.appnode.roy;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import static pl.appnode.roy.Constants.CANCEL_WAKE_UP_ALARM;
import static pl.appnode.roy.Constants.KEY_SETTINGS_DEVICE_CUSTOM_NAME;
import static pl.appnode.roy.Constants.KEY_SETTINGS_FIRSTRUN;
import static pl.appnode.roy.Constants.KEY_SETTINGS_ORIENTATION;
import static pl.appnode.roy.Constants.KEY_SETTINGS_THEME;
import static pl.appnode.roy.Constants.KEY_SETTINGS_TRANSITIONS;
import static pl.appnode.roy.Constants.KEY_SETTINGS_UPLOAD;
import static pl.appnode.roy.Constants.KEY_SETTINGS_UPLOAD_FREQUENCY;
import static pl.appnode.roy.Constants.SET_WAKE_UP_ALARM;

/**
 * Reads (and uses some of) application settings
 * from app's default shared preferences.
 */
public class PreferencesSetupHelper {

    /**
     * Controls ability to upload local battery status to remote database.
     *
     * @param context the context of calling activity
     */

    public static void uploadAlarmSetup(Context context) {
        if (isUploadOn(context)) {
            WakeUpAlarmHelper.alarmManager(uploadFrequency(context), SET_WAKE_UP_ALARM);
        } else WakeUpAlarmHelper.alarmManager(0, CANCEL_WAKE_UP_ALARM);
    }

    /**
     * Sets up proper (dark or light) system theme.
     *
     * @param context the context of calling activity
     */
    public static void themeSetup(Context context) {
        if (isDarkTheme(context)) {
            context.setTheme(R.style.AppThemeDark);
        } else {
            context.setTheme(R.style.AppTheme);
        }
    }

    /**
     * Controls ability to change app display orientation accordingly to device state.
     *
     * @param activity the activity which is to be allowed to be displayed in portrait/landscape or
     *                 limited to only portrait orientation
     */
    public static void orientationSetup(Activity activity) {
        if (isRotationOn(activity)) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        } else activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    /**
     * Returns state of preference setting allowing local battery status periodical upload to remote database.
     *
     * @param context the context of calling activity
     *
     * @return true if upload is allowed in preferences
     */
    public static boolean isUploadOn(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        return settings.getBoolean(KEY_SETTINGS_UPLOAD, false);
    }

    /**
     * Returns user's custom device name, if set in preferences.
     *
     * @param context the context of calling activity
     *
     * @return device name set up in app preferences
     */
    public static String getDeviceCustomName(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        return settings.getString(KEY_SETTINGS_DEVICE_CUSTOM_NAME, "");
    }

    /**
     * Returns state of preference setting with battery status upload frequency in minutes.
     *
     * @param context the context of calling activity
     *
     * @return number of minutes between local battery status uploads
     */
    public static int uploadFrequency(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        return settings.getInt(KEY_SETTINGS_UPLOAD_FREQUENCY, 30);
    }

    /**
     * Returns state of dark theme setting in app preferences, used to set proper system theme.
     *
     * @return true if dark theme is set in preferences
     */
    public static boolean isDarkTheme(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        return settings.getBoolean(KEY_SETTINGS_THEME, false);
    }

    /**
     * Returns state of preference setting allowing app display orientation change.
     *
     * @param context the context of calling activity
     *
     * @return true if display orientation change is allowed in preferences
     */
    public static boolean isRotationOn(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        return settings.getBoolean(KEY_SETTINGS_ORIENTATION, false);
    }

    /**
     * Returns state of timer's button colors transitions preference setting
     *
     * @param context the context of calling activity
     *
     * @return true if timer's button colors transitions are allowed in preferences
     */
    public static boolean isTransitionsOn(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        return settings.getBoolean(KEY_SETTINGS_TRANSITIONS, true);
    }


    /**
     * Checks if app is started first time after installation.
     *
     * @return true if application is started with no previously existing preferences
     */
    public static boolean isFirstRun(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        boolean firstRun = settings.getBoolean(KEY_SETTINGS_FIRSTRUN, true);
        settings.edit().putBoolean(KEY_SETTINGS_FIRSTRUN, false).apply();
        return firstRun;
    }
}