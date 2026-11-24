package mydemoapp.pages;

import io.appium.java_client.android.AndroidDriver;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.util.List;

/** fragment_cart.xml, with one item_my_cart.xml per line. */
public class CartPage extends BasePage {

    private static final By CART_LIST = a11y("Displays list of selected products");
    private static final By EMPTY_TITLE = id("noItemTitleTV");
    private static final By ITEM_TITLES = id("titleTV");
    private static final By ITEM_PRICES = id("priceTV");
    private static final By ITEM_QUANTITIES = id("noTV");
    private static final By TOTAL_ITEMS = id("itemsTV");
    private static final By TOTAL_PRICE = id("totalPriceTV");
    private static final By PROCEED = a11y("Confirms products for checkout");

    public CartPage(AndroidDriver driver) {
        super(driver);
    }

    public boolean isEmpty() {
        return isShown(EMPTY_TITLE, Duration.ofSeconds(5));
    }

    public CartPage waitForItems() {
        visible(CART_LIST);
        return this;
    }

    public List<String> itemNames() {
        return allVisible(ITEM_TITLES).stream().map(WebElement::getText).toList();
    }

    public List<String> itemPrices() {
        return allVisible(ITEM_PRICES).stream().map(WebElement::getText).toList();
    }

    public List<Integer> itemQuantities() {
        return allVisible(ITEM_QUANTITIES).stream().map(e -> Integer.parseInt(e.getText().trim())).toList();
    }

    /** "1 item" / "2 items" line at the bottom of the cart. */
    public String totalItemsLine() {
        return text(TOTAL_ITEMS);
    }

    public String totalPrice() {
        return text(TOTAL_PRICE);
    }

    public String emptyTitle() {
        return text(EMPTY_TITLE);
    }

    @Step("Proceed to checkout")
    public void proceedToCheckout() {
        tap(PROCEED);
    }
}
