package mydemoapp.pages;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import mydemoapp.core.Config;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Shared waits and locator helpers. Locators come from the app's layouts at tag 2.3.0
 * (res/layout/*.xml): resource IDs, and accessibility IDs (content descriptions) where the app sets
 * them. No coordinates, and XPath only where nothing better exists.
 */
public abstract class BasePage {

    protected final AndroidDriver driver;
    protected final WebDriverWait wait;

    protected BasePage(AndroidDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Config.explicitWait());
        this.wait.ignoring(StaleElementReferenceException.class);
    }

    /** A view in the app under test, by its resource ID (without the package prefix). */
    protected static By id(String resourceId) {
        return AppiumBy.id(Config.appPackage() + ":id/" + resourceId);
    }

    /** A view by its content description. */
    protected static By a11y(String contentDescription) {
        return AppiumBy.accessibilityId(contentDescription);
    }

    /** Scrolls the first scrollable container until the view with this resource ID is on screen. */
    protected static By scrollToId(String resourceId) {
        return AppiumBy.androidUIAutomator(
                "new UiScrollable(new UiSelector().scrollable(true).instance(0))"
                        + ".scrollIntoView(new UiSelector().resourceId(\"" + Config.appPackage() + ":id/" + resourceId + "\"))");
    }

    protected WebElement visible(By by) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    protected List<WebElement> allVisible(By by) {
        return wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(by));
    }

    protected void tap(By by) {
        wait.until(ExpectedConditions.elementToBeClickable(by)).click();
    }

    protected void type(By by, String text) {
        WebElement field = visible(by);
        field.clear();
        field.sendKeys(text);
    }

    protected String text(By by) {
        return visible(by).getText();
    }

    /** True if the view appears within the given time; never throws. */
    protected boolean isShown(By by, Duration within) {
        try {
            new WebDriverWait(driver, within)
                    .ignoring(StaleElementReferenceException.class)
                    .until(ExpectedConditions.visibilityOfElementLocated(by));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    /** True if the view is on screen right now (no wait). */
    protected boolean isShownNow(By by) {
        try {
            return driver.findElement(by).isDisplayed();
        } catch (NoSuchElementException | StaleElementReferenceException e) {
            return false;
        }
    }

    protected void hideKeyboardIfShown() {
        if (driver.isKeyboardShown()) {
            driver.hideKeyboard();
        }
    }
}
