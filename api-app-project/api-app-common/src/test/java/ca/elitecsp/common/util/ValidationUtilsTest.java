package ca.elitecsp.common.util;

import ca.elitecsp.common.exception.ApiException;
import ca.elitecsp.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilsTest {

    // -------------------------------------------------------------------------
    // isBlank
    // -------------------------------------------------------------------------

    @Test
    void isBlank_returnsTrue_whenNull() {
        assertTrue(ValidationUtils.isBlank(null));
    }

    @Test
    void isBlank_returnsTrue_whenEmpty() {
        assertTrue(ValidationUtils.isBlank(""));
    }

    @Test
    void isBlank_returnsTrue_whenWhitespace() {
        assertTrue(ValidationUtils.isBlank("   "));
    }

    @Test
    void isBlank_returnsFalse_whenNonBlank() {
        assertFalse(ValidationUtils.isBlank("hello"));
    }

    // -------------------------------------------------------------------------
    // requireNonBlank
    // -------------------------------------------------------------------------

    @Test
    void requireNonBlank_doesNotThrow_whenValuePresent() {
        assertDoesNotThrow(() -> ValidationUtils.requireNonBlank("value", "field"));
    }

    @Test
    void requireNonBlank_throws_whenBlank() {
        ApiException ex = assertThrows(ApiException.class,
                () -> ValidationUtils.requireNonBlank("", "MyField"));
        assertEquals(ErrorCode.MISSING_REQUIRED_FIELD, ex.getErrorCode());
        assertEquals(400, ex.getHttpStatus());
        assertTrue(ex.getMessage().contains("MyField"));
    }

    @Test
    void requireNonBlank_throws_whenNull() {
        ApiException ex = assertThrows(ApiException.class,
                () -> ValidationUtils.requireNonBlank(null, "Email"));
        assertEquals(ErrorCode.MISSING_REQUIRED_FIELD, ex.getErrorCode());
    }

    // -------------------------------------------------------------------------
    // isValidEmail / requireValidEmail
    // -------------------------------------------------------------------------

    @Test
    void isValidEmail_returnsTrue_forWellFormedAddress() {
        assertTrue(ValidationUtils.isValidEmail("user@example.com"));
    }

    @Test
    void isValidEmail_returnsFalse_forMissingAt() {
        assertFalse(ValidationUtils.isValidEmail("userexample.com"));
    }

    @Test
    void isValidEmail_returnsFalse_forNull() {
        assertFalse(ValidationUtils.isValidEmail(null));
    }

    @Test
    void requireValidEmail_doesNotThrow_forValidAddress() {
        assertDoesNotThrow(() -> ValidationUtils.requireValidEmail("a@b.ca", "Email"));
    }

    @Test
    void requireValidEmail_throws_forInvalidEmail() {
        ApiException ex = assertThrows(ApiException.class,
                () -> ValidationUtils.requireValidEmail("not-an-email", "Email"));
        assertEquals(ErrorCode.INVALID_EMAIL, ex.getErrorCode());
        assertEquals(400, ex.getHttpStatus());
    }

    @Test
    void requireValidEmail_throws_forBlankEmail() {
        ApiException ex = assertThrows(ApiException.class,
                () -> ValidationUtils.requireValidEmail("", "Email"));
        assertEquals(ErrorCode.MISSING_REQUIRED_FIELD, ex.getErrorCode());
    }

    // -------------------------------------------------------------------------
    // decodeBase64File
    // -------------------------------------------------------------------------

    @Test
    void decodeBase64File_decodesRawBase64() {
        byte[] original = "hello".getBytes();
        String encoded = Base64.getEncoder().encodeToString(original);
        assertArrayEquals(original, ValidationUtils.decodeBase64File(encoded));
    }

    @Test
    void decodeBase64File_stripsDataUriPrefix() {
        byte[] original = "hello".getBytes();
        String encoded = "data:application/pdf;base64," + Base64.getEncoder().encodeToString(original);
        assertArrayEquals(original, ValidationUtils.decodeBase64File(encoded));
    }

    @Test
    void decodeBase64File_throws_forInvalidBase64() {
        ApiException ex = assertThrows(ApiException.class,
                () -> ValidationUtils.decodeBase64File("!!!not-base64!!!"));
        assertEquals(ErrorCode.VALIDATION_ERROR, ex.getErrorCode());
    }

    // -------------------------------------------------------------------------
    // requireCvSizeWithinLimit
    // -------------------------------------------------------------------------

    @Test
    void requireCvSizeWithinLimit_doesNotThrow_whenBelowLimit() {
        assertDoesNotThrow(() -> ValidationUtils.requireCvSizeWithinLimit(new byte[1024]));
    }

    @Test
    void requireCvSizeWithinLimit_throws_whenExceedsLimit() {
        byte[] overLimit = new byte[Constants.MAX_CV_SIZE_BYTES + 1];
        ApiException ex = assertThrows(ApiException.class,
                () -> ValidationUtils.requireCvSizeWithinLimit(overLimit));
        assertEquals(ErrorCode.FILE_TOO_LARGE, ex.getErrorCode());
    }

    // -------------------------------------------------------------------------
    // requireAllowedFileType
    // -------------------------------------------------------------------------

    @Test
    void requireAllowedFileType_accepts_validPdf() {
        byte[] pdf = {0x25, 0x50, 0x44, 0x46, 0x01, 0x02};
        assertDoesNotThrow(() -> ValidationUtils.requireAllowedFileType(pdf, "resume.pdf"));
    }

    @Test
    void requireAllowedFileType_accepts_validDocx() {
        byte[] docx = {0x50, 0x4B, 0x03, 0x04, 0x01, 0x02};
        assertDoesNotThrow(() -> ValidationUtils.requireAllowedFileType(docx, "resume.docx"));
    }

    @Test
    void requireAllowedFileType_throws_forUnsupportedExtension() {
        ApiException ex = assertThrows(ApiException.class,
                () -> ValidationUtils.requireAllowedFileType(new byte[]{1, 2, 3}, "resume.txt"));
        assertEquals(ErrorCode.INVALID_FILE_TYPE, ex.getErrorCode());
    }

    @Test
    void requireAllowedFileType_throws_forInvalidPdfMagicBytes() {
        byte[] notPdf = {0x00, 0x01, 0x02, 0x03};
        ApiException ex = assertThrows(ApiException.class,
                () -> ValidationUtils.requireAllowedFileType(notPdf, "resume.pdf"));
        assertEquals(ErrorCode.INVALID_FILE_TYPE, ex.getErrorCode());
    }
}
