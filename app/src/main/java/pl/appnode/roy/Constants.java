package pl.appnode.roy;

/**
 * Set of constants.
 */

final class Constants {
    private Constants() {} /** Private constructor of final class to prevent instantiating. */

    static final int BATTERY_CHECK_ERROR = -1;

    static final int BATTERY_NOT_PLUGGED = 0;
    static final int BATTERY_PLUGGED_AC = 1;
    static final int BATTERY_PLUGGED_USB = 2;
    static final int BATTERY_PLUGGED_WIRELESS = 3;

    static final int BATTERY_DISCHARGING_1 = 0;
    static final int BATTERY_DISCHARGING_2 = -1;
    static final int BATTERY_CHARGING = 1;

    /** Time units in milliseconds, used to determine time from last battery status check */
    static final int SECOND_IN_MILLIS = 1000;
    static final int MINUTE_IN_MILLIS = 60 * SECOND_IN_MILLIS;
    static final int HOUR_IN_MILLIS = 60 * MINUTE_IN_MILLIS;
    static final int DAY_IN_MILLIS = 24 * HOUR_IN_MILLIS;

    /** Keys for saving app settings (first run indicator, local battery status upload) */
    public static final String KEY_SETTINGS_FIRSTRUN = "settings_first_run";
    public static final String KEY_SETTINGS_UPLOAD = "settings_checkbox_upload_battery_status";
    public static final String KEY_SETTINGS_UPLOAD_FREQUENCY = "settings_upload_frequency";
    public static final String KEY_SETTINGS_DEVICE_CUSTOM_NAME = "settings_device_custom_name";

    /** Keys for saving appearance settings (theme, orientation, colors transitions). */
    public static final String KEY_SETTINGS_THEME = "settings_checkbox_theme";
    public static final String KEY_SETTINGS_ORIENTATION = "settings_checkbox_orientation";
    public static final String KEY_SETTINGS_TRANSITIONS = "settings_checkbox_transitions";

    /** Request codes for intents */
    public static final int REQUEST_SIGN_IN = 9001;

    /** Current account snackbar info display time in milliseconds*/
    public static final int ACCOUNT_HINT_TIME = 2000;

    /** Commands for setting up and canceling wake up alarms for uploading local battery status */
    public static final int CANCEL_WAKE_UP_ALARM = 0;
    public static final int SET_WAKE_UP_ALARM = 1;
    public static final String ACTION_BATTERY_STATUS_UPLOAD = "BATTERY_STATUS_UPLOAD";

}
