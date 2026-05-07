package ca.elitecsp.contact.service;

import ca.elitecsp.common.exception.CustomException;
import ca.elitecsp.common.exception.ErrorCode;
import ca.elitecsp.common.util.Constants;
import ca.elitecsp.common.util.EmailTemplateLoader;
import ca.elitecsp.contact.model.ContactRequest;
import jakarta.activation.DataHandler;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.RawMessage;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SendRawEmailRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Service responsible for sending transactional emails via Amazon Simple Email Service (SES).
 *
 * <p>Two email types are supported:
 * <ul>
 *   <li><b>Contact</b> – sent via {@link #sendContactEmail}; supports an optional inline
 *       attachment (PDF or DOCX). When an attachment is present the email is built as a
 *       raw MIME multipart message; otherwise a simple {@code sendEmail} call is used.</li>
 *   <li><b>Job Application</b> – sent via {@link #sendJobApplicationEmail}; the CV is
 *       attached directly to the email as a MIME multipart message.</li>
 * </ul>
 *
 * <p>Email bodies (plain-text and HTML) are loaded from classpath templates:
 * <ul>
 *   <li>{@code templates/contact-email.txt} / {@code .html}</li>
 *   <li>{@code templates/job-application-email.txt} / {@code .html}</li>
 * </ul>
 *
 * <p>Required environment variables:
 * <ul>
 *   <li>{@code FROM_EMAIL}        – Verified SES sender address</li>
 *   <li>{@code DESTINATION_EMAIL} – Recipient email address for all messages</li>
 *   <li>{@code AWS_REGION}        – AWS region where SES is configured (e.g. {@code us-east-1})</li>
 * </ul>
 *
 * <p>AWS credentials are resolved automatically by the SDK's default credential chain
 * (Lambda execution role, environment variables, or instance profile) — no hardcoded secrets.
 */
@Slf4j
public class EmailService {

    private static final String ENV_FROM_EMAIL = "FROM_EMAIL";
    private static final String ENV_DESTINATION_EMAIL = "DESTINATION_EMAIL";
    private static final String ENV_AWS_REGION = "AWS_REGION";

    /** Verified SES sender (From) address. */
    private final String fromEmail;

    /** Recipient address for all notification emails. */
    private final String destinationEmail;

    /** SES client initialised with the configured AWS region. */
    private final SesClient sesClient;

    /**
     * Default no-arg constructor used by the Lambda runtime.
     * Reads configuration from environment variables and initialises the SES client.
     *
     * @throws CustomException if any required environment variable is missing
     */
    public EmailService() {
        this.fromEmail = requireEnv(ENV_FROM_EMAIL);
        this.destinationEmail = requireEnv(ENV_DESTINATION_EMAIL);
        String awsRegion = requireEnv(ENV_AWS_REGION);
        this.sesClient = SesClient.builder()
                .region(Region.of(awsRegion))
                .build();
    }

    /**
     * Package-private constructor for dependency injection in tests.
     *
     * @param fromEmail        verified SES sender address
     * @param destinationEmail recipient email address
     * @param sesClient        pre-configured SES client
     */
    EmailService(String fromEmail, String destinationEmail, SesClient sesClient) {
        this.fromEmail = fromEmail;
        this.destinationEmail = destinationEmail;
        this.sesClient = sesClient;
    }

    /**
     * Sends a contact-form notification email via Amazon SES, optionally with a file attachment.
     *
     * <p>When {@code attachmentBytes} is non-null the message is assembled as a raw MIME
     * multipart email so the attachment can be included. Otherwise a simple
     * {@code sendEmail} call (text + HTML) is used.
     *
     * @throws CustomException with {@link ErrorCode#EMAIL_SEND_FAILURE} (HTTP 500) if sending fails
     */
    public void sendContactEmail(ContactRequest req) {
        log.info("Sending contact email via SES on behalf of: {}", req.getEmail());
        try {
            String emailSubject = resolveContactSubject(req.getSubject(), req.getFullName());
            Map<String, String> placeholders = buildContactPlaceholders(req);

            if (req.getFileBytes() != null) {
                byte[] rawMime = buildRawMimeMessage(emailSubject, req.getEmail(),
                        EmailTemplateLoader.load("contact-email.txt", placeholders),
                        EmailTemplateLoader.load("contact-email.html", placeholders),
                        req.getFileBytes(), req.getAttachmentFileName());
                sesClient.sendRawEmail(SendRawEmailRequest.builder()
                        .rawMessage(RawMessage.builder()
                                .data(SdkBytes.fromByteArray(rawMime))
                                .build())
                        .build());
            } else {
                sendSimpleEmail(emailSubject, req.getEmail(),
                        EmailTemplateLoader.load("contact-email.txt", placeholders),
                        EmailTemplateLoader.load("contact-email.html", placeholders));
            }
            log.info("Contact email sent successfully to {} on behalf of {}", destinationEmail, req.getEmail());
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to send contact email for sender: {}", req.getEmail(), e);
            throw new CustomException(ErrorCode.EMAIL_SEND_FAILURE, 500,
                    "Failed to send email via SES: " + e.getMessage(), e);
        }
    }

    /**
     * Sends a job-application notification email via Amazon SES with the CV attached directly.
     *
     * @throws CustomException with {@link ErrorCode#EMAIL_SEND_FAILURE} (HTTP 500) if sending fails
     */
    public void sendJobApplicationEmail(ContactRequest req) {
        log.info("Sending job-application email via SES on behalf of: {}", req.getEmail());
        try {
            String emailSubject = Constants.JOB_APPLICATION_EMAIL_SUBJECT_PREFIX + req.getFullName();
            Map<String, String> placeholders = buildJobApplicationPlaceholders(req);

            byte[] rawMime = buildRawMimeMessage(emailSubject, req.getEmail(),
                    EmailTemplateLoader.load("job-application-email.txt", placeholders),
                    EmailTemplateLoader.load("job-application-email.html", placeholders),
                    req.getFileBytes(), req.getAttachmentFileName());

            sesClient.sendRawEmail(SendRawEmailRequest.builder()
                    .rawMessage(RawMessage.builder()
                            .data(SdkBytes.fromByteArray(rawMime))
                            .build())
                    .build());

            log.info("Job-application email sent successfully to {} on behalf of {}",
                    destinationEmail, req.getEmail());
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to send job-application email for sender: {}", req.getEmail(), e);
            throw new CustomException(ErrorCode.EMAIL_SEND_FAILURE, 500,
                    "Failed to send email via SES: " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers – simple email (no attachment)
    // -------------------------------------------------------------------------

    private void sendSimpleEmail(String subject, String replyTo, String textBody, String htmlBody) {
        Content subjectContent = Content.builder().data(subject).charset("UTF-8").build();
        Content textContent = Content.builder().data(textBody).charset("UTF-8").build();
        Content htmlContent = Content.builder().data(htmlBody).charset("UTF-8").build();

        Body body = Body.builder().text(textContent).html(htmlContent).build();
        software.amazon.awssdk.services.ses.model.Message message =
                software.amazon.awssdk.services.ses.model.Message.builder()
                        .subject(subjectContent).body(body).build();

        SendEmailRequest emailRequest = SendEmailRequest.builder()
                .source(fromEmail)
                .destination(Destination.builder().toAddresses(destinationEmail).build())
                .replyToAddresses(replyTo)
                .message(message)
                .build();

        sesClient.sendEmail(emailRequest);
    }

    // -------------------------------------------------------------------------
    // Private helpers – raw MIME email with attachment
    // -------------------------------------------------------------------------

    private byte[] buildRawMimeMessage(String subject, String replyTo,
                                        String textBody, String htmlBody,
                                        byte[] attachmentBytes, String attachmentName)
            throws MessagingException, IOException {

        Session session = Session.getInstance(new Properties());
        MimeMessage mimeMessage = new MimeMessage(session);
        mimeMessage.setFrom(new InternetAddress(fromEmail));
        mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(destinationEmail));
        mimeMessage.setReplyTo(new InternetAddress[]{new InternetAddress(replyTo)});
        mimeMessage.setSubject(subject, "UTF-8");

        // Outer multipart/mixed container
        MimeMultipart mixed = new MimeMultipart("mixed");

        // Inner multipart/alternative for text + HTML bodies
        MimeBodyPart bodyPart = new MimeBodyPart();
        MimeMultipart alternative = new MimeMultipart("alternative");

        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setContent(textBody, Constants.CONTENT_TYPE_TEXT_PLAIN);

        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(htmlBody, Constants.CONTENT_TYPE_TEXT_HTML);

        alternative.addBodyPart(textPart);
        alternative.addBodyPart(htmlPart);
        bodyPart.setContent(alternative);
        mixed.addBodyPart(bodyPart);

        // Attachment part
        String lowerName = attachmentName != null ? attachmentName.toLowerCase() : "";
        String attachmentMime;
        if (lowerName.endsWith(".pdf")) {
            attachmentMime = Constants.CONTENT_TYPE_PDF;
        } else if (lowerName.endsWith(".docx")) {
            attachmentMime = Constants.CONTENT_TYPE_DOCX;
        } else {
            attachmentMime = "application/octet-stream";
        }
        MimeBodyPart attachmentPart = new MimeBodyPart();
        attachmentPart.setDataHandler(
                new DataHandler(new ByteArrayDataSource(attachmentBytes, attachmentMime)));
        attachmentPart.setFileName(attachmentName);
        mixed.addBodyPart(attachmentPart);

        mimeMessage.setContent(mixed);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        mimeMessage.writeTo(out);
        return out.toByteArray();
    }

    private Map<String, String> buildCommonPlaceholders(ContactRequest req) {
        Map<String, String> map = new HashMap<>();
        map.put("{{NAME}}", htmlEscape(req.getFullName()));
        map.put("{{PHONE}}", htmlEscape(req.getPhone()));
        map.put("{{EMAIL}}", htmlEscape(req.getEmail()));
        map.put("{{CITY}}", htmlEscape(req.getCity() != null ? req.getCity() : ""));
        map.put("{{SUBJECT}}", htmlEscape(req.getSubject() != null ? req.getSubject() : ""));
        map.put("{{MESSAGE}}", htmlEscape(req.getMessage()).replace("\n", "<br/>"));
        return map;
    }

    // -------------------------------------------------------------------------
    // Private helpers – placeholder builders
    // -------------------------------------------------------------------------

    /**
     * Builds the template placeholder map for contact-form emails.
     * All user-supplied values are HTML-escaped before use.
     */
    private Map<String, String> buildContactPlaceholders(ContactRequest req) {
        Map<String, String> map = buildCommonPlaceholders(req);
        map.put("{{COMPANY}}", htmlEscape(req.getCompany() != null ? req.getCompany() : ""));
        return map;
    }

    /**
     * Builds the template placeholder map for job-application emails.
     * All user-supplied values are HTML-escaped before use.
     */
    private Map<String, String> buildJobApplicationPlaceholders(ContactRequest req) {
        Map<String, String> map = buildCommonPlaceholders(req);
        map.put("{{CITY}}", htmlEscape(req.getCity() != null ? req.getCity() : ""));
        map.put("{{ATTACHMENT_NAME}}", htmlEscape(req.getAttachmentFileName() != null ? req.getAttachmentFileName() : ""));
        return map;
    }

    // -------------------------------------------------------------------------
    // Private helpers – shared
    // -------------------------------------------------------------------------

    /**
     * Resolves the email subject: uses the caller-supplied {@code subject} when non-blank,
     * otherwise falls back to the default prefix plus the sender's full name.
     *
     * @param subject  optional subject field from the request
     * @param fullName the sender's full name
     * @return the resolved email subject string
     */
    private static String resolveContactSubject(String subject, String fullName) {
        return (subject != null && !subject.isBlank())
                ? subject
                : Constants.CONTACT_EMAIL_SUBJECT_PREFIX + fullName;
    }

    /**
     * Escapes HTML special characters to prevent injection in the email body.
     *
     * @param input raw input string
     * @return HTML-escaped string, or an empty string if {@code input} is {@code null}
     */
    private static String htmlEscape(String input) {
        if (input == null) {
            return "";
        }
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }

    /**
     * Reads a required environment variable or throws {@link CustomException}.
     *
     * @param name the environment variable name
     * @return the value of the environment variable
     * @throws CustomException if the variable is not set or blank
     */
    private static String requireEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new CustomException(ErrorCode.INTERNAL_ERROR, 500,
                    "Missing required environment variable: " + name);
        }
        return value;
    }
}
