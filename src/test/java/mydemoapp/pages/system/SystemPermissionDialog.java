package mydemoapp.pages.system;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import io.qameta.allure.Step;
import mydemoapp.pages.BasePage;
import org.openqa.selenium.By;

import java.time.Duration;

/**
 * Android 12 (API 31) runtime permission dialog, drawn by the system permission controller, not the
 * app. Its package is com.google.android.permissioncontroller on Google images and
 * com.android.permissioncontroller on AOSP images, so views are matched on the resource ID suffix.
 *
 * Camera:   "While using the app" / "Only this time" / "Don't allow"
 * Location: the same buttons, plus a "Precise" / "Approximate" choice above them.
 */
public class SystemPermissionDialog extends BasePage {

    private static final By MESSAGE = idSuffix("permission_message");
    private static final By ALLOW_WHILE_USING = idSuffix("permission_allow_foreground_only_button");
    private static final By ALLOW_ONCE = idSuffix("permission_allow_one_time_button");
    // First request: permission_deny_button. A repeated request (Android 11+) shows the same
    // "Don't allow" label on permission_deny_and_dont_ask_again_button (seen in CI run 45447987289).
    private static final By DENY = idSuffix("permission_deny(_and_dont_ask_again)?_button");
    private static final By PRECISE = idSuffix("permission_location_accuracy_radio_fine");
    private static final By APPROXIMATE = idSuffix("permission_location_accuracy_radio_coarse");

    public SystemPermissionDialog(AndroidDriver driver) {
        super(driver);
    }

    private static By idSuffix(String name) {
        return AppiumBy.androidUIAutomator("new UiSelector().resourceIdMatches(\".*:id/" + name + "\")");
    }

    /** True if the permission dialog appears within the given time. */
    public boolean isShown(Duration within) {
        return isShown(DENY, within);
    }

    /** For example "Allow My Demo App to take pictures and record video?" */
    public String message() {
        return text(MESSAGE);
    }

    public boolean offersLocationAccuracy() {
        return isShown(PRECISE, Duration.ofSeconds(3)) && isShownNow(APPROXIMATE);
    }

    @Step("Permission dialog: Don't allow")
    public void deny() {
        tap(DENY);
    }

    @Step("Permission dialog: While using the app")
    public void allowWhileUsing() {
        tap(ALLOW_WHILE_USING);
    }

    @Step("Permission dialog: Only this time")
    public void allowOnce() {
        tap(ALLOW_ONCE);
    }

    @Step("Permission dialog: choose Precise")
    public SystemPermissionDialog choosePrecise() {
        tap(PRECISE);
        return this;
    }

    @Step("Permission dialog: choose Approximate")
    public SystemPermissionDialog chooseApproximate() {
        tap(APPROXIMATE);
        return this;
    }

    public boolean preciseSelected() {
        return Boolean.parseBoolean(visible(PRECISE).getAttribute("checked"));
    }
}
