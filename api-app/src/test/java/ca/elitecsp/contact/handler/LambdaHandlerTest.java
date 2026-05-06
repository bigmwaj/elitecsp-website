package ca.elitecsp.contact.handler;

import ca.elitecsp.common.exception.CustomException;
import ca.elitecsp.common.exception.ErrorCode;
import ca.elitecsp.contact.service.EmailService;
import ca.elitecsp.fixtures.TestFixtures;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Integration tests for {@link LambdaHandler}.
 *
 * <p>Covers the full request → response flow for Contact and JobApplication
 * scenarios, including success paths, validation errors, and unexpected exceptions.
 * All external dependencies (EmailService) are mocked.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LambdaHandler")
class LambdaHandlerTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Mock
    private EmailService emailService;

    @Mock
    private Context context;

    private LambdaHandler handler;

    @BeforeEach
    void setUp() {
        handler = new LambdaHandler(emailService);
    }

    // -------------------------------------------------------------------------
    // Success scenarios
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("valid contact request → 200 with success body")
    void handleRequest_validContact_returns200() throws Exception {
        APIGatewayProxyRequestEvent request = TestFixtures.apiGatewayRequest(
                TestFixtures.contactRequestJson());

        APIGatewayProxyResponseEvent response = handler.handleRequest(request, context);

        assertEquals(200, response.getStatusCode());
        JsonNode body = MAPPER.readTree(response.getBody());
        assertTrue(body.get("success").asBoolean());
        assertNotNull(body.get("message").asText());
        verify(emailService, times(1)).sendContactEmail(any());
        verify(emailService, never()).sendJobApplicationEmail(any());
    }

    @Test
    @DisplayName("valid job-application request → 200 with success body")
    void handleRequest_validJobApplication_returns200() throws Exception {
        APIGatewayProxyRequestEvent request = TestFixtures.apiGatewayRequest(
                TestFixtures.jobApplicationRequestJson());

        APIGatewayProxyResponseEvent response = handler.handleRequest(request, context);

        assertEquals(200, response.getStatusCode());
        JsonNode body = MAPPER.readTree(response.getBody());
        assertTrue(body.get("success").asBoolean());
        verify(emailService, times(1)).sendJobApplicationEmail(any());
        verify(emailService, never()).sendContactEmail(any());
    }

    @Test
    @DisplayName("contact request using 'name' alias for fullName → 200")
    void handleRequest_nameAliasField_returns200() throws Exception {
        String json = """
                {
                  "name": "Alice",
                  "email": "alice@example.com",
                  "phone": "555-0300",
                  "message": "Testing the name alias."
                }
                """;
        APIGatewayProxyRequestEvent request = TestFixtures.apiGatewayRequest(json);

        APIGatewayProxyResponseEvent response = handler.handleRequest(request, context);

        assertEquals(200, response.getStatusCode());
        verify(emailService, times(1)).sendContactEmail(any());
    }

    @Test
    @DisplayName("contact request with explicit CONTACT type → 200")
    void handleRequest_explicitContactType_returns200() throws Exception {
        String json = """
                {
                  "type": "CONTACT",
                  "fullName": "Bob",
                  "email": "bob@example.com",
                  "message": "Explicit type."
                }
                """;
        APIGatewayProxyResponseEvent response = handler.handleRequest(
                TestFixtures.apiGatewayRequest(json), context);

        assertEquals(200, response.getStatusCode());
        verify(emailService, times(1)).sendContactEmail(any());
    }

    @Test
    @DisplayName("response headers include Content-Type and CORS headers")
    void handleRequest_validContact_responseIncludesCorsAndContentTypeHeaders() {
        APIGatewayProxyRequestEvent request = TestFixtures.apiGatewayRequest(
                TestFixtures.contactRequestJson());

        APIGatewayProxyResponseEvent response = handler.handleRequest(request, context);

        assertNotNull(response.getHeaders());
        assertEquals("application/json", response.getHeaders().get("Content-Type"));
        assertEquals("*", response.getHeaders().get("Access-Control-Allow-Origin"));
    }

    // -------------------------------------------------------------------------
    // Validation error scenarios (400)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("null body → 400 MISSING_REQUIRED_FIELD")
    void handleRequest_nullBody_returns400() throws Exception {
        APIGatewayProxyRequestEvent request = TestFixtures.apiGatewayRequest(null);

        APIGatewayProxyResponseEvent response = handler.handleRequest(request, context);

        assertEquals(400, response.getStatusCode());
        assertErrorResponse(response, ErrorCode.MISSING_REQUIRED_FIELD);
        verifyNoInteractions(emailService);
    }

    @Test
    @DisplayName("blank body → 400 MISSING_REQUIRED_FIELD")
    void handleRequest_blankBody_returns400() throws Exception {
        APIGatewayProxyRequestEvent request = TestFixtures.apiGatewayRequest("   ");

        APIGatewayProxyResponseEvent response = handler.handleRequest(request, context);

        assertEquals(400, response.getStatusCode());
        assertErrorResponse(response, ErrorCode.MISSING_REQUIRED_FIELD);
        verifyNoInteractions(emailService);
    }

    @Test
    @DisplayName("URL-encoded body → 400 JSON_PARSE_ERROR")
    void handleRequest_urlEncodedBody_returns400() throws Exception {
        APIGatewayProxyRequestEvent request = TestFixtures.apiGatewayRequest(
                "fullName=John+Doe&email=john%40example.com");

        APIGatewayProxyResponseEvent response = handler.handleRequest(request, context);

        assertEquals(400, response.getStatusCode());
        assertErrorResponse(response, ErrorCode.JSON_PARSE_ERROR);
        verifyNoInteractions(emailService);
    }

    @Test
    @DisplayName("malformed JSON → 400 JSON_PARSE_ERROR")
    void handleRequest_invalidJson_returns400() throws Exception {
        APIGatewayProxyRequestEvent request = TestFixtures.apiGatewayRequest("{not-valid-json}");

        APIGatewayProxyResponseEvent response = handler.handleRequest(request, context);

        assertEquals(400, response.getStatusCode());
        assertErrorResponse(response, ErrorCode.JSON_PARSE_ERROR);
        verifyNoInteractions(emailService);
    }

    @Test
    @DisplayName("missing fullName → 400 MISSING_REQUIRED_FIELD")
    void handleRequest_missingFullName_returns400() throws Exception {
        String json = """
                {
                  "email": "john.doe@example.com",
                  "message": "No name provided."
                }
                """;
        APIGatewayProxyResponseEvent response = handler.handleRequest(
                TestFixtures.apiGatewayRequest(json), context);

        assertEquals(400, response.getStatusCode());
        assertErrorResponse(response, ErrorCode.MISSING_REQUIRED_FIELD);
        verifyNoInteractions(emailService);
    }

    @Test
    @DisplayName("blank email → 400 MISSING_REQUIRED_FIELD")
    void handleRequest_blankEmail_returns400() throws Exception {
        String json = """
                {
                  "fullName": "John Doe",
                  "email": "",
                  "message": "Blank email."
                }
                """;
        APIGatewayProxyResponseEvent response = handler.handleRequest(
                TestFixtures.apiGatewayRequest(json), context);

        assertEquals(400, response.getStatusCode());
        assertErrorResponse(response, ErrorCode.MISSING_REQUIRED_FIELD);
        verifyNoInteractions(emailService);
    }

    @Test
    @DisplayName("invalid email format → 400 INVALID_EMAIL")
    void handleRequest_invalidEmail_returns400() throws Exception {
        String json = """
                {
                  "fullName": "John Doe",
                  "email": "not-an-email",
                  "message": "Invalid email format."
                }
                """;
        APIGatewayProxyResponseEvent response = handler.handleRequest(
                TestFixtures.apiGatewayRequest(json), context);

        assertEquals(400, response.getStatusCode());
        assertErrorResponse(response, ErrorCode.INVALID_EMAIL);
        verifyNoInteractions(emailService);
    }

    @Test
    @DisplayName("missing message → 400 MISSING_REQUIRED_FIELD")
    void handleRequest_missingMessage_returns400() throws Exception {
        String json = """
                {
                  "fullName": "John Doe",
                  "email": "john.doe@example.com"
                }
                """;
        APIGatewayProxyResponseEvent response = handler.handleRequest(
                TestFixtures.apiGatewayRequest(json), context);

        assertEquals(400, response.getStatusCode());
        assertErrorResponse(response, ErrorCode.MISSING_REQUIRED_FIELD);
        verifyNoInteractions(emailService);
    }

    @Test
    @DisplayName("job application missing attachment → 400 MISSING_REQUIRED_FIELD")
    void handleRequest_jobApplicationMissingAttachment_returns400() throws Exception {
        String json = """
                {
                  "type": "JOB_APPLICATION",
                  "fullName": "Jane Smith",
                  "email": "jane.smith@example.com",
                  "message": "No CV attached."
                }
                """;
        APIGatewayProxyResponseEvent response = handler.handleRequest(
                TestFixtures.apiGatewayRequest(json), context);

        assertEquals(400, response.getStatusCode());
        assertErrorResponse(response, ErrorCode.MISSING_REQUIRED_FIELD);
        verifyNoInteractions(emailService);
    }

    @Test
    @DisplayName("job application missing attachment file name → 400 MISSING_REQUIRED_FIELD")
    void handleRequest_jobApplicationMissingAttachmentFileName_returns400() throws Exception {
        String json = """
                {
                  "type": "JOB_APPLICATION",
                  "fullName": "Jane Smith",
                  "email": "jane.smith@example.com",
                  "message": "CV attached but no filename.",
                  "attachment": "%s"
                }
                """.formatted(TestFixtures.base64Pdf());
        APIGatewayProxyResponseEvent response = handler.handleRequest(
                TestFixtures.apiGatewayRequest(json), context);

        assertEquals(400, response.getStatusCode());
        assertErrorResponse(response, ErrorCode.MISSING_REQUIRED_FIELD);
        verifyNoInteractions(emailService);
    }

    @Test
    @DisplayName("job application with unsupported file type → 400 INVALID_FILE_TYPE")
    void handleRequest_jobApplicationUnsupportedFileType_returns400() throws Exception {
        byte[] txtBytes = "Hello world".getBytes();
        String b64 = java.util.Base64.getEncoder().encodeToString(txtBytes);
        String json = """
                {
                  "type": "JOB_APPLICATION",
                  "fullName": "Jane Smith",
                  "email": "jane.smith@example.com",
                  "message": "TXT not allowed.",
                  "attachment": "%s",
                  "attachmentFileName": "cv.txt"
                }
                """.formatted(b64);
        APIGatewayProxyResponseEvent response = handler.handleRequest(
                TestFixtures.apiGatewayRequest(json), context);

        assertEquals(400, response.getStatusCode());
        assertErrorResponse(response, ErrorCode.INVALID_FILE_TYPE);
        verifyNoInteractions(emailService);
    }

    // -------------------------------------------------------------------------
    // Failure scenarios (500)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("EmailService throws CustomException → propagates correct status")
    void handleRequest_emailServiceThrowsCustomException_returns500() throws Exception {
        doThrow(new CustomException(ErrorCode.EMAIL_SEND_FAILURE, 500, "SES failure"))
                .when(emailService).sendContactEmail(any());

        APIGatewayProxyResponseEvent response = handler.handleRequest(
                TestFixtures.apiGatewayRequest(TestFixtures.contactRequestJson()), context);

        assertEquals(500, response.getStatusCode());
        assertErrorResponse(response, ErrorCode.EMAIL_SEND_FAILURE);
    }

    @Test
    @DisplayName("EmailService throws unexpected RuntimeException → 500 INTERNAL_ERROR")
    void handleRequest_unexpectedException_returns500() throws Exception {
        doThrow(new RuntimeException("Unexpected failure"))
                .when(emailService).sendContactEmail(any());

        APIGatewayProxyResponseEvent response = handler.handleRequest(
                TestFixtures.apiGatewayRequest(TestFixtures.contactRequestJson()), context);

        assertEquals(500, response.getStatusCode());
        assertErrorResponse(response, ErrorCode.INTERNAL_ERROR);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void assertErrorResponse(APIGatewayProxyResponseEvent response, ErrorCode expectedCode)
            throws Exception {
        JsonNode body = MAPPER.readTree(response.getBody());
        assertFalse(body.get("success").asBoolean(),
                "Expected success=false for error response");
        assertEquals(expectedCode.name(), body.get("error").asText(),
                "Expected error code " + expectedCode.name());
    }
}
