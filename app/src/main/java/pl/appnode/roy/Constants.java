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

    static final int BATTERY_DISCHARGING = 0;
    static final int BATTERY_CHARGING = 1;

    /** Keys for saving app settings (theme, orientation, colors transitions) and first run check). */
    public static final String KEY_SETTINGS_THEME = "settings_checkbox_theme";
    public static final String KEY_SETTINGS_ORIENTATION = "settings_checkbox_orientation";
    public static final String KEY_SETTINGS_TRANSITIONS = "settings_checkbox_transitions";
    public static final String KEY_SETTINGS_FIRSTRUN = "settings_first_run";
}
