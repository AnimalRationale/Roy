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
}
