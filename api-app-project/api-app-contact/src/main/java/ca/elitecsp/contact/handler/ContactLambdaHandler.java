package ca.elitecsp.contact.handler;

import ca.elitecsp.common.exception.CustomException;
import ca.elitecsp.common.exception.ErrorCode;
import ca.elitecsp.common.response.ApiResponseBuilder;
import ca.elitecsp.common.util.JsonUtils;
import ca.elitecsp.common.util.ValidationUtils;
import ca.elitecsp.contact.model.ContactRequest;
import ca.elitecsp.contact.model.ContactType;
import ca.elitecsp.contact.service.EmailService;
import ca.elitecsp.contact.util.ValidationUtil;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ContactLambdaHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final EmailService emailService;

    public ContactLambdaHandler() {
        this.emailService = new EmailService();
    }

    /** Package-private constructor for dependency injection in tests. */
    ContactLambdaHandler(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        log.info("LambdaHandler invoked");
        try {
            ContactRequest contactRequest = parseRequest(request);
            ContactType type = contactRequest.getEffectiveType();

            log.info("Contact request received: type={}, email={}", type, contactRequest.getEmail());

            ValidationUtil.validateContactRequest(contactRequest);

            return switch (type) {
                case JOB_APPLICATION -> handleJobApplication(contactRequest);
                case CONTACT        -> handleContact(contactRequest);
            };

        } catch (CustomException e) {
            log.warn("Request error [{}]: {}", e.getErrorCode(), e.getMessage());
            return ApiResponseBuilder.fromException(e);

        } catch (Exception e) {
            log.error("Unexpected error processing contact request", e);
            return ApiResponseBuilder.internalError(
                    "An unexpected error occurred. Please try again later.",
                    ErrorCode.INTERNAL_ERROR.name());
        }
    }

    private APIGatewayProxyResponseEvent handleContact(ContactRequest req) {
        emailService.sendContactEmail(req);
        log.info("Contact email sent successfully for: {}", req.getEmail());
        return ApiResponseBuilder.success("Your message has been sent successfully.");
    }

    private APIGatewayProxyResponseEvent handleJobApplication(ContactRequest req) {
        log.info("Sending job application email with CV '{}' for applicant: {}",
                req.getAttachmentFileName(), req.getEmail());

        emailService.sendJobApplicationEmail(req);
        log.info("Job-application email sent successfully for: {}", req.getEmail());
        return ApiResponseBuilder.success("Your application has been submitted successfully.");
    }

    private ContactRequest parseRequest(APIGatewayProxyRequestEvent request) {
        String body = request.getBody();
        if (body == null || body.isBlank()) {
            throw new CustomException(ErrorCode.MISSING_REQUIRED_FIELD, 400,
                    "Request body must not be empty");
        }

        // Log the raw body for debugging purposes (first 200 chars)
        String bodyPreview = body.length() > 200 ? body.substring(0, 200) + "..." : body;
        log.debug("Parsing request body: {}", bodyPreview);

        // Check if body appears to be URL-encoded (common mistake in API Gateway configuration)
        if (body.startsWith("form-data") || body.contains("=") && !body.startsWith("{") && !body.startsWith("[")) {
            log.warn("Request body appears to be URL-encoded instead of JSON");
            throw new CustomException(ErrorCode.JSON_PARSE_ERROR, 400,
                    "Request body must be valid JSON. Ensure API Gateway is NOT converting the body. " +
                    "Content-Type should be 'application/json' and body should be raw JSON, not URL-encoded.");
        }

        ContactRequest cr = JsonUtils.fromJson(body, ContactRequest.class);
        if (cr.getAttachment() != null) {
            byte[] fileBytes = ValidationUtils.decodeBase64File(cr.getAttachment());
            cr.setFileBytes(fileBytes);
        }
        return cr;
    }
}
