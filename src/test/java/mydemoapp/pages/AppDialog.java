package mydemoapp.pages;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import io.qameta.allure.Step;
import org.openqa.selenium.By;

/**
 * The app's own AlertDialogs (MainActivity.showResetDialog, Methods.showDialog). They use the
 * standard Android dialog views: android:id/message, button1 (positive) and button2 (negative).
 */
public class AppDialog extends BasePage {

    private static final By MESSAGE = AppiumBy.id("android:id/message");
    private static final By POSITIVE = AppiumBy.id("android:id/button1");

    public AppDialog(AndroidDriver driver) {
        super(driver);
    }

    public String message() {
        return text(MESSAGE);
    }

    @Step("Confirm the dialog")
    public void confirm() {
        tap(POSITIVE);
    }
}
