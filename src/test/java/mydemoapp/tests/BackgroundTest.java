package mydemoapp.tests;

import io.appium.java_client.appmanagement.ApplicationState;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import mydemoapp.pages.CartPage;
import mydemoapp.pages.CatalogPage.Product;
import org.testng.annotations.Test;

import java.util.EnumSet;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@Feature("App state")
public class BackgroundTest extends BaseTest {

    @Test(description = "Home and back: the app keeps the cart and the screen it was on")
    @Description("Add a product, open the cart, press Home, check the app is in the background, bring "
            + "it back with activateApp, and check the same cart screen and contents are shown.")
    public void backgroundAndForegroundKeepsState() throws InterruptedException {
        Product product = catalog.productAt(0);
        catalog.open(product.name()).addToCart();
        CartPage cart = header.openCart().waitForItems();
        assertEquals(cart.itemNames(), List.of(product.name()), "cart before backgrounding");

        app.pressHome();
        ApplicationState inBackground = waitForState(
                EnumSet.of(ApplicationState.RUNNING_IN_BACKGROUND, ApplicationState.RUNNING_IN_BACKGROUND_SUSPENDED));
        assertTrue(inBackground == ApplicationState.RUNNING_IN_BACKGROUND
                        || inBackground == ApplicationState.RUNNING_IN_BACKGROUND_SUSPENDED,
                "app in the background after Home, state was " + inBackground);

        Thread.sleep(5_000); // stay in the background for a while, as a user switching apps would

        app.activate();
        assertEquals(waitForState(EnumSet.of(ApplicationState.RUNNING_IN_FOREGROUND)),
                ApplicationState.RUNNING_IN_FOREGROUND, "app in the foreground after activateApp");

        CartPage back = new CartPage(driver).waitForItems();
        assertEquals(back.itemNames(), List.of(product.name()), "same cart contents after returning");
        assertEquals(back.itemQuantities(), List.of(1), "same quantity after returning");
        assertEquals(header.cartCount(), 1, "cart badge after returning");
    }

    /** Polls the app state for up to 10 s; returns the first expected state, or the last one seen. */
    private ApplicationState waitForState(EnumSet<ApplicationState> expected) throws InterruptedException {
        ApplicationState state = app.state();
        for (int i = 0; i < 20 && !expected.contains(state); i++) {
            Thread.sleep(500);
            state = app.state();
        }
        return state;
    }
}
