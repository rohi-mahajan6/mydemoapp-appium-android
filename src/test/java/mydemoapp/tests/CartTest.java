package mydemoapp.tests;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import mydemoapp.core.TestData;
import mydemoapp.pages.AppDialog;
import mydemoapp.pages.CartPage;
import mydemoapp.pages.CatalogPage.Product;
import mydemoapp.pages.CheckoutPage;
import mydemoapp.pages.LoginPage;
import mydemoapp.pages.ProductDetailPage;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@Feature("Cart and checkout")
public class CartTest extends BaseTest {

    @Test(description = "Adding a product puts it in the cart with the catalog price")
    public void addToCart() {
        Product product = catalog.productAt(0);
        ProductDetailPage detail = catalog.open(product.name());
        assertEquals(detail.name(), product.name(), "detail screen shows the product that was tapped");

        detail.addToCart();
        assertEquals(header.cartCount(), 1, "cart badge");

        CartPage cart = header.openCart().waitForItems();
        assertEquals(cart.itemNames(), List.of(product.name()));
        assertEquals(cart.itemQuantities(), List.of(1));
        assertEquals(amount(cart.itemPrices().get(0)), amount(product.price()), 0.001, "line price");
        assertEquals(amount(cart.totalPrice()), amount(product.price()), 0.001, "cart total");
    }

    @Test(description = "Checkout while logged out: login is asked for, then the order completes")
    @Description("CartFragment sends a logged-out user to the login screen with the checkout flag; "
            + "after login the address form opens, then payment, review and the completion screen.")
    public void checkoutFlow() {
        Product product = catalog.productAt(0);
        catalog.open(product.name()).addToCart();
        header.openCart().waitForItems().proceedToCheckout();

        LoginPage login = new LoginPage(driver);
        assertTrue(login.isShown(), "login asked for at checkout");
        login.loginAs(TestData.user("standard"), TestData.password());

        CheckoutPage checkout = new CheckoutPage(driver)
                .waitForAddressForm()
                .enterAddress(TestData.address())
                .enterPayment(TestData.payment());

        assertEquals(checkout.reviewFullName(), TestData.address().path("fullName").asText(), "review: shipping name");
        // The review total includes delivery, so it can only be at least the product price.
        assertTrue(amount(checkout.reviewTotal()) >= amount(product.price()), "review total covers the product");

        checkout.placeOrder();
        assertEquals(checkout.completeTitle(), "Checkout Complete");
        assertTrue(checkout.continueShoppingShown(), "Continue Shopping button shown");
        assertEquals(header.cartCount(), 0, "cart emptied after the order");
    }

    @Test(description = "Reset App State empties the cart and logs out")
    @Description("MainActivity.showResetDialog: RESET APP clears the cart and the login, then confirms "
            + "with \"App State has been reset.\"")
    public void appResetClearsCart() {
        header.openLogin().loginAs(TestData.user("standard"), TestData.password());
        catalog.waitUntilShown();
        catalog.open(catalog.productAt(0).name()).addToCart();
        assertEquals(header.cartCount(), 1, "cart badge before reset");

        header.choose("Reset App State");
        AppDialog confirm = new AppDialog(driver);
        assertEquals(confirm.message(), "Are you sure you want to Reset the App");
        confirm.confirm();

        AppDialog done = new AppDialog(driver);
        assertEquals(done.message(), "App State has been reset.");
        done.confirm();

        assertEquals(header.cartCount(), 0, "cart badge after reset");
        CartPage cart = header.openCart();
        assertTrue(cart.isEmpty(), "cart shows no items");
        assertEquals(cart.emptyTitle(), "No Items");
        assertFalse(header.menuOffersLogout(), "logged out after reset");
    }
}
