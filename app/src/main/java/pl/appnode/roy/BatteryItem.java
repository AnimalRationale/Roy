package pl.appnode.roy;

/**
 * Defines structure of items holding battery information.
 */
public class BatteryItem {
    String batteryDeviceId;
    String batteryDeviceName;
    String batteryDeviceCustomName;
    long batteryCheckTime;
    int batteryLevel;
    int batteryPluggedStatus;
    int batteryChargingStatus;

    public BatteryItem() {} // Default constructor needed by Firebase client

    public String getBatteryDeviceId() {
        return batteryDeviceId;
    }

    public String getBatteryDeviceName() {
        return batteryDeviceName;
    }

    public String getBatteryDeviceCustomName() { return batteryDeviceCustomName; }

    public long getBatteryCheckTime() {
        return batteryCheckTime;
    }

    public int getBatteryLevel() {
        return batteryLevel;
    }

    public int getBatteryPluggedStatus() {
        return batteryPluggedStatus;
    }

    public int getBatteryChargingStatus() {
        return batteryChargingStatus;
    }
}
