package ca.elitecsp.contact.handler;

import ca.elitecsp.contact.model.ContactRequest;
import ca.elitecsp.contact.model.ContactType;
import ca.elitecsp.contact.service.EmailService;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContactLambdaHandlerTest {

    @Mock
    private EmailService emailService;

    @Mock
    private Context context;

    private ContactLambdaHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ContactLambdaHandler(emailService);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static final byte[] VALID_PDF_BYTES = {0x25, 0x50, 0x44, 0x46, 0x01};

    private APIGatewayProxyRequestEvent requestWithBody(String body) {
        APIGatewayProxyRequestEvent req = new APIGatewayProxyRequestEvent();
        req.setBody(body);
        return req;
    }

    private String contactJson(String name, String email, String message) {
        return String.format(
                "{\"type\":\"CONTACT\",\"fullName\":\"%s\",\"email\":\"%s\",\"message\":\"%s\"}",
                name, email, message);
    }

    // -------------------------------------------------------------------------
    // CONTACT type
    // -------------------------------------------------------------------------

    @Test
    void handleRequest_returns200_forValidContactRequest() {
        APIGatewayProxyRequestEvent req = requestWithBody(
                contactJson("Alice", "alice@example.com", "Hello!"));

        APIGatewayProxyResponseEvent resp = handler.handleRequest(req, context);

        assertEquals(200, resp.getStatusCode());
        verify(emailService, times(1)).sendContactEmail(any(ContactRequest.class));
        verify(emailService, times(1)).sendContactConfirmationEmail(any(ContactRequest.class));
        verify(emailService, never()).sendJobApplicationEmail(any());
        verify(emailService, never()).sendJobApplicationConfirmationEmail(any());
    }

    @Test
    void handleRequest_returns400_whenBodyIsEmpty() {
        APIGatewayProxyRequestEvent req = requestWithBody("");
        APIGatewayProxyResponseEvent resp = handler.handleRequest(req, context);
        assertEquals(400, resp.getStatusCode());
        verifyNoInteractions(emailService);
    }

    @Test
    void handleRequest_returns400_whenBodyIsNull() {
        APIGatewayProxyRequestEvent req = requestWithBody(null);
        APIGatewayProxyResponseEvent resp = handler.handleRequest(req, context);
        assertEquals(400, resp.getStatusCode());
        verifyNoInteractions(emailService);
    }

    @Test
    void handleRequest_returns400_forInvalidJson() {
        APIGatewayProxyRequestEvent req = requestWithBody("{not valid json}");
        APIGatewayProxyResponseEvent resp = handler.handleRequest(req, context);
        assertEquals(400, resp.getStatusCode());
    }

    @Test
    void handleRequest_returns400_whenEmailMissing() {
        APIGatewayProxyRequestEvent req = requestWithBody(
                "{\"type\":\"CONTACT\",\"fullName\":\"Alice\",\"message\":\"Hi\"}");
        APIGatewayProxyResponseEvent resp = handler.handleRequest(req, context);
        assertEquals(400, resp.getStatusCode());
    }

    @Test
    void handleRequest_returns400_whenEmailInvalid() {
        APIGatewayProxyRequestEvent req = requestWithBody(
                "{\"type\":\"CONTACT\",\"fullName\":\"Alice\",\"email\":\"not-an-email\",\"message\":\"Hi\"}");
        APIGatewayProxyResponseEvent resp = handler.handleRequest(req, context);
        assertEquals(400, resp.getStatusCode());
    }

    // -------------------------------------------------------------------------
    // JOB_APPLICATION type
    // -------------------------------------------------------------------------

    @Test
    void handleRequest_returns200_forValidJobApplication() {
        String attachment = Base64.getEncoder().encodeToString(VALID_PDF_BYTES);
        String body = String.format(
                "{\"type\":\"JOB_APPLICATION\",\"fullName\":\"Bob\",\"email\":\"bob@example.com\"," +
                "\"message\":\"Apply\",\"attachment\":\"%s\",\"attachmentFileName\":\"cv.pdf\"}",
                attachment);

        APIGatewayProxyResponseEvent resp = handler.handleRequest(requestWithBody(body), context);

        assertEquals(200, resp.getStatusCode());
        verify(emailService, times(1)).sendJobApplicationEmail(any(ContactRequest.class));
        verify(emailService, times(1)).sendJobApplicationConfirmationEmail(any(ContactRequest.class));
        verify(emailService, never()).sendContactEmail(any());
        verify(emailService, never()).sendContactConfirmationEmail(any());
    }

    @Test
    void handleRequest_returns400_forJobApplication_whenAttachmentMissing() {
        String body = "{\"type\":\"JOB_APPLICATION\",\"fullName\":\"Bob\"," +
                      "\"email\":\"bob@example.com\",\"message\":\"Apply\"}";
        APIGatewayProxyResponseEvent resp = handler.handleRequest(requestWithBody(body), context);
        assertEquals(400, resp.getStatusCode());
    }

    // -------------------------------------------------------------------------
    // Error handling
    // -------------------------------------------------------------------------

    @Test
    void handleRequest_returns500_whenEmailServiceThrows() {
        doThrow(new RuntimeException("SES down"))
                .when(emailService).sendContactEmail(any());

        APIGatewayProxyResponseEvent resp = handler.handleRequest(
                requestWithBody(contactJson("Alice", "alice@example.com", "Hi")), context);

        assertEquals(500, resp.getStatusCode());
    }

    @Test
    void handleRequest_responseContainsJsonContentType() {
        APIGatewayProxyResponseEvent resp = handler.handleRequest(
                requestWithBody(contactJson("Alice", "alice@example.com", "Hi")), context);
        assertTrue(resp.getHeaders().containsKey("Content-Type"));
        assertEquals("application/json", resp.getHeaders().get("Content-Type"));
    }

    @Test
    void handleRequest_usesNameAliasForFullName() {
        String body = "{\"type\":\"CONTACT\",\"name\":\"Carol\"," +
                      "\"email\":\"carol@example.com\",\"message\":\"Hi\"}";
        APIGatewayProxyResponseEvent resp = handler.handleRequest(requestWithBody(body), context);
        assertEquals(200, resp.getStatusCode());
    }

    // -------------------------------------------------------------------------
    // Confirmation email – fire-and-forget behaviour
    // -------------------------------------------------------------------------

    @Test
    void handleRequest_returns200_whenContactConfirmationEmailThrows() {
        doNothing().when(emailService).sendContactEmail(any());
        doThrow(new RuntimeException("SES down"))
                .when(emailService).sendContactConfirmationEmail(any());

        APIGatewayProxyResponseEvent resp = handler.handleRequest(
                requestWithBody(contactJson("Alice", "alice@example.com", "Hi")), context);

        assertEquals(200, resp.getStatusCode(),
                "A confirmation-email failure must not affect the API response");
    }

    @Test
    void handleRequest_returns200_whenJobApplicationConfirmationEmailThrows() {
        String attachment = Base64.getEncoder().encodeToString(VALID_PDF_BYTES);
        String body = String.format(
                "{\"type\":\"JOB_APPLICATION\",\"fullName\":\"Bob\",\"email\":\"bob@example.com\"," +
                "\"message\":\"Apply\",\"attachment\":\"%s\",\"attachmentFileName\":\"cv.pdf\"}",
                attachment);

        doNothing().when(emailService).sendJobApplicationEmail(any());
        doThrow(new RuntimeException("SES down"))
                .when(emailService).sendJobApplicationConfirmationEmail(any());

        APIGatewayProxyResponseEvent resp = handler.handleRequest(requestWithBody(body), context);

        assertEquals(200, resp.getStatusCode(),
                "A confirmation-email failure must not affect the API response");
    }
}
