package ca.elitecsp.common.util;

import ca.elitecsp.common.exception.CustomException;
import ca.elitecsp.common.exception.ErrorCode;
import ca.elitecsp.fixtures.TestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ValidationUtils}.
 *
 * <p>Covers blank-string detection, email format validation, Base64 file decoding,
 * CV size enforcement, and file-type magic-byte checks.
 */
@DisplayName("ValidationUtils")
class ValidationUtilsTest {

    // -------------------------------------------------------------------------
    // isBlank
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("isBlank returns true for null")
    void isBlank_null_returnsTrue() {
        assertTrue(ValidationUtils.isBlank(null));
    }

    @Test
    @DisplayName("isBlank returns true for empty string")
    void isBlank_empty_returnsTrue() {
        assertTrue(ValidationUtils.isBlank(""));
    }

    @Test
    @DisplayName("isBlank returns true for whitespace-only string")
    void isBlank_whitespace_returnsTrue() {
        assertTrue(ValidationUtils.isBlank("   "));
    }

    @Test
    @DisplayName("isBlank returns false for non-blank string")
    void isBlank_nonBlank_returnsFalse() {
        assertFalse(ValidationUtils.isBlank("hello"));
    }

    // -------------------------------------------------------------------------
    // requireNonBlank
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("requireNonBlank with non-blank value → no exception")
    void requireNonBlank_nonBlank_doesNotThrow() {
        assertDoesNotThrow(() -> ValidationUtils.requireNonBlank("value", "Field"));
    }

    @Test
    @DisplayName("requireNonBlank with null → throws MISSING_REQUIRED_FIELD HTTP 400")
    void requireNonBlank_null_throwsMissingRequiredField() {
        CustomException ex = assertThrows(CustomException.class,
                () -> ValidationUtils.requireNonBlank(null, "TestField"));
        assertEquals(ErrorCode.MISSING_REQUIRED_FIELD, ex.getErrorCode());
        assertEquals(400, ex.getHttpStatus());
        assertTrue(ex.getMessage().contains("TestField"));
    }

    @Test
    @DisplayName("requireNonBlank with blank string → throws MISSING_REQUIRED_FIELD")
    void requireNonBlank_blank_throwsMissingRequiredField() {
        CustomException ex = assertThrows(CustomException.class,
                () -> ValidationUtils.requireNonBlank("  ", "MyField"));
        assertEquals(ErrorCode.MISSING_REQUIRED_FIELD, ex.getErrorCode());
    }

    // -------------------------------------------------------------------------
    // isValidEmail
    // -------------------------------------------------------------------------

    @ParameterizedTest(name = "valid email: {0}")
    @ValueSource(strings = {
        "user@example.com",
        "first.last@sub.domain.org",
        "user+tag@example.co.uk",
        "123@numbers.io"
    })
    @DisplayName("isValidEmail returns true for valid email addresses")
    void isValidEmail_validAddresses_returnsTrue(String email) {
        assertTrue(ValidationUtils.isValidEmail(email));
    }

    @ParameterizedTest(name = "invalid email: {0}")
    @ValueSource(strings = {
        "not-an-email",
        "@nodomain",
        "user@",
        "user@domain",
        ""
    })
    @DisplayName("isValidEmail returns false for invalid email addresses")
    void isValidEmail_invalidAddresses_returnsFalse(String email) {
        assertFalse(ValidationUtils.isValidEmail(email));
    }

    @Test
    @DisplayName("isValidEmail returns false for null")
    void isValidEmail_null_returnsFalse() {
        assertFalse(ValidationUtils.isValidEmail(null));
    }

    // -------------------------------------------------------------------------
    // requireValidEmail
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("requireValidEmail with valid address → no exception")
    void requireValidEmail_valid_doesNotThrow() {
        assertDoesNotThrow(() ->
                ValidationUtils.requireValidEmail("user@example.com", "Email"));
    }

    @Test
    @DisplayName("requireValidEmail with blank email → throws MISSING_REQUIRED_FIELD")
    void requireValidEmail_blank_throwsMissingRequiredField() {
        CustomException ex = assertThrows(CustomException.class,
                () -> ValidationUtils.requireValidEmail("", "Email"));
        assertEquals(ErrorCode.MISSING_REQUIRED_FIELD, ex.getErrorCode());
    }

    @Test
    @DisplayName("requireValidEmail with invalid format → throws INVALID_EMAIL")
    void requireValidEmail_invalidFormat_throwsInvalidEmail() {
        CustomException ex = assertThrows(CustomException.class,
                () -> ValidationUtils.requireValidEmail("bad-email", "Email"));
        assertEquals(ErrorCode.INVALID_EMAIL, ex.getErrorCode());
        assertEquals(400, ex.getHttpStatus());
    }

    // -------------------------------------------------------------------------
    // decodeBase64File
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("decodeBase64File with plain Base64 string → returns decoded bytes")
    void decodeBase64File_plainBase64_returnsBytes() {
        byte[] original = TestFixtures.validPdfBytes();
        String b64 = Base64.getEncoder().encodeToString(original);

        byte[] decoded = ValidationUtils.decodeBase64File(b64);

        assertArrayEquals(original, decoded);
    }

    @Test
    @DisplayName("decodeBase64File with data-URI prefix → strips prefix and returns bytes")
    void decodeBase64File_dataUriPrefix_stripsAndReturnsBytes() {
        byte[] original = TestFixtures.validPdfBytes();
        String dataUri = "data:application/pdf;base64,"
                + Base64.getEncoder().encodeToString(original);

        byte[] decoded = ValidationUtils.decodeBase64File(dataUri);

        assertArrayEquals(original, decoded);
    }

