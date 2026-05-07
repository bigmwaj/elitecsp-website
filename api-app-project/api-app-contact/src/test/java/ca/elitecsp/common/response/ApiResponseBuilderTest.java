package ca.elitecsp.common.response;

import ca.elitecsp.common.exception.CustomException;
import ca.elitecsp.common.exception.ErrorCode;
import ca.elitecsp.common.util.JsonUtils;
import ca.elitecsp.common.model.BaseResponse;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ApiResponseBuilder}.
 *
 * <p>Verifies that each factory method sets the correct HTTP status code,
 * response body fields, and standard headers.
 */
@DisplayName("ApiResponseBuilder")
class ApiResponseBuilderTest {

    // -------------------------------------------------------------------------
    // success
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("success returns HTTP 200")
    void success_returns200() {
        APIGatewayProxyResponseEvent response = ApiResponseBuilder.success("Done");

        assertEquals(200, response.getStatusCode());
    }

    @Test
    @DisplayName("success body has success=true and the provided message")
    void success_bodyHasSuccessTrueAndMessage() {
        APIGatewayProxyResponseEvent response = ApiResponseBuilder.success("Your message has been sent.");

        BaseResponse body = parseBody(response);
        assertTrue(body.isSuccess());
        assertEquals("Your message has been sent.", body.getMessage());
        assertNull(body.getError());
    }

    // -------------------------------------------------------------------------
    // badRequest
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("badRequest returns HTTP 400")
    void badRequest_returns400() {
        APIGatewayProxyResponseEvent response =
                ApiResponseBuilder.badRequest("Invalid input", "VALIDATION_ERROR");

        assertEquals(400, response.getStatusCode());
    }

    @Test
    @DisplayName("badRequest body has success=false, message, and error code")
    void badRequest_bodyHasCorrectFields() {
        APIGatewayProxyResponseEvent response =
                ApiResponseBuilder.badRequest("Email is required", "MISSING_REQUIRED_FIELD");

        BaseResponse body = parseBody(response);
        assertFalse(body.isSuccess());
        assertEquals("Email is required", body.getMessage());
        assertEquals("MISSING_REQUIRED_FIELD", body.getError());
    }

    // -------------------------------------------------------------------------
    // internalError
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("internalError returns HTTP 500")
    void internalError_returns500() {
        APIGatewayProxyResponseEvent response =
                ApiResponseBuilder.internalError("Server error", "INTERNAL_ERROR");

        assertEquals(500, response.getStatusCode());
    }

    @Test
    @DisplayName("internalError body has success=false, message, and error code")
    void internalError_bodyHasCorrectFields() {
        APIGatewayProxyResponseEvent response =
                ApiResponseBuilder.internalError("Something went wrong", "INTERNAL_ERROR");

        BaseResponse body = parseBody(response);
        assertFalse(body.isSuccess());
        assertEquals("Something went wrong", body.getMessage());
        assertEquals("INTERNAL_ERROR", body.getError());
    }

    // -------------------------------------------------------------------------
    // fromException
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("fromException uses httpStatus from the exception")
    void fromException_usesExceptionHttpStatus() {
        CustomException ex = new CustomException(ErrorCode.INVALID_EMAIL, 400, "Bad email");

        APIGatewayProxyResponseEvent response = ApiResponseBuilder.fromException(ex);

        assertEquals(400, response.getStatusCode());
    }

    @Test
    @DisplayName("fromException body has success=false, exception message, and error code name")
    void fromException_bodyHasCorrectFields() {
        CustomException ex = new CustomException(ErrorCode.FILE_TOO_LARGE, 400, "File exceeds limit");

        APIGatewayProxyResponseEvent response = ApiResponseBuilder.fromException(ex);

        BaseResponse body = parseBody(response);
        assertFalse(body.isSuccess());
        assertEquals("File exceeds limit", body.getMessage());
        assertEquals("FILE_TOO_LARGE", body.getError());
    }

    @Test
    @DisplayName("fromException with 500-level exception maps to HTTP 500")
    void fromException_500levelException_returns500() {
        CustomException ex = new CustomException(ErrorCode.EMAIL_SEND_FAILURE, 500, "SES error");

        APIGatewayProxyResponseEvent response = ApiResponseBuilder.fromException(ex);

        assertEquals(500, response.getStatusCode());
    }

    // -------------------------------------------------------------------------
    // Headers
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("all responses include Content-Type: application/json")
    void allResponses_includeContentTypeHeader() {
        APIGatewayProxyResponseEvent successResp = ApiResponseBuilder.success("OK");
        APIGatewayProxyResponseEvent badReqResp = ApiResponseBuilder.badRequest("Bad", "CODE");
        APIGatewayProxyResponseEvent errResp = ApiResponseBuilder.internalError("Error", "CODE");

        for (APIGatewayProxyResponseEvent resp : new APIGatewayProxyResponseEvent[]{
                successResp, badReqResp, errResp}) {
            assertNotNull(resp.getHeaders(), "Headers must not be null");
            assertEquals("application/json", resp.getHeaders().get("Content-Type"),
                    "Content-Type header must be application/json");
        }
    }

    @Test
    @DisplayName("all responses include Access-Control-Allow-Origin: *")
    void allResponses_includeCorsHeader() {
        APIGatewayProxyResponseEvent successResp = ApiResponseBuilder.success("OK");
        APIGatewayProxyResponseEvent badReqResp = ApiResponseBuilder.badRequest("Bad", "CODE");
        APIGatewayProxyResponseEvent errResp = ApiResponseBuilder.internalError("Error", "CODE");

        for (APIGatewayProxyResponseEvent resp : new APIGatewayProxyResponseEvent[]{
                successResp, badReqResp, errResp}) {
            assertEquals("*", resp.getHeaders().get("Access-Control-Allow-Origin"),
                    "CORS header must be *");
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private BaseResponse parseBody(APIGatewayProxyResponseEvent response) {
        return JsonUtils.fromJson(response.getBody(), BaseResponse.class);
    }
}
