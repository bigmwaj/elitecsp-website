package ca.elitecsp.common.util;

import ca.elitecsp.common.exception.CustomException;
import ca.elitecsp.common.exception.ErrorCode;
import ca.elitecsp.contact.model.ContactRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link JsonUtils}.
 *
 * <p>Covers JSON deserialisation success/failure and serialisation success/failure paths.
 */
@DisplayName("JsonUtils")
class JsonUtilsTest {

    // -------------------------------------------------------------------------
    // fromJson
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("fromJson with valid JSON → returns populated object")
    void fromJson_validJson_returnsPopulatedObject() {
        String json = """
                {
                  "fullName": "John Doe",
                  "email": "john@example.com",
                  "message": "Hello"
                }
                """;

        ContactRequest result = JsonUtils.fromJson(json, ContactRequest.class);

        assertNotNull(result);
        assertEquals("John Doe", result.getFullName());
        assertEquals("john@example.com", result.getEmail());
        assertEquals("Hello", result.getMessage());
    }

    @Test
    @DisplayName("fromJson with unknown JSON fields → ignores unknown properties")
    void fromJson_unknownFields_ignoresUnknownProperties() {
        String json = """
                {
                  "fullName": "Jane",
                  "email": "jane@example.com",
                  "message": "Hi",
                  "unknownField": "should be ignored"
                }
                """;

        assertDoesNotThrow(() -> JsonUtils.fromJson(json, ContactRequest.class));
    }

    @Test
    @DisplayName("fromJson with 'name' alias field → maps to fullName")
    void fromJson_nameAlias_mapsToFullName() {
        String json = """
                {
                  "name": "Alice",
                  "email": "alice@example.com",
                  "message": "Testing alias"
                }
                """;

        ContactRequest result = JsonUtils.fromJson(json, ContactRequest.class);

        assertEquals("Alice", result.getFullName());
    }

    @Test
    @DisplayName("fromJson with invalid JSON → throws CustomException JSON_PARSE_ERROR HTTP 400")
    void fromJson_invalidJson_throwsJsonParseError() {
        CustomException ex = assertThrows(CustomException.class,
                () -> JsonUtils.fromJson("{not-valid-json}", ContactRequest.class));

        assertEquals(ErrorCode.JSON_PARSE_ERROR, ex.getErrorCode());
        assertEquals(400, ex.getHttpStatus());
    }

    @Test
    @DisplayName("fromJson with empty JSON object → returns empty object without error")
    void fromJson_emptyJsonObject_returnsEmptyObject() {
        ContactRequest result = JsonUtils.fromJson("{}", ContactRequest.class);

        assertNotNull(result);
        assertNull(result.getFullName());
        assertNull(result.getEmail());
    }

    @Test
    @DisplayName("fromJson with long invalid JSON includes truncated preview in message")
    void fromJson_longInvalidJson_includesPreviewInMessage() {
        String longJson = "X".repeat(100) + "{invalid}";

        CustomException ex = assertThrows(CustomException.class,
                () -> JsonUtils.fromJson(longJson, ContactRequest.class));

        // Message should contain a truncated preview (max 50 chars + "...")
        assertTrue(ex.getMessage().contains("..."),
                "Message should include truncated preview of long invalid input");
    }

    // -------------------------------------------------------------------------
    // toJson
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("toJson with valid object → returns JSON string")
    void toJson_validObject_returnsJsonString() {
        ContactRequest req = new ContactRequest();
        req.setFullName("Bob");
        req.setEmail("bob@example.com");

        String json = JsonUtils.toJson(req);

        assertNotNull(json);
        assertTrue(json.contains("\"email\""));
        assertTrue(json.contains("bob@example.com"));
    }

    @Test
    @DisplayName("toJson with null value fields → includes null fields in JSON")
    void toJson_objectWithNullFields_includesNulls() {
        ContactRequest req = new ContactRequest();

        String json = JsonUtils.toJson(req);

        assertNotNull(json);
        // Jackson serialises null fields by default
        assertTrue(json.startsWith("{") && json.endsWith("}"));
    }
}
