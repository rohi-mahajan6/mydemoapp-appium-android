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

Behaviour observed on My Demo App 2.3.0 on the emulator and confirmed in the app's source. Both are covered by the tests, so a change in behaviour shows up in the report.

| ID | Finding | Evidence |
| -- | ------- | -------- |
| APP-001 | **The QR scanner asks for the camera twice.** Opening the scanner without the permission shows the system dialog twice in a row ("Allow My Demo App to take pictures and record video?"). The second dialog uses Android's "don't ask again" variant (`permission_deny_and_dont_ask_again_button`). `QRFragment` requests the permission both when its view is set up and again in `onResume`. After both are denied, the app returns to the catalog. | `cameraDeniedReturnsToCatalog` records the number of dialogs: 2 in run 45452401750 |
| APP-002 | **The cart is not emptied when the order is placed.** On the "Checkout Complete" screen the cart badge still shows the ordered item. The cart is only cleared when **Continue Shopping** is tapped (`CheckoutCompleteFragment`). A user who leaves the completion screen another way still has the ordered items in the cart. | `checkoutFlow` records the badge on the completion screen (1), then checks it is 0 after Continue Shopping |

Also seen, not counted as an app finding: on the emulator Appium can set the GPS position but cannot read it back (`mobile: getGeolocation` fails with "Cannot execute the 'retrieve geolocation' action"). The location test only records that readback. It checks the position the app shows on screen.

## 4. CI pipeline

`.github/workflows/e2e.yml` runs on every push to `main`, on pull requests and on demand:

1. Java 17 and Node set up; KVM enabled for the emulator.
2. The APK is downloaded from the Sauce Labs release and checked against its SHA-256.
3. Appium 2.19.0 and the UiAutomator2 4.2.9 driver are installed; the tests are compiled.
4. An Android 12 (API 31, `google_apis`, x86_64, Pixel 6) emulator snapshot is created, or restored from the cache.
5. `reactivecircus/android-emulator-runner` boots the emulator and runs `ci/run-tests.sh`. The script:
   - starts logcat and the Appium server in the background;
   - waits for Appium to answer;
   - runs the TestNG suite with Maven.
6. The Allure report is built and published to GitHub Pages from `main`. The raw Allure results, the TestNG reports, `appium.log` and `logcat.txt` are uploaded as artifacts.

## 5. Execution evidence

CI run [45452401750](https://github.com/rohi-mahajan6/mydemoapp-appium-android/actions/runs/45452401750), commit `6eead6d`, 2026-11-24, on the GitHub-hosted emulator (Android 12, API 31). **10 passed, 0 failed, 0 skipped.** The test step took 194 s. [Allure report](https://rohi-mahajan6.github.io/mydemoapp-appium-android/).

| Test | Result | Recorded in the run |
| ---- | ------ | ------------------- |
| LoginTest · validUserLogsIn | passed | |
| LoginTest · lockedOutUserIsRefused | passed | |
| LoginTest · emptyUsernameIsRejected | passed | |
| LoginTest · emptyPasswordIsRejected | passed | the message shown is "Enter Password" |
| CartTest · addToCart | passed | |
| CartTest · checkoutFlow | passed | cart badge 1 on Checkout Complete, 0 after Continue Shopping (APP-002) |
| CartTest · appResetClearsCart | passed | |
| BackgroundTest · backgroundAndForegroundKeepsState | passed | |
| PermissionTest · cameraDeniedReturnsToCatalog | passed | 2 camera dialogs, both denied (APP-001) |
| PermissionTest · locationAllowedShowsCoordinates | passed | position set 52.3676, 4.9041; shown by the app 52.3676, 4.9040983 (tolerance 0.001°) |

Earlier runs, and what each one fixed:

- [45440441628](https://github.com/rohi-mahajan6/mydemoapp-appium-android/actions/runs/45440441628): no tests ran. maven-surefire-plugin 3.6+ no longer reads `testng.xml`, so it is pinned to 3.5.6.
- [45447987289](https://github.com/rohi-mahajan6/mydemoapp-appium-android/actions/runs/45447987289): 4 passed, 6 failed.
  - Product titles are not clickable; products are now opened through their image.
  - The second camera dialog has a different "Don't allow" ID; both IDs are now matched.
  - The location grant was checked about 20 ms before Android recorded it (from `appium.log` and logcat); the test now waits for it.
- [45450932364](https://github.com/rohi-mahajan6/mydemoapp-appium-android/actions/runs/45450932364): 8 passed, 2 failed.
  - The cart only empties on Continue Shopping (APP-002).
  - Reading the position back is not supported on the emulator, so it is now recorded only.

## 6. Run locally

The tests need an Android emulator or device and an Appium server, so they run in CI only. Locally, the project only compiles:

```bash
mvn -B test-compile
```

## License

MIT. See [LICENSE](LICENSE).

The app under test belongs to Sauce Labs and is not included in this repository.
