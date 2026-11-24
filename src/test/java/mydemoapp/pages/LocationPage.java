package mydemoapp.pages;

import io.appium.java_client.android.AndroidDriver;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * The geo location screen (fragment_location.xml). LocationFragment asks for coarse and fine
 * location when it opens and writes the position into latitudeTV / longitudeTV as plain numbers.
 */
public class LocationPage extends BasePage {

    public static final String MENU_LABEL = "Geo Location";

    private static final By TITLE = id("locationTV");
    private static final By LATITUDE = id("latitudeTV");
    private static final By LONGITUDE = id("longitudeTV");
    private static final By START = a11y("Start observation of user location");

    private final HeaderBar header;

    public LocationPage(AndroidDriver driver) {
        super(driver);
        this.header = new HeaderBar(driver);
    }

    @Step("Open the geo location screen from the menu")
    public LocationPage open() {
        header.choose(MENU_LABEL);
        return this;
    }

    public boolean isShown() {
        return isShown(TITLE, Duration.ofSeconds(5));
    }

    public String latitudeText() {
        return driver.findElement(LATITUDE).getText();
    }

    public String longitudeText() {
        return driver.findElement(LONGITUDE).getText();
    }

    @Step("Start observing the location")
    public LocationPage startObserving() {
        tap(START);
        return this;
    }

    /**
     * Waits until the screen shows the expected position (within the tolerance, in degrees).
     * Returns the last values seen, so a failure can report what the screen actually showed.
     */
    @Step("Wait for the screen to show {latitude}, {longitude}")
    public Shown waitForPosition(double latitude, double longitude, double tolerance, Duration within) {
        final Shown[] last = {new Shown("", "")};
        try {
            new WebDriverWait(driver, within).pollingEvery(Duration.ofMillis(500)).until(d -> {
                last[0] = new Shown(latitudeText(), longitudeText());
                return last[0].matches(latitude, longitude, tolerance);
            });
        } catch (TimeoutException e) {
            // Fall through: the caller asserts on the values that were shown.
        }
        return last[0];
    }

    public record Shown(String latitude, String longitude) {
        boolean matches(double lat, double lon, double tolerance) {
            try {
                return Math.abs(Double.parseDouble(latitude.trim()) - lat) <= tolerance
                        && Math.abs(Double.parseDouble(longitude.trim()) - lon) <= tolerance;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    }
}
