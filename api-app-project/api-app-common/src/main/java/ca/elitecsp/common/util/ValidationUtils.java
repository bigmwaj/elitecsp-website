package ca.elitecsp.common.util;

import ca.elitecsp.common.exception.ApiException;
import ca.elitecsp.common.exception.ErrorCode;

import java.util.Arrays;
import java.util.Base64;

public final class ValidationUtils {

    private ValidationUtils() {
        // Utility class – do not instantiate
    }

    public static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public static void requireNonBlank(String value, String fieldName) {
        if (isBlank(value)) {
            throw new ApiException(ErrorCode.MISSING_REQUIRED_FIELD, 400, fieldName + " must not be blank");
        }
    }

    public static boolean isValidEmail(String email) {
        return email != null && email.matches(Constants.EMAIL_REGEX);
    }

    public static void requireValidEmail(String email, String fieldName) {
        requireNonBlank(email, fieldName);
        if (!isValidEmail(email)) {
            throw new ApiException(ErrorCode.INVALID_EMAIL, 400,
                    fieldName + " address is invalid: " + email);
        }
    }

    public static byte[] decodeBase64File(String base64File) {
        try {
            String base64Data = base64File.contains(",")
                    ? base64File.split(",", 2)[1]
                    : base64File;
            return Base64.getDecoder().decode(base64Data);
        } catch (IllegalArgumentException e) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, 400,
                    "File is not valid Base64: " + e.getMessage(), e);
        }
    }

    public static void requireCvSizeWithinLimit(byte[] fileBytes) {
        if (fileBytes.length > Constants.MAX_CV_SIZE_BYTES) {
            throw new ApiException(ErrorCode.FILE_TOO_LARGE, 400,
                    "CV file exceeds the maximum allowed size of 5 MB (actual: "
                    + fileBytes.length + " bytes)");
        }
    }

    public static void requirePdfMagicBytes(byte[] fileBytes) {
        byte[] magic = Constants.PDF_MAGIC_BYTES;
        if (fileBytes.length < magic.length) {
            throw new ApiException(ErrorCode.INVALID_FILE_TYPE, 400, "CV file is too small to be a valid PDF");
        }
        if (!Arrays.equals(fileBytes, 0, magic.length, magic, 0, magic.length)) {
            throw new ApiException(ErrorCode.INVALID_FILE_TYPE, 400, "CV file must be a valid PDF document");
        }
    }

    public static void requireAllowedFileType(byte[] fileBytes, String fileName) {
        String lowerName = fileName == null ? "" : fileName.toLowerCase();
        if (lowerName.endsWith(".pdf")) {
            requirePdfMagicBytes(fileBytes);
        } else if (lowerName.endsWith(".docx")) {
            requireDocxMagicBytes(fileBytes);
        } else {
            throw new ApiException(ErrorCode.INVALID_FILE_TYPE, 400, "Unsupported file type. Allowed types: PDF, DOCX");
        }
    }

    private static void requireDocxMagicBytes(byte[] fileBytes) {
        byte[] magic = Constants.DOCX_MAGIC_BYTES;
        if (fileBytes.length < magic.length) {
            throw new ApiException(ErrorCode.INVALID_FILE_TYPE, 400, "File is too small to be a valid DOCX document");
        }
        if (!Arrays.equals(fileBytes, 0, magic.length, magic, 0, magic.length)) {
            throw new ApiException(ErrorCode.INVALID_FILE_TYPE, 400, "DOCX file must be a valid Office Open XML document");
        }
    }

}
