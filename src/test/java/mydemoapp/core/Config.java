package mydemoapp.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.Properties;

/**
 * Settings from src/test/resources/config.properties. A JVM system property with the same name
 * wins, which is how Maven passes apk.path and appium.url (see pom.xml and ci/run-tests.sh).
 */
public final class Config {

    private static final Properties FILE = load();

    private Config() {
    }

    private static Properties load() {
        Properties p = new Properties();
        try (InputStream in = Config.class.getResourceAsStream("/config.properties")) {
            if (in == null) {
                throw new IllegalStateException("config.properties not found on the test classpath");
            }
            p.load(in);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return p;
    }

    public static String get(String key) {
        String value = System.getProperty(key, FILE.getProperty(key));
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing setting: " + key);
        }
        return value.trim();
    }

    public static int getInt(String key) {
        return Integer.parseInt(get(key));
    }

    public static boolean getBoolean(String key) {
        return Boolean.parseBoolean(get(key));
    }

    public static String appPackage() {
        return get("appPackage");
    }

    public static Duration explicitWait() {
        return Duration.ofSeconds(getInt("explicitWait"));
    }
}
