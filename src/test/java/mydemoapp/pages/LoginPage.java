package mydemoapp.pages;

import io.appium.java_client.android.AndroidDriver;
import io.qameta.allure.Step;
import org.openqa.selenium.By;

import java.time.Duration;

/** fragment_login.xml */
public class LoginPage extends BasePage {

    private static final By USERNAME = id("nameET");
    private static final By PASSWORD = id("passwordET");
    private static final By LOGIN_BUTTON = a11y("Tap to login with given credentials");
    private static final By USERNAME_ERROR = id("nameErrorTV");
    private static final By PASSWORD_ERROR = id("passwordErrorTV");

    public LoginPage(AndroidDriver driver) {
        super(driver);
    }

    public LoginPage waitUntilShown() {
        visible(USERNAME);
        return this;
    }

    public boolean isShown() {
        return isShown(USERNAME, Duration.ofSeconds(10));
    }

    @Step("Log in as {username}")
    public void loginAs(String username, String password) {
        type(USERNAME, username);
        type(PASSWORD, password);
        hideKeyboardIfShown();
        tap(LOGIN_BUTTON);
    }

    public String usernameError() {
        return text(USERNAME_ERROR);
    }

    /** The password error line; the app also uses it for the locked-out message. */
    public String passwordError() {
        return text(PASSWORD_ERROR);
    }
}
