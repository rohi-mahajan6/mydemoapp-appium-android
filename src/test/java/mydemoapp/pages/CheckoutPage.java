package mydemoapp.pages;

import com.fasterxml.jackson.databind.JsonNode;
import io.appium.java_client.android.AndroidDriver;
import io.qameta.allure.Step;
import org.openqa.selenium.By;

/**
 * The four checkout screens. They share the button ID "paymentBtn", so each step waits for something
 * only that screen has before it acts:
 *   fragment_checkout_info.xml    shipping address   "To Payment"
 *   fragment_checkout.xml         payment            "Review Order"
 *   fragment_place_order.xml      review             "Place Order"
 *   fragment_checkout_complete.xml                   "Checkout Complete"
 */
public class CheckoutPage extends BasePage {

    // Shipping address
    private static final By ADDRESS_FORM_TITLE = id("enterShippingAddressTV");
    private static final By FULL_NAME = id("fullNameET");
    private static final By ADDRESS_1 = id("address1ET");
    private static final By ADDRESS_2 = id("address2ET");
    private static final By CITY = id("cityET");
    private static final By STATE = id("stateET");
    private static final By ZIP = id("zipET");
    private static final By COUNTRY = id("countryET");
    private static final By TO_PAYMENT = a11y("Saves user info for checkout");

    // Payment
    private static final By PAYMENT_TITLE = id("enterPaymentMethodTV");
    private static final By CARD_NAME = id("nameET");
    private static final By CARD_NUMBER = id("cardNumberET");
    private static final By EXPIRATION = id("expirationDateET");
    private static final By SECURITY_CODE = id("securityCodeET");
    private static final By REVIEW_ORDER = a11y("Saves payment info and launches screen to review checkout data");

    // Review
    private static final By REVIEW_LIST = id("placeOrderRV");
    private static final By REVIEW_FULL_NAME = id("fullNameTV");
    private static final By REVIEW_TOTAL = id("totalAmountTV");
    private static final By PLACE_ORDER = a11y("Completes the process of checkout");

    // Complete
    private static final By COMPLETE_TITLE = id("completeTV");
    private static final By CONTINUE_SHOPPING = a11y("Tap to open catalog");

    public CheckoutPage(AndroidDriver driver) {
        super(driver);
    }

    public CheckoutPage waitForAddressForm() {
        visible(ADDRESS_FORM_TITLE);
        visible(FULL_NAME);
        return this;
    }

    @Step("Enter the shipping address")
    public CheckoutPage enterAddress(JsonNode a) {
        waitForAddressForm();
        typeIfGiven(FULL_NAME, "fullNameET", a.path("fullName").asText());
        typeIfGiven(ADDRESS_1, "address1ET", a.path("address1").asText());
        typeIfGiven(ADDRESS_2, "address2ET", a.path("address2").asText());
        typeIfGiven(CITY, "cityET", a.path("city").asText());
        typeIfGiven(STATE, "stateET", a.path("state").asText());
        typeIfGiven(ZIP, "zipET", a.path("zip").asText());
        typeIfGiven(COUNTRY, "countryET", a.path("country").asText());
        hideKeyboardIfShown();
        visible(scrollToId("paymentBtn"));
        tap(TO_PAYMENT);
        visible(PAYMENT_TITLE);
        return this;
    }

    @Step("Enter the payment details")
    public CheckoutPage enterPayment(JsonNode p) {
        visible(PAYMENT_TITLE);
        typeIfGiven(CARD_NAME, "nameET", p.path("fullName").asText());
        typeIfGiven(CARD_NUMBER, "cardNumberET", p.path("cardNumber").asText());
        typeIfGiven(EXPIRATION, "expirationDateET", p.path("expiration").asText());
        typeIfGiven(SECURITY_CODE, "securityCodeET", p.path("securityCode").asText());
        hideKeyboardIfShown();
        visible(scrollToId("paymentBtn"));
        tap(REVIEW_ORDER);
        visible(REVIEW_LIST);
        return this;
    }

    public String reviewFullName() {
        return text(REVIEW_FULL_NAME);
    }

    public String reviewTotal() {
        visible(scrollToId("totalAmountTV"));
        return text(REVIEW_TOTAL);
    }

    @Step("Place the order")
    public CheckoutPage placeOrder() {
        visible(scrollToId("paymentBtn"));
        tap(PLACE_ORDER);
        return this;
    }

    public String completeTitle() {
        return text(COMPLETE_TITLE);
    }

    public boolean continueShoppingShown() {
        return visible(CONTINUE_SHOPPING).isDisplayed();
    }

    private void typeIfGiven(By field, String resourceId, String value) {
        if (value == null || value.isEmpty()) {
            return;
        }
        visible(scrollToId(resourceId));
        type(field, value);
    }
}
