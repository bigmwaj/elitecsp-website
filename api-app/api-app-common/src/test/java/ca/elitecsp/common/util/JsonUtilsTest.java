package ca.elitecsp.common.util;

import ca.elitecsp.common.exception.ApiException;
import ca.elitecsp.common.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JsonUtilsTest {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Sample {
        private String name;
        private int value;
    }

    // -------------------------------------------------------------------------
    // fromJson
    // -------------------------------------------------------------------------

    @Test
    void fromJson_deserializesValidJson() {
        Sample result = JsonUtils.fromJson("{\"name\":\"Alice\",\"value\":42}", Sample.class);
        assertEquals("Alice", result.getName());
        assertEquals(42, result.getValue());
    }

    @Test
    void fromJson_ignoresUnknownFields() {
        Sample result = JsonUtils.fromJson("{\"name\":\"Bob\",\"value\":1,\"extra\":\"ignored\"}", Sample.class);
        assertEquals("Bob", result.getName());
    }

    @Test
    void fromJson_throws_forInvalidJson() {
        ApiException ex = assertThrows(ApiException.class,
                () -> JsonUtils.fromJson("{not valid json}", Sample.class));
        assertEquals(ErrorCode.JSON_PARSE_ERROR, ex.getErrorCode());
        assertEquals(400, ex.getHttpStatus());
    }

    @Test
    void fromJson_throws_forNull() {
        assertThrows(ApiException.class,
                () -> JsonUtils.fromJson(null, Sample.class));
    }

    // -------------------------------------------------------------------------
    // toJson
    // -------------------------------------------------------------------------

    @Test
    void toJson_serializesObject() {
        Sample sample = new Sample("Alice", 42);
        String json = JsonUtils.toJson(sample);
        assertTrue(json.contains("\"name\":\"Alice\""));
        assertTrue(json.contains("\"value\":42"));
    }

    @Test
    void toJson_roundtrips() {
        Sample original = new Sample("Bob", 7);
        String json = JsonUtils.toJson(original);
        Sample restored = JsonUtils.fromJson(json, Sample.class);
        assertEquals(original, restored);
    }
}
