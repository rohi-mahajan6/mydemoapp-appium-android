package mydemoapp.pages;

import io.appium.java_client.android.AndroidDriver;
import io.qameta.allure.Step;
import org.openqa.selenium.By;

import java.time.Duration;

/**
 * The QR code scanner (fragment_qr.xml). QRFragment asks for CAMERA when it opens; if the permission
 * is denied, onRequestPermissionsResult sends the user back to the catalog.
 */
public class CameraPage extends BasePage {

    public static final String MENU_LABEL = "QR Code Scanner";

    private static final By TITLE = id("qrCodeTV");
    private static final By PREVIEW = id("previewView");

    private final HeaderBar header;

    public CameraPage(AndroidDriver driver) {
        super(driver);
        this.header = new HeaderBar(driver);
    }

    @Step("Open the QR code scanner from the menu")
    public CameraPage open() {
        header.choose(MENU_LABEL);
        return this;
    }

    public boolean isShown() {
        return isShown(TITLE, Duration.ofSeconds(5));
    }

    public boolean previewShown() {
        return isShown(PREVIEW, Duration.ofSeconds(10));
    }
}
