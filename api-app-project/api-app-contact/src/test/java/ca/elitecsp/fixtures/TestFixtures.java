package ca.elitecsp.fixtures;

import ca.elitecsp.contact.model.ContactRequest;
import ca.elitecsp.contact.model.ContactType;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;

import java.util.Base64;

/**
 * Reusable test data fixtures shared across all test classes.
 *
 * <p>Provides valid and invalid payloads for Contact and JobApplication scenarios.
 */
public final class TestFixtures {

    private TestFixtures() {
        // Utility class – do not instantiate
    }

    // -------------------------------------------------------------------------
    // Binary file fixtures
    // -------------------------------------------------------------------------

    /** Minimal valid PDF bytes: {@code %PDF} magic + filler. */
    public static byte[] validPdfBytes() {
        byte[] bytes = new byte[20];
        bytes[0] = 0x25; // %
        bytes[1] = 0x50; // P
        bytes[2] = 0x44; // D
        bytes[3] = 0x46; // F
        return bytes;
    }

    /** Minimal valid DOCX bytes: ZIP PK\x03\x04 magic + filler. */
    public static byte[] validDocxBytes() {
        byte[] bytes = new byte[20];
        bytes[0] = 0x50; // P
        bytes[1] = 0x4B; // K
        bytes[2] = 0x03;
        bytes[3] = 0x04;
        return bytes;
    }

    /** Returns a Base64 string for a minimal valid PDF file. */
    public static String base64Pdf() {
        return Base64.getEncoder().encodeToString(validPdfBytes());
    }

    /** Returns a Base64 string for a minimal valid DOCX file. */
    public static String base64Docx() {
        return Base64.getEncoder().encodeToString(validDocxBytes());
    }

    /** Returns a Base64 data-URI string for a minimal valid PDF file. */
    public static String dataUriPdf() {
        return "data:application/pdf;base64," + base64Pdf();
    }

    // -------------------------------------------------------------------------
    // ContactRequest model fixtures
    // -------------------------------------------------------------------------

    /** Returns a fully populated, valid {@link ContactRequest} of type CONTACT. */
    public static ContactRequest validContactRequest() {
        ContactRequest req = new ContactRequest();
        req.setFullName("John Doe");
        req.setEmail("john.doe@example.com");
        req.setPhone("555-0100");
        req.setMessage("Hello, I would like to get in touch.");
        return req;
    }

    /** Returns a valid {@link ContactRequest} of type CONTACT with an optional PDF attachment. */
    public static ContactRequest contactRequestWithAttachment() {
        ContactRequest req = validContactRequest();
        req.setAttachment(base64Pdf());
        req.setAttachmentFileName("document.pdf");
        req.setFileBytes(validPdfBytes());
        return req;
    }

    /** Returns a fully populated, valid {@link ContactRequest} of type JOB_APPLICATION. */
    public static ContactRequest validJobApplicationRequest() {
        ContactRequest req = new ContactRequest();
        req.setType(ContactType.JOB_APPLICATION);
        req.setFullName("Jane Smith");
        req.setEmail("jane.smith@example.com");
        req.setPhone("555-0200");
        req.setMessage("I am interested in the open position.");
        req.setAttachment(base64Pdf());
        req.setAttachmentFileName("cv.pdf");
        req.setFileBytes(validPdfBytes());
        return req;
    }

    // -------------------------------------------------------------------------
    // JSON body fixtures
    // -------------------------------------------------------------------------

    /** Returns a JSON string for a minimal valid contact request. */
    public static String contactRequestJson() {
        return """
                {
                  "fullName": "John Doe",
                  "email": "john.doe@example.com",
                  "phone": "555-0100",
                  "message": "Hello, I would like to get in touch."
                }
                """;
    }

    /** Returns a JSON string for a valid job-application request with a Base64 PDF attachment. */
    public static String jobApplicationRequestJson() {
        return """
                {
                  "type": "JOB_APPLICATION",
                  "fullName": "Jane Smith",
                  "email": "jane.smith@example.com",
                  "phone": "555-0200",
                  "message": "I am interested in the open position.",
                  "attachment": "%s",
                  "attachmentFileName": "cv.pdf"
                }
                """.formatted(base64Pdf());
    }

    // -------------------------------------------------------------------------
    // APIGatewayProxyRequestEvent helpers
    // -------------------------------------------------------------------------

    /** Wraps the given body string in a minimal {@link APIGatewayProxyRequestEvent}. */
    public static APIGatewayProxyRequestEvent apiGatewayRequest(String body) {
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        event.setBody(body);
        return event;
    }
}
