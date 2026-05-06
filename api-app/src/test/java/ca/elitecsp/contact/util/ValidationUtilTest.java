package ca.elitecsp.contact.util;

import ca.elitecsp.common.exception.CustomException;
import ca.elitecsp.common.exception.ErrorCode;
import ca.elitecsp.contact.model.ContactRequest;
import ca.elitecsp.contact.model.ContactType;
import ca.elitecsp.fixtures.TestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ValidationUtil}.
 *
 * <p>Covers all validation rules for Contact and JobApplication request types,
 * including required field checks, email format, file size limits, and file type
 * verification.
 */
@DisplayName("ValidationUtil")
class ValidationUtilTest {

    // -------------------------------------------------------------------------
    // Null / non-null guard
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("null request → throws MISSING_REQUIRED_FIELD")
    void validateContactRequest_nullRequest_throwsMissingField() {
        CustomException ex = assertThrows(CustomException.class,
                () -> ValidationUtil.validateContactRequest(null));
        assertEquals(ErrorCode.MISSING_REQUIRED_FIELD, ex.getErrorCode());
    }

    // -------------------------------------------------------------------------
    // Required fields – CONTACT type
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("valid contact request → no exception")
    void validateContactRequest_validContact_doesNotThrow() {
        assertDoesNotThrow(() ->
                ValidationUtil.validateContactRequest(TestFixtures.validContactRequest()));
    }

    @Test
    @DisplayName("contact request with null fullName → throws MISSING_REQUIRED_FIELD")
    void validateContactRequest_nullFullName_throwsMissingField() {
        ContactRequest req = TestFixtures.validContactRequest();
        req.setFullName(null);

        CustomException ex = assertThrows(CustomException.class,
                () -> ValidationUtil.validateContactRequest(req));
        assertEquals(ErrorCode.MISSING_REQUIRED_FIELD, ex.getErrorCode());
        assertEquals(400, ex.getHttpStatus());
    }

    @Test
    @DisplayName("contact request with blank fullName → throws MISSING_REQUIRED_FIELD")
    void validateContactRequest_blankFullName_throwsMissingField() {
        ContactRequest req = TestFixtures.validContactRequest();
        req.setFullName("   ");

        CustomException ex = assertThrows(CustomException.class,
                () -> ValidationUtil.validateContactRequest(req));
        assertEquals(ErrorCode.MISSING_REQUIRED_FIELD, ex.getErrorCode());
    }

    @Test
    @DisplayName("contact request with null email → throws MISSING_REQUIRED_FIELD")
    void validateContactRequest_nullEmail_throwsMissingField() {
        ContactRequest req = TestFixtures.validContactRequest();
        req.setEmail(null);

        CustomException ex = assertThrows(CustomException.class,
                () -> ValidationUtil.validateContactRequest(req));
        assertEquals(ErrorCode.MISSING_REQUIRED_FIELD, ex.getErrorCode());
    }

    @Test
    @DisplayName("contact request with invalid email format → throws INVALID_EMAIL")
    void validateContactRequest_invalidEmail_throwsInvalidEmail() {
        ContactRequest req = TestFixtures.validContactRequest();
        req.setEmail("not-an-email");

        CustomException ex = assertThrows(CustomException.class,
                () -> ValidationUtil.validateContactRequest(req));
        assertEquals(ErrorCode.INVALID_EMAIL, ex.getErrorCode());
    }

    @Test
    @DisplayName("contact request with null message → throws MISSING_REQUIRED_FIELD")
    void validateContactRequest_nullMessage_throwsMissingField() {
        ContactRequest req = TestFixtures.validContactRequest();
        req.setMessage(null);

        CustomException ex = assertThrows(CustomException.class,
                () -> ValidationUtil.validateContactRequest(req));
        assertEquals(ErrorCode.MISSING_REQUIRED_FIELD, ex.getErrorCode());
    }

    @Test
    @DisplayName("contact request with blank message → throws MISSING_REQUIRED_FIELD")
    void validateContactRequest_blankMessage_throwsMissingField() {
        ContactRequest req = TestFixtures.validContactRequest();
        req.setMessage("  ");

        CustomException ex = assertThrows(CustomException.class,
                () -> ValidationUtil.validateContactRequest(req));
        assertEquals(ErrorCode.MISSING_REQUIRED_FIELD, ex.getErrorCode());
    }

