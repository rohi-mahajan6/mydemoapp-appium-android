package mydemoapp.pages;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import io.qameta.allure.Step;
import org.openqa.selenium.By;

import java.time.Duration;

/** The header on every main screen (menu_header_layout.xml) and the side menu (navigation_layout.xml). */
public class HeaderBar extends BasePage {

    private static final By MENU_BUTTON = a11y("View menu");
    private static final By CART_BUTTON = id("cartRL");
    private static final By CART_BADGE = id("cartCircleRL");
    private static final By CART_COUNT = id("cartTV");
    private static final By MENU_LIST = id("menuRV");
    // Content descriptions set in MainActivity for the last menu entry.
    private static final By LOGIN_ITEM = a11y("Login Menu Item");
    private static final By LOGOUT_ITEM = a11y("Logout Menu Item");

    public HeaderBar(AndroidDriver driver) {
        super(driver);
    }

    /** Menu entries are TextViews with id itemTV; the label is the only thing that tells them apart. */
    private static By menuItem(String label) {
        return AppiumBy.androidUIAutomator(
                "new UiScrollable(new UiSelector().resourceIdMatches(\".*:id/menuRV\"))"
                        + ".scrollIntoView(new UiSelector().resourceIdMatches(\".*:id/itemTV\").text(\"" + label + "\"))");
    }

    @Step("Open the side menu")
    public HeaderBar openMenu() {
        tap(MENU_BUTTON);
        visible(MENU_LIST);
        return this;
    }

    @Step("Choose \"{label}\" in the side menu")
    public void choose(String label) {
        openMenu();
        tap(menuItem(label));
    }

    @Step("Open the login screen from the menu")
    public LoginPage openLogin() {
        openMenu();
        tap(LOGIN_ITEM);
        return new LoginPage(driver).waitUntilShown();
    }

    public boolean menuOffersLogout() {
        openMenu();
        boolean shown = isShown(LOGOUT_ITEM, Duration.ofSeconds(3));
        driver.navigate().back();
        return shown;
    }

    @Step("Open the cart")
    public CartPage openCart() {
        tap(CART_BUTTON);
        return new CartPage(driver);
    }

    /** Number on the cart badge; 0 when the badge is hidden (MainActivity.setData hides it at 0). */
    public int cartCount() {
        if (!isShown(CART_BADGE, Duration.ofSeconds(2))) {
            return 0;
        }
        return Integer.parseInt(text(CART_COUNT).trim());
    }
}
