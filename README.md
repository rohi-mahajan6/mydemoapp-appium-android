# mydemoapp-appium-android

Appium tests for the Sauce Labs [My Demo App](https://github.com/saucelabs/my-demo-app-android) on Android. They run on the Android emulator that GitHub Actions provides.

> The app is Sauce Labs' public demo app for practising mobile automation. This project is not affiliated with Sauce Labs. The APK is downloaded from the official release in CI and checked against a fixed SHA-256. It is never committed to this repository.

## 1. Stack

| Part | Version |
| ---- | ------- |
| App under test | My Demo App 2.3.0 (`mda-2.3.0-27.apk`), package `com.saucelabs.mydemoapp.android`, target SDK 31 |
| Language and build | Java 17, Maven |
| Automation | Appium 2.19.0, UiAutomator2 driver 4.2.9, java-client 9.4.0 |
| Test runner and report | TestNG 7.12.0, Allure 2 |
| Device | Android emulator, API 31 (Android 12), `google_apis` x86_64 image, Pixel 6 profile |
| CI | GitHub Actions with `reactivecircus/android-emulator-runner` |

## 2. What is tested

| Class | Test | What it checks |
| ----- | ---- | -------------- |
| `LoginTest` | `validUserLogsIn` | `bod@example.com` logs in, lands on the catalog, and the menu then offers Log Out |
| | `lockedOutUserIsRefused` | `alice@example.com` stays on the login screen with "Sorry this user has been locked out." |
| | `emptyUsernameIsRejected`, `emptyPasswordIsRejected` | each field shows its own error |
| `CartTest` | `addToCart` | the product from the catalog is in the cart with quantity 1 and the catalog price; the badge shows 1 |
| | `checkoutFlow` | logged out at checkout: login is asked for, then address, payment, review and "Checkout Complete"; the cart is emptied |
| | `appResetClearsCart` | "Reset App State" empties the cart and logs out |
| `BackgroundTest` | `backgroundAndForegroundKeepsState` | after Home, the app is in the background; after `activateApp`, the same cart screen and contents are back |
| `PermissionTest` | `cameraDeniedReturnsToCatalog` | camera revoked, QR scanner opened, system dialog denied: the app returns to the catalog and still has no camera permission |
| | `locationAllowedShowsCoordinates` | location revoked, Geo Location opened, Android 12 dialog answered with Precise and "While using the app", GPS position set through Appium: the screen shows that latitude and longitude |

How the suite is built:

- **Page objects** are in `src/test/java/mydemoapp/pages`. Their locators are resource IDs and content descriptions taken from the app's own layouts at tag 2.3.0.
- **The system permission dialog** has its own object, `pages/system/SystemPermissionDialog`. It matches the permission controller's views by resource ID, so it works on both Google and AOSP images.
- **Every test starts from a clean app.** Its data is cleared, so the cart is empty and nobody is logged in.
- **Permission tests reinstall the app** before each test. This also clears Android's "don't ask again" flags.
- **On failure,** a screenshot, the page source and the current activity are attached to Allure.

## 3. Findings

_None yet._

## 4. CI pipeline

_To be written after the first CI run._

## 5. Execution evidence

_Pending: no CI run yet._

## 6. Run locally

The tests need an Android emulator or device and an Appium server, so they run in CI only. Locally, the project only compiles:

```bash
mvn -B test-compile
```

## License

MIT. See [LICENSE](LICENSE).

The app under test belongs to Sauce Labs and is not included in this repository.