    // -------------------------------------------------------------------------
    // Optional attachment for CONTACT type
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("contact request with valid PDF attachment → no exception")
    void validateContactRequest_contactWithPdfAttachment_doesNotThrow() {
        assertDoesNotThrow(() ->
                ValidationUtil.validateContactRequest(TestFixtures.contactRequestWithAttachment()));
    }

    @Test
    @DisplayName("contact request with valid DOCX attachment → no exception")
    void validateContactRequest_contactWithDocxAttachment_doesNotThrow() {
        ContactRequest req = TestFixtures.validContactRequest();
        req.setAttachment(TestFixtures.base64Docx());
        req.setAttachmentFileName("document.docx");

        assertDoesNotThrow(() -> ValidationUtil.validateContactRequest(req));
    }

    @Test
    @DisplayName("contact request with attachment but null file name → throws INVALID_FILE_TYPE")
    void validateContactRequest_contactAttachmentNullFileName_throwsInvalidFileType() {
        // requireAllowedFileType is called before requireNonBlank; a null filename
        // maps to an empty extension, which is unsupported → INVALID_FILE_TYPE
        ContactRequest req = TestFixtures.validContactRequest();
        req.setAttachment(TestFixtures.base64Pdf());
        req.setAttachmentFileName(null);

        CustomException ex = assertThrows(CustomException.class,
                () -> ValidationUtil.validateContactRequest(req));
        assertEquals(ErrorCode.INVALID_FILE_TYPE, ex.getErrorCode());
    }

    @Test
    @DisplayName("contact request with oversized attachment → throws FILE_TOO_LARGE")
    void validateContactRequest_contactAttachmentTooLarge_throwsFileTooLarge() {
        // Build a byte array slightly above 5 MB limit with PDF magic bytes
        byte[] oversized = new byte[5 * 1024 * 1024 + 1];
        oversized[0] = 0x25; // %
        oversized[1] = 0x50; // P
        oversized[2] = 0x44; // D
        oversized[3] = 0x46; // F
        String b64 = Base64.getEncoder().encodeToString(oversized);

        ContactRequest req = TestFixtures.validContactRequest();
        req.setAttachment(b64);
        req.setAttachmentFileName("big.pdf");

        CustomException ex = assertThrows(CustomException.class,
                () -> ValidationUtil.validateContactRequest(req));
        assertEquals(ErrorCode.FILE_TOO_LARGE, ex.getErrorCode());
    }

    @Test
    @DisplayName("contact request with attachment having unsupported extension → throws INVALID_FILE_TYPE")
    void validateContactRequest_contactAttachmentUnsupportedType_throwsInvalidFileType() {
        ContactRequest req = TestFixtures.validContactRequest();
        req.setAttachment(TestFixtures.base64Pdf());
        req.setAttachmentFileName("cv.txt");

        CustomException ex = assertThrows(CustomException.class,
                () -> ValidationUtil.validateContactRequest(req));
        assertEquals(ErrorCode.INVALID_FILE_TYPE, ex.getErrorCode());
    }

    // -------------------------------------------------------------------------
    // JOB_APPLICATION type – required fields
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("valid job-application request → no exception")
    void validateContactRequest_validJobApplication_doesNotThrow() {
        assertDoesNotThrow(() ->
                ValidationUtil.validateContactRequest(TestFixtures.validJobApplicationRequest()));
    }

    @Test
    @DisplayName("job application with null attachment → throws MISSING_REQUIRED_FIELD")
    void validateContactRequest_jobApplicationNullAttachment_throwsMissingField() {
        ContactRequest req = TestFixtures.validJobApplicationRequest();
        req.setAttachment(null);

        CustomException ex = assertThrows(CustomException.class,
                () -> ValidationUtil.validateContactRequest(req));
        assertEquals(ErrorCode.MISSING_REQUIRED_FIELD, ex.getErrorCode());
    }

    @Test
    @DisplayName("job application with blank attachment → throws MISSING_REQUIRED_FIELD")
    void validateContactRequest_jobApplicationBlankAttachment_throwsMissingField() {
        ContactRequest req = TestFixtures.validJobApplicationRequest();
        req.setAttachment("");

        CustomException ex = assertThrows(CustomException.class,
                () -> ValidationUtil.validateContactRequest(req));
        assertEquals(ErrorCode.MISSING_REQUIRED_FIELD, ex.getErrorCode());
    }

