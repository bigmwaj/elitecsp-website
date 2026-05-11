package ca.elitecsp.common.util;

import ca.elitecsp.common.exception.ApiException;
import ca.elitecsp.common.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class JsonUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private JsonUtils() {
        // Utility class – do not instantiate
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (Exception e) {
            // Include a preview of the JSON in the error message for debugging
            String jsonPreview = json != null && json.length() > 50
                ? json.substring(0, 50) + "..."
                : json;
            String errorMsg = "Failed to parse request JSON: " + e.getMessage() +
                    " [Input preview: " + jsonPreview + "]";
            throw new ApiException(ErrorCode.JSON_PARSE_ERROR, 400, errorMsg, e);
        }
    }

    public static String toJson(Object object) {
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (Exception e) {
            throw new ApiException(ErrorCode.INTERNAL_ERROR, 500,
                    "Failed to serialise response to JSON: " + e.getMessage(), e);
        }
    }
}
