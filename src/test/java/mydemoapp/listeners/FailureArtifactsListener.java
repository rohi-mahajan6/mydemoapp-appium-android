package mydemoapp.listeners;

import io.appium.java_client.android.AndroidDriver;
import io.qameta.allure.Allure;
import mydemoapp.core.DriverManager;
import org.openqa.selenium.OutputType;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

/** On a failed test: attaches a screenshot, the UI hierarchy and the current activity to Allure. */
public class FailureArtifactsListener implements ITestListener {

    @Override
    public void onTestFailure(ITestResult result) {
        AndroidDriver driver = DriverManager.current();
        if (driver == null) {
            return;
        }
        try {
            byte[] png = driver.getScreenshotAs(OutputType.BYTES);
            Allure.addAttachment("Screenshot on failure", "image/png", new ByteArrayInputStream(png), "png");
        } catch (RuntimeException e) {
            Allure.addAttachment("Screenshot on failure (not taken)", e.toString());
        }
        try {
            Allure.addAttachment("Page source on failure", "application/xml",
                    new ByteArrayInputStream(driver.getPageSource().getBytes(StandardCharsets.UTF_8)), "xml");
        } catch (RuntimeException e) {
            Allure.addAttachment("Page source on failure (not taken)", e.toString());
        }
        try {
            Allure.addAttachment("Current activity", driver.currentActivity());
        } catch (RuntimeException ignored) {
            // The session may already be gone.
        }
    }
}
