package mydemoapp.tests;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import mydemoapp.core.TestData;
import mydemoapp.pages.LoginPage;
import org.testng.annotations.Test;

import java.time.Duration;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@Feature("Login")
public class LoginTest extends BaseTest {

    @Test(description = "Valid user logs in and lands on the catalog")
    @Description("bod@example.com with the password shown on the login screen. LoginFragment sends a "
            + "successful login to the catalog, and the menu then offers Log Out instead of Log In.")
    public void validUserLogsIn() {
        LoginPage login = header.openLogin();
        login.loginAs(TestData.user("standard"), TestData.password());

        assertTrue(catalog.isShown(Duration.ofSeconds(15)), "catalog shown after login");
        assertTrue(header.menuOffersLogout(), "menu offers Log Out after login");
    }

    @Test(description = "Locked-out user is refused with a message")
    @Description("alice@example.com is the app's locked-out account: the login screen stays and shows "
            + "the locked-out message under the password field.")
    public void lockedOutUserIsRefused() {
        LoginPage login = header.openLogin();
        login.loginAs(TestData.user("lockedOut"), TestData.password());

        assertEquals(login.passwordError(), TestData.message("lockedOut"));
        assertTrue(login.isShown(), "still on the login screen");
    }

    @Test(description = "Empty username shows the username error")
    public void emptyUsernameIsRejected() {
        LoginPage login = header.openLogin();
        login.loginAs("", TestData.password());

        assertEquals(login.usernameError(), TestData.message("usernameRequired"));
        assertTrue(login.isShown(), "still on the login screen");
    }

    @Test(description = "Empty password shows the password error")
    public void emptyPasswordIsRejected() {
        LoginPage login = header.openLogin();
        login.loginAs(TestData.user("standard"), "");

        assertEquals(login.passwordError(), TestData.message("passwordRequired"));
        assertTrue(login.isShown(), "still on the login screen");
    }
}