    @Test
    @DisplayName("decodeBase64File with invalid Base64 → throws VALIDATION_ERROR")
    void decodeBase64File_invalidBase64_throwsValidationError() {
        CustomException ex = assertThrows(CustomException.class,
                () -> ValidationUtils.decodeBase64File("!!!not-valid!!!"));
        assertEquals(ErrorCode.VALIDATION_ERROR, ex.getErrorCode());
        assertEquals(400, ex.getHttpStatus());
    }

    // -------------------------------------------------------------------------
    // requireCvSizeWithinLimit
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("requireCvSizeWithinLimit with file exactly at limit → no exception")
    void requireCvSizeWithinLimit_atLimit_doesNotThrow() {
        byte[] maxSize = new byte[Constants.MAX_CV_SIZE_BYTES];
        assertDoesNotThrow(() -> ValidationUtils.requireCvSizeWithinLimit(maxSize));
    }

    @Test
    @DisplayName("requireCvSizeWithinLimit with file one byte over limit → throws FILE_TOO_LARGE")
    void requireCvSizeWithinLimit_oneByteOver_throwsFileTooLarge() {
        byte[] oversized = new byte[Constants.MAX_CV_SIZE_BYTES + 1];
        CustomException ex = assertThrows(CustomException.class,
                () -> ValidationUtils.requireCvSizeWithinLimit(oversized));
        assertEquals(ErrorCode.FILE_TOO_LARGE, ex.getErrorCode());
        assertEquals(400, ex.getHttpStatus());
    }

    @Test
    @DisplayName("requireCvSizeWithinLimit with small file → no exception")
    void requireCvSizeWithinLimit_smallFile_doesNotThrow() {
        assertDoesNotThrow(() ->
                ValidationUtils.requireCvSizeWithinLimit(TestFixtures.validPdfBytes()));
    }

    // -------------------------------------------------------------------------
    // requireAllowedFileType
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("requireAllowedFileType with valid PDF → no exception")
    void requireAllowedFileType_validPdf_doesNotThrow() {
        assertDoesNotThrow(() ->
                ValidationUtils.requireAllowedFileType(TestFixtures.validPdfBytes(), "resume.pdf"));
    }

    @Test
    @DisplayName("requireAllowedFileType with valid DOCX → no exception")
    void requireAllowedFileType_validDocx_doesNotThrow() {
        assertDoesNotThrow(() ->
                ValidationUtils.requireAllowedFileType(TestFixtures.validDocxBytes(), "cv.docx"));
    }

    @Test
    @DisplayName("requireAllowedFileType with unsupported extension → throws INVALID_FILE_TYPE")
    void requireAllowedFileType_unsupportedExtension_throwsInvalidFileType() {
        CustomException ex = assertThrows(CustomException.class,
                () -> ValidationUtils.requireAllowedFileType("hello".getBytes(), "cv.txt"));
        assertEquals(ErrorCode.INVALID_FILE_TYPE, ex.getErrorCode());
        assertEquals(400, ex.getHttpStatus());
    }

    @Test
    @DisplayName("requireAllowedFileType with null filename → throws INVALID_FILE_TYPE")
    void requireAllowedFileType_nullFilename_throwsInvalidFileType() {
        CustomException ex = assertThrows(CustomException.class,
                () -> ValidationUtils.requireAllowedFileType(TestFixtures.validPdfBytes(), null));
        assertEquals(ErrorCode.INVALID_FILE_TYPE, ex.getErrorCode());
    }

    @Test
    @DisplayName("requireAllowedFileType .pdf extension but wrong magic bytes → throws INVALID_FILE_TYPE")
    void requireAllowedFileType_pdfExtensionWrongMagic_throwsInvalidFileType() {
        byte[] fakeBytes = new byte[20]; // zeros – not %PDF
        CustomException ex = assertThrows(CustomException.class,
                () -> ValidationUtils.requireAllowedFileType(fakeBytes, "cv.pdf"));
        assertEquals(ErrorCode.INVALID_FILE_TYPE, ex.getErrorCode());
    }

    @Test
    @DisplayName("requireAllowedFileType .docx extension but wrong magic bytes → throws INVALID_FILE_TYPE")
    void requireAllowedFileType_docxExtensionWrongMagic_throwsInvalidFileType() {
        byte[] fakeBytes = new byte[20]; // zeros – not PK\x03\x04
        CustomException ex = assertThrows(CustomException.class,
                () -> ValidationUtils.requireAllowedFileType(fakeBytes, "cv.docx"));
        assertEquals(ErrorCode.INVALID_FILE_TYPE, ex.getErrorCode());
    }

    // -------------------------------------------------------------------------
    // requirePdfMagicBytes edge cases
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("requirePdfMagicBytes with too-short byte array → throws INVALID_FILE_TYPE")
    void requirePdfMagicBytes_tooShort_throwsInvalidFileType() {
        byte[] tooShort = {0x25, 0x50}; // only 2 bytes, need 4
        CustomException ex = assertThrows(CustomException.class,
                () -> ValidationUtils.requirePdfMagicBytes(tooShort));
        assertEquals(ErrorCode.INVALID_FILE_TYPE, ex.getErrorCode());
    }

    @Test
    @DisplayName("requirePdfMagicBytes with valid PDF magic → no exception")
    void requirePdfMagicBytes_valid_doesNotThrow() {
        assertDoesNotThrow(() ->
                ValidationUtils.requirePdfMagicBytes(TestFixtures.validPdfBytes()));
    }
}
