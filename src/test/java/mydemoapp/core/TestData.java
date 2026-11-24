package mydemoapp.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

/** Reads the JSON files in src/test/resources/testdata/. */
public final class TestData {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final JsonNode USERS = read("users.json");
    private static final JsonNode CHECKOUT = read("checkout.json");

    private TestData() {
    }

    private static JsonNode read(String name) {
        try (InputStream in = TestData.class.getResourceAsStream("/testdata/" + name)) {
            if (in == null) {
                throw new IllegalStateException("testdata/" + name + " not found on the test classpath");
            }
            return MAPPER.readTree(in);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static String password() {
        return USERS.path("password").asText();
    }

    public static String user(String role) {
        return required(USERS.path("users").path(role), "users." + role);
    }

    public static String message(String key) {
        return required(USERS.path("messages").path(key), "messages." + key);
    }

    public static JsonNode address() {
        return CHECKOUT.path("address");
    }

    public static JsonNode payment() {
        return CHECKOUT.path("payment");
    }

    public static double latitude() {
        return CHECKOUT.path("geo").path("latitude").asDouble();
    }

    public static double longitude() {
        return CHECKOUT.path("geo").path("longitude").asDouble();
    }

    private static String required(JsonNode node, String path) {
        if (node.isMissingNode() || node.asText().isEmpty()) {
            throw new IllegalStateException("Missing test data: " + path);
        }
        return node.asText();
    }
}