    @Test
    @DisplayName("job application with null attachment file name → throws MISSING_REQUIRED_FIELD")
    void validateContactRequest_jobApplicationNullFileName_throwsMissingField() {
        ContactRequest req = TestFixtures.validJobApplicationRequest();
        req.setAttachmentFileName(null);

        CustomException ex = assertThrows(CustomException.class,
                () -> ValidationUtil.validateContactRequest(req));
        assertEquals(ErrorCode.MISSING_REQUIRED_FIELD, ex.getErrorCode());
    }

    @Test
    @DisplayName("job application with oversized file → throws FILE_TOO_LARGE")
    void validateContactRequest_jobApplicationFileTooLarge_throwsFileTooLarge() {
        byte[] oversized = new byte[5 * 1024 * 1024 + 1];
        oversized[0] = 0x25;
        oversized[1] = 0x50;
        oversized[2] = 0x44;
        oversized[3] = 0x46;
        String b64 = Base64.getEncoder().encodeToString(oversized);

        ContactRequest req = new ContactRequest();
        req.setType(ContactType.JOB_APPLICATION);
        req.setFullName("Jane Smith");
        req.setEmail("jane.smith@example.com");
        req.setMessage("I am applying.");
        req.setAttachment(b64);
        req.setAttachmentFileName("cv.pdf");

        CustomException ex = assertThrows(CustomException.class,
                () -> ValidationUtil.validateContactRequest(req));
        assertEquals(ErrorCode.FILE_TOO_LARGE, ex.getErrorCode());
    }

    @Test
    @DisplayName("job application with invalid file type → throws INVALID_FILE_TYPE")
    void validateContactRequest_jobApplicationInvalidFileType_throwsInvalidFileType() {
        byte[] txtBytes = "not a pdf or docx".getBytes();
        String b64 = Base64.getEncoder().encodeToString(txtBytes);

        ContactRequest req = new ContactRequest();
        req.setType(ContactType.JOB_APPLICATION);
        req.setFullName("Jane Smith");
        req.setEmail("jane.smith@example.com");
        req.setMessage("Applying with invalid file.");
        req.setAttachment(b64);
        req.setAttachmentFileName("cv.txt");

        CustomException ex = assertThrows(CustomException.class,
                () -> ValidationUtil.validateContactRequest(req));
        assertEquals(ErrorCode.INVALID_FILE_TYPE, ex.getErrorCode());
    }

    @Test
    @DisplayName("job application with invalid base64 data → throws VALIDATION_ERROR")
    void validateContactRequest_jobApplicationInvalidBase64_throwsValidationError() {
        ContactRequest req = new ContactRequest();
        req.setType(ContactType.JOB_APPLICATION);
        req.setFullName("Jane Smith");
        req.setEmail("jane.smith@example.com");
        req.setMessage("Applying with broken base64.");
        req.setAttachment("!!!not-valid-base64!!!");
        req.setAttachmentFileName("cv.pdf");

        CustomException ex = assertThrows(CustomException.class,
                () -> ValidationUtil.validateContactRequest(req));
        assertEquals(ErrorCode.VALIDATION_ERROR, ex.getErrorCode());
    }

    @Test
    @DisplayName("job application with PDF magic bytes mismatch → throws INVALID_FILE_TYPE")
    void validateContactRequest_jobApplicationPdfMagicBytesMismatch_throwsInvalidFileType() {
        byte[] fakePdf = new byte[20]; // all zeros – not a valid PDF
        String b64 = Base64.getEncoder().encodeToString(fakePdf);

        ContactRequest req = new ContactRequest();
        req.setType(ContactType.JOB_APPLICATION);
        req.setFullName("Jane Smith");
        req.setEmail("jane.smith@example.com");
        req.setMessage("Fake PDF.");
        req.setAttachment(b64);
        req.setAttachmentFileName("cv.pdf");

        CustomException ex = assertThrows(CustomException.class,
                () -> ValidationUtil.validateContactRequest(req));
        assertEquals(ErrorCode.INVALID_FILE_TYPE, ex.getErrorCode());
    }
}
