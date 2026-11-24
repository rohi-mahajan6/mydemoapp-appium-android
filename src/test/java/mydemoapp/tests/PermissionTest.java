package mydemoapp.tests;

import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import mydemoapp.core.AppControl;
import mydemoapp.core.TestData;
import mydemoapp.pages.CameraPage;
import mydemoapp.pages.LocationPage;
import mydemoapp.pages.system.SystemPermissionDialog;
import org.openqa.selenium.WebDriverException;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Runtime permissions on Android 12, answered in the real system dialog. Appium does not grant
 * anything automatically (autoGrantPermissions=false).
 */
@Feature("Runtime permissions")
public class PermissionTest extends BaseTest {

    private static final double GPS_TOLERANCE_DEGREES = 0.001;

    /** Reinstalling also clears "don't ask again" flags left by an earlier denial. */
    @Override
    protected void resetApp() {
        app.reinstall();
    }

    @Test(description = "Camera denied: the QR scanner gives up and the app returns to the catalog")
    @Description("QRFragment.onRequestPermissionsResult: without CAMERA it starts the catalog again.")
    public void cameraDeniedReturnsToCatalog() {
        List<String> camera = List.of(AppControl.CAMERA);
        app.revoke(camera);
        assertFalse(app.grantedPermissions().contains(AppControl.CAMERA), "CAMERA revoked before the test");

        new CameraPage(driver).open();

        SystemPermissionDialog dialog = new SystemPermissionDialog(driver);
        assertTrue(dialog.isShown(Duration.ofSeconds(10)), "system camera permission dialog shown");
        Allure.addAttachment("Dialog message", dialog.message());
        dialog.deny();

        // QRFragment asks from two places (when the view is set up and in onResume), and CI run
        // 45447987289 showed a second dialog over the catalog after the first denial. Deny every
        // dialog until none is left (at most 3), and record how many were shown.
        int dialogs = 1;
        while (dialogs < 3 && dialog.isShown(Duration.ofSeconds(5))) {
            Allure.addAttachment("Dialog " + (dialogs + 1) + " message", dialog.message());
            dialog.deny();
            dialogs++;
        }
        assertFalse(dialog.isShown(Duration.ofSeconds(3)), "no permission dialog left after " + dialogs + " denial(s)");
        Allure.addAttachment("Camera permission dialogs shown", String.valueOf(dialogs));

        assertTrue(catalog.isShown(Duration.ofSeconds(10)), "catalog shown after denying the camera");
        assertFalse(app.grantedPermissions().contains(AppControl.CAMERA), "CAMERA still not granted");
    }

    @Test(description = "Location allowed (Precise): the screen shows the emulator's GPS position")
    @Description("Revoke location, open Geo Location, choose Precise and While using the app, set the "
            + "device position through Appium, and check that latitudeTV / longitudeTV show it.")
    public void locationAllowedShowsCoordinates() throws InterruptedException {
        List<String> location = List.of(AppControl.FINE_LOCATION, AppControl.COARSE_LOCATION);
        app.revoke(location);
        List<String> before = app.grantedPermissions();
        assertFalse(before.contains(AppControl.FINE_LOCATION) || before.contains(AppControl.COARSE_LOCATION),
                "location revoked before the test");

        double latitude = TestData.latitude();
        double longitude = TestData.longitude();
        app.setGeolocation(latitude, longitude);

        LocationPage page = new LocationPage(driver).open();

        SystemPermissionDialog dialog = new SystemPermissionDialog(driver);
        assertTrue(dialog.isShown(Duration.ofSeconds(10)), "system location permission dialog shown");
        assertTrue(dialog.offersLocationAccuracy(), "Android 12 dialog offers Precise and Approximate");
        dialog.choosePrecise();
        assertTrue(dialog.preciseSelected(), "Precise selected");
        dialog.allowWhileUsing();

        assertTrue(app.waitForGranted(AppControl.FINE_LOCATION, Duration.ofSeconds(10)), "fine (precise) location granted");
        assertTrue(app.grantedPermissions().contains(AppControl.COARSE_LOCATION), "coarse location granted with it");
        assertTrue(page.isShown(), "geo location screen shown");

        // Set the position again now that the app listens, then ask the app to observe.
        app.setGeolocation(latitude, longitude);
        // Diagnostics only: in CI run 45450932364 Appium could not read the position back
        // ("Cannot execute the 'retrieve geolocation' action"). The check is what the screen shows.
        try {
            Allure.addAttachment("Device position reported by Appium", String.valueOf(app.geolocation()));
        } catch (WebDriverException e) {
            Allure.addAttachment("Device position reported by Appium (not available)", e.getMessage().split("\n")[0]);
        }
        page.startObserving();

        LocationPage.Shown shown = page.waitForPosition(latitude, longitude, GPS_TOLERANCE_DEGREES, Duration.ofSeconds(30));
        Allure.addAttachment("Position shown by the app", shown.latitude() + ", " + shown.longitude());
        assertEquals(Double.parseDouble(orNaN(shown.latitude())), latitude, GPS_TOLERANCE_DEGREES, "latitude shown");
        assertEquals(Double.parseDouble(orNaN(shown.longitude())), longitude, GPS_TOLERANCE_DEGREES, "longitude shown");
    }

    private static String orNaN(String text) {
        return text == null || text.isBlank() ? "NaN" : text.trim();
    }
}
