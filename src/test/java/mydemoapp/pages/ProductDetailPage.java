package mydemoapp.pages;

import io.appium.java_client.android.AndroidDriver;
import io.qameta.allure.Step;
import org.openqa.selenium.By;

/** fragment_product_detail.xml */
public class ProductDetailPage extends BasePage {

    private static final By NAME = id("productTV");
    private static final By PRICE = id("priceTV");
    private static final By QUANTITY = id("noTV");
    private static final By ADD_TO_CART = a11y("Tap to add product to cart");

    public ProductDetailPage(AndroidDriver driver) {
        super(driver);
    }

    public ProductDetailPage waitUntilShown() {
        visible(scrollToId("cartBt"));
        return this;
    }

    public String name() {
        return text(NAME);
    }

    public String price() {
        return text(PRICE);
    }

    public int quantity() {
        return Integer.parseInt(text(QUANTITY).trim());
    }

    @Step("Add the product to the cart")
    public ProductDetailPage addToCart() {
        visible(scrollToId("cartBt"));
        tap(ADD_TO_CART);
        return this;
    }
}
