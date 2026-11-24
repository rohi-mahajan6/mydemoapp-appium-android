package mydemoapp.pages;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.util.List;

/** fragment_product_catalog.xml, with one item_products.xml per product. */
public class CatalogPage extends BasePage {

    private static final By TITLE = id("productTV");
    private static final By PRODUCT_LIST = a11y("Displays all products of catalog");
    private static final By PRODUCT_TITLES = id("titleTV");
    private static final By PRODUCT_PRICES = id("priceTV");

    public CatalogPage(AndroidDriver driver) {
        super(driver);
    }

    public CatalogPage waitUntilShown() {
        visible(PRODUCT_LIST);
        return this;
    }

    /** The catalog is on screen: the "Products" title and the product grid. */
    public boolean isShown(Duration within) {
        return isShown(PRODUCT_LIST, within) && "Products".equals(text(TITLE));
    }

    /** Name and price of the product in the given grid position, as the catalog shows them. */
    public Product productAt(int index) {
        List<WebElement> names = allVisible(PRODUCT_TITLES);
        List<WebElement> prices = allVisible(PRODUCT_PRICES);
        return new Product(names.get(index).getText(), prices.get(index).getText());
    }

    @Step("Open product \"{name}\"")
    public ProductDetailPage open(String name) {
        tap(AppiumBy.androidUIAutomator(
                "new UiSelector().resourceIdMatches(\".*:id/titleTV\").text(\"" + name + "\")"));
        return new ProductDetailPage(driver).waitUntilShown();
    }

    public record Product(String name, String price) {
    }
}
