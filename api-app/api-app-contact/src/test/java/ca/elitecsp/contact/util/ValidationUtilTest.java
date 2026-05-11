package ca.elitecsp.contact.util;

import ca.elitecsp.common.exception.ApiException;
import ca.elitecsp.common.exception.ErrorCode;
import ca.elitecsp.contact.model.ContactRequest;
import ca.elitecsp.contact.model.ContactType;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilTest {

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static final byte[] VALID_PDF_BYTES = new byte[]{0x25, 0x50, 0x44, 0x46, 0x01};
    private static final byte[] VALID_DOCX_BYTES = new byte[]{0x50, 0x4B, 0x03, 0x04, 0x01};

    private static String base64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    private ContactRequest validContactRequest() {
        ContactRequest req = new ContactRequest();
        req.setType(ContactType.CONTACT);
        req.setFullName("Alice Example");
        req.setEmail("alice@example.com");
        req.setMessage("Hello!");
        return req;
    }

    // -------------------------------------------------------------------------
    // null request
    // -------------------------------------------------------------------------

    @Test
    void validateContactRequest_throws_whenNull() {
        ApiException ex = assertThrows(ApiException.class,
                () -> ValidationUtil.validateContactRequest(null));
        assertEquals(ErrorCode.MISSING_REQUIRED_FIELD, ex.getErrorCode());
    }

    // -------------------------------------------------------------------------
    // Required fields
    // -------------------------------------------------------------------------

    @Test
    void validateContactRequest_passes_forValidContactRequest() {
        assertDoesNotThrow(() -> ValidationUtil.validateContactRequest(validContactRequest()));
    }

    @Test
    void validateContactRequest_throws_whenFullNameBlank() {
        ContactRequest req = validContactRequest();
        req.setFullName("");
        ApiException ex = assertThrows(ApiException.class,
                () -> ValidationUtil.validateContactRequest(req));
        assertEquals(ErrorCode.MISSING_REQUIRED_FIELD, ex.getErrorCode());
    }

    @Test
    void validateContactRequest_throws_whenEmailInvalid() {
        ContactRequest req = validContactRequest();
        req.setEmail("not-an-email");
        ApiException ex = assertThrows(ApiException.class,
                () -> ValidationUtil.validateContactRequest(req));
        assertEquals(ErrorCode.INVALID_EMAIL, ex.getErrorCode());
    }

    @Test
    void validateContactRequest_throws_whenMessageBlank() {
        ContactRequest req = validContactRequest();
        req.setMessage("");
        ApiException ex = assertThrows(ApiException.class,
                () -> ValidationUtil.validateContactRequest(req));
        assertEquals(ErrorCode.MISSING_REQUIRED_FIELD, ex.getErrorCode());
    }

    // -------------------------------------------------------------------------
    // JOB_APPLICATION: attachment required
    // -------------------------------------------------------------------------

    @Test
    void validateContactRequest_throws_forJobApplication_whenAttachmentMissing() {
        ContactRequest req = validContactRequest();
        req.setType(ContactType.JOB_APPLICATION);
        ApiException ex = assertThrows(ApiException.class,
                () -> ValidationUtil.validateContactRequest(req));
        assertEquals(ErrorCode.MISSING_REQUIRED_FIELD, ex.getErrorCode());
    }

    @Test
    void validateContactRequest_throws_forJobApplication_whenFileNameMissing() {
        ContactRequest req = validContactRequest();
        req.setType(ContactType.JOB_APPLICATION);
        req.setAttachment(base64(VALID_PDF_BYTES));
        ApiException ex = assertThrows(ApiException.class,
                () -> ValidationUtil.validateContactRequest(req));
        assertEquals(ErrorCode.MISSING_REQUIRED_FIELD, ex.getErrorCode());
    }

    @Test
    void validateContactRequest_passes_forJobApplication_withValidPdfAttachment() {
        ContactRequest req = validContactRequest();
        req.setType(ContactType.JOB_APPLICATION);
        req.setAttachment(base64(VALID_PDF_BYTES));
        req.setAttachmentFileName("resume.pdf");
        assertDoesNotThrow(() -> ValidationUtil.validateContactRequest(req));
    }

    @Test
    void validateContactRequest_passes_forJobApplication_withValidDocxAttachment() {
        ContactRequest req = validContactRequest();
        req.setType(ContactType.JOB_APPLICATION);
        req.setAttachment(base64(VALID_DOCX_BYTES));
        req.setAttachmentFileName("resume.docx");
        assertDoesNotThrow(() -> ValidationUtil.validateContactRequest(req));
    }

    // -------------------------------------------------------------------------
    // CONTACT: attachment optional
    // -------------------------------------------------------------------------

    @Test
    void validateContactRequest_passes_forContact_withNoAttachment() {
        ContactRequest req = validContactRequest();
        assertDoesNotThrow(() -> ValidationUtil.validateContactRequest(req));
    }

    @Test
    void validateContactRequest_passes_forContact_withValidOptionalAttachment() {
        ContactRequest req = validContactRequest();
        req.setAttachment(base64(VALID_PDF_BYTES));
        req.setAttachmentFileName("document.pdf");
        assertDoesNotThrow(() -> ValidationUtil.validateContactRequest(req));
    }

    @Test
    void validateContactRequest_throws_forContact_whenAttachmentPresentButNoFileName() {
        ContactRequest req = validContactRequest();
        req.setAttachment(base64(VALID_PDF_BYTES));
        ApiException ex = assertThrows(ApiException.class,
                () -> ValidationUtil.validateContactRequest(req));
        assertEquals(ErrorCode.MISSING_REQUIRED_FIELD, ex.getErrorCode());
    }
}
