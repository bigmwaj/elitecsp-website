package ca.elitecsp.contact.util;

import ca.elitecsp.common.exception.ApiException;
import ca.elitecsp.common.exception.ErrorCode;
import ca.elitecsp.common.util.ValidationUtils;
import ca.elitecsp.contact.model.ContactRequest;
import ca.elitecsp.contact.model.ContactType;

public final class ValidationUtil {

    private ValidationUtil() {
        // Utility class – do not instantiate
    }

    public static void validateContactRequest(ContactRequest request) {
        if (request == null) {
            throw new ApiException(ErrorCode.MISSING_REQUIRED_FIELD, 400,
                    "Request body must not be null");
        }

        ValidationUtils.requireNonBlank(request.getFullName(), "Full name");
        ValidationUtils.requireValidEmail(request.getEmail(), "Email");
        ValidationUtils.requireNonBlank(request.getMessage(), "Message");

        ContactType type = request.getEffectiveType();

        if (type == ContactType.JOB_APPLICATION) {
            // Attachment is mandatory for job applications
            ValidationUtils.requireNonBlank(request.getAttachment(), "Attachment");
            ValidationUtils.requireNonBlank(request.getAttachmentFileName(), "Attachment file name");
            byte[] fileBytes = ValidationUtils.decodeBase64File(request.getAttachment());
            ValidationUtils.requireCvSizeWithinLimit(fileBytes);
            ValidationUtils.requireAllowedFileType(fileBytes, request.getAttachmentFileName());
        } else {
            // CONTACT: attachment is optional; validate if present
            if (!ValidationUtils.isBlank(request.getAttachment())) {
                ValidationUtils.requireNonBlank(request.getAttachmentFileName(), "Attachment file name");
                byte[] fileBytes = ValidationUtils.decodeBase64File(request.getAttachment());
                ValidationUtils.requireCvSizeWithinLimit(fileBytes);
                ValidationUtils.requireAllowedFileType(fileBytes, request.getAttachmentFileName());
            }
        }
    }
}
