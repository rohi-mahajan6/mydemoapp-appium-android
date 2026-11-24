package mydemoapp.core;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;

import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

/**
 * Owns the single Appium session. The suite runs on one emulator with parallel="false", so one
 * static driver is enough; the failure listener reads it from here.
 */
public final class DriverManager {

    private static AndroidDriver driver;

    private DriverManager() {
    }

    public static AndroidDriver start() {
        if (driver != null) {
            return driver;
        }
        Path apk = Path.of(Config.get("apk.path")).toAbsolutePath();
        if (!Files.isRegularFile(apk)) {
            throw new IllegalStateException("APK not found at " + apk + " (set -Dapk.path)");
        }

        UiAutomator2Options options = new UiAutomator2Options()
                .setPlatformVersion(Config.get("platformVersion"))
                .setDeviceName(Config.get("deviceName"))
                .setApp(apk.toString())
                .setAppPackage(Config.appPackage())
                .setAppActivity(Config.get("appActivity"))
                .setAppWaitActivity(Config.get("appWaitActivity"))
                // Permission tests answer the system dialogs themselves.
                .setAutoGrantPermissions(Config.getBoolean("autoGrantPermissions"))
                .setNoReset(false)
                .setFullReset(false)
                .setNewCommandTimeout(Duration.ofSeconds(Config.getInt("newCommandTimeout")))
                .setUiautomator2ServerInstallTimeout(Duration.ofMillis(Config.getInt("uiautomator2ServerInstallTimeout")))
                .setAdbExecTimeout(Duration.ofMillis(Config.getInt("adbExecTimeout")));

        try {
            driver = new AndroidDriver(URI.create(Config.get("appium.url")).toURL(), options);
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Invalid appium.url: " + Config.get("appium.url"), e);
        }
        return driver;
    }

    /** The running session, or null when none was started (for listeners). */
    public static AndroidDriver current() {
        return driver;
    }

    public static AndroidDriver get() {
        if (driver == null) {
            throw new IllegalStateException("No Appium session: call DriverManager.start() first");
        }
        return driver;
    }

    public static void quit() {
        if (driver != null) {
            try {
                driver.quit();
            } finally {
                driver = null;
            }
        }
    }
}
