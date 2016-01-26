package pl.appnode.roy;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.Build;

import static pl.appnode.roy.Constants.KEY_SETTINGS_FIRSTRUN;
import static pl.appnode.roy.Constants.KEY_SETTINGS_ORIENTATION;
import static pl.appnode.roy.Constants.KEY_SETTINGS_THEME;
import static pl.appnode.roy.Constants.KEY_SETTINGS_TRANSITIONS;

/**
 * Reads (and uses some of) application settings
 * from app's default shared preferences.
 */
public class PreferencesSetupHelper {

    /**
     * Sets up proper (dark or light) system theme.
     *
     * @param context the context of calling activity
     */
    public static void themeSetup(Context context) {
        if (isDarkTheme(context)) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                context.setTheme(android.R.style.Theme_Holo);
            } else {
                context.setTheme(android.R.style.Theme_Material);
            }
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            context.setTheme(android.R.style.Theme_Holo_Light);
        } else {
            context.setTheme(android.R.style.Theme_Material_Light);
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