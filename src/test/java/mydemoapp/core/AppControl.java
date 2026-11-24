package mydemoapp.core;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import io.appium.java_client.appmanagement.ApplicationState;
import io.qameta.allure.Step;

import java.util.List;
import java.util.Map;

/**
 * App lifecycle, permissions and device state through Appium's UiAutomator2 commands. None of these
 * need Appium's insecure features (no adb shell).
 */
public final class AppControl {

    public static final String CAMERA = "android.permission.CAMERA";
    public static final String FINE_LOCATION = "android.permission.ACCESS_FINE_LOCATION";
    public static final String COARSE_LOCATION = "android.permission.ACCESS_COARSE_LOCATION";

    private final AndroidDriver driver;
    private final String pkg = Config.appPackage();

    public AppControl(AndroidDriver driver) {
        this.driver = driver;
    }

    /** Stops the app and clears its data, then starts it again: an empty cart and nobody logged in. */
    @Step("Restart the app with cleared data")
    public void restartClean() {
        driver.terminateApp(pkg);
        driver.executeScript("mobile: clearApp", Map.of("appId", pkg));
        driver.activateApp(pkg);
    }

    /**
     * Uninstalls and reinstalls the APK. This also clears Android's per-app permission flags
     * (for example "denied, don't ask again"), which a revoke alone does not.
     */
    @Step("Reinstall the app")
    public void reinstall() {
        driver.terminateApp(pkg);
        driver.removeApp(pkg);
        driver.installApp(Config.get("apk.path"));
        driver.activateApp(pkg);
    }

    @Step("Revoke {permissions}")
    public void revoke(List<String> permissions) {
        driver.executeScript("mobile: changePermissions", Map.of(
                "permissions", permissions,
                "appPackage", pkg,
                "action", "revoke"));
    }

    /** Runtime permissions the app currently holds, as reported by the package manager. */
    @SuppressWarnings("unchecked")
    public List<String> grantedPermissions() {
        Object result = driver.executeScript("mobile: getPermissions", Map.of(
                "type", "granted",
                "appPackage", pkg));
        return (List<String>) result;
    }

    @Step("Set the device GPS position to {latitude}, {longitude}")
    public void setGeolocation(double latitude, double longitude) {
        driver.executeScript("mobile: setGeolocation", Map.of(
                "latitude", latitude,
                "longitude", longitude,
                "altitude", 0));
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> geolocation() {
        return (Map<String, Object>) driver.executeScript("mobile: getGeolocation");
    }

    @Step("Press the Home key")
    public void pressHome() {
        driver.pressKey(new KeyEvent(AndroidKey.HOME));
    }

    @Step("Bring the app back to the foreground")
    public void activate() {
        driver.activateApp(pkg);
    }

    public ApplicationState state() {
        return driver.queryAppState(pkg);
    }
}
