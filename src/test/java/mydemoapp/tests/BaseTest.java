package mydemoapp.tests;

import io.appium.java_client.android.AndroidDriver;
import mydemoapp.core.AppControl;
import mydemoapp.core.DriverManager;
import mydemoapp.pages.CatalogPage;
import mydemoapp.pages.HeaderBar;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

/**
 * One Appium session for the whole suite (session start includes installing the APK and the
 * UiAutomator2 server, the slowest part). Every test starts from a clean app: data cleared, so the
 * cart is empty and nobody is logged in, and the catalog on screen.
 */
public abstract class BaseTest {

    protected AndroidDriver driver;
    protected AppControl app;
    protected HeaderBar header;
    protected CatalogPage catalog;

    @BeforeSuite(alwaysRun = true)
    public void startSession() {
        DriverManager.start();
    }

    @BeforeMethod(alwaysRun = true)
    public void cleanApp() {
        driver = DriverManager.get();
        app = new AppControl(driver);
        header = new HeaderBar(driver);
        catalog = new CatalogPage(driver);
        resetApp();
        catalog.waitUntilShown();
    }

    /** How a test class gets a clean app. PermissionTest overrides this to reinstall. */
    protected void resetApp() {
        app.restartClean();
    }

    @AfterSuite(alwaysRun = true)
    public void endSession() {
        DriverManager.quit();
    }

    /** "$ 29.99" and "$29.99" both become 29.99, so prices from different screens can be compared. */
    protected static double amount(String shown) {
        return Double.parseDouble(shown.replaceAll("[^0-9.]", ""));
    }
}
