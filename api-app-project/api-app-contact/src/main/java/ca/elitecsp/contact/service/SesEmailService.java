package ca.elitecsp.contact.service;

import ca.elitecsp.common.exception.ApiException;
import ca.elitecsp.common.exception.ErrorCode;
import ca.elitecsp.common.util.Constants;
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
import java.util.Properties;

/**
 * Amazon SES implementation of {@link EmailService}.
 *
 * <p>Uses the AWS SDK v2 {@link SesClient} to send transactional emails. All email
 * addresses and AWS configuration are read from environment variables so that no
 * sensitive data is hard-coded.
 *
 * <p>Required environment variables:
 * <ul>
 *   <li>{@code FROM_EMAIL} – SES-verified sender address</li>
 *   <li>{@code DESTINATION_EMAIL} – company recipient address for notification emails</li>
 *   <li>{@code AWS_REGION} – AWS region where SES is configured (e.g. {@code ca-central-1})</li>
 * </ul>
 */
@Slf4j
public class SesEmailService implements EmailService {

    private static final String ENV_FROM_EMAIL = "FROM_EMAIL";
    private static final String ENV_DESTINATION_EMAIL = "DESTINATION_EMAIL";
    private static final String ENV_AWS_REGION = "AWS_REGION";

    /** Verified SES sender (From) address. */
    private final String fromEmail;

    /** Recipient address for all notification emails (sent to the company team). */
    private final String destinationEmail;

    /** SES client initialized with the configured AWS region. */
    private final SesClient sesClient;

    /** Template service used to render HTML and plain-text email bodies. */
    private final EmailTemplateService emailTemplateService;

    /** Production constructor – reads configuration from environment variables. */
    public SesEmailService() {
        this.fromEmail = requireEnv(ENV_FROM_EMAIL);
        this.destinationEmail = requireEnv(ENV_DESTINATION_EMAIL);
        String awsRegion = requireEnv(ENV_AWS_REGION);
        this.sesClient = SesClient.builder()
                .region(Region.of(awsRegion))
                .build();
        this.emailTemplateService = new EmailTemplateService();
    }

    /**
     * Package-private constructor for dependency injection in tests.
     *
     * @param fromEmail            verified SES sender address
     * @param destinationEmail     company recipient email address
     * @param sesClient            pre-configured SES client mock
     * @param emailTemplateService template-rendering service
     */
    SesEmailService(String fromEmail, String destinationEmail,
                    SesClient sesClient, EmailTemplateService emailTemplateService) {
        this.fromEmail = fromEmail;
        this.destinationEmail = destinationEmail;
        this.sesClient = sesClient;
        this.emailTemplateService = emailTemplateService;
    }

    // -------------------------------------------------------------------------
    // EmailService – notification emails (sent to company)
    // -------------------------------------------------------------------------

    @Override
    public void sendContactEmail(ContactRequest req) {
        log.info("Sending contact notification email via SES on behalf of: {}", req.getEmail());
        try {
            String subject = resolveContactSubject(req.getSubject(), req.getFullName());
            String htmlBody = emailTemplateService.buildContactEmailHtml(req);
            String textBody = emailTemplateService.buildContactEmailText(req);

            if (req.getFileBytes() != null) {
                byte[] rawMime = buildRawMimeMessage(subject, req.getEmail(),
                        textBody, htmlBody, req.getFileBytes(), req.getAttachmentFileName());
                sesClient.sendRawEmail(SendRawEmailRequest.builder()
                        .rawMessage(RawMessage.builder()
                                .data(SdkBytes.fromByteArray(rawMime))
                                .build())
                        .build());
            } else {
                sendSimpleEmail(subject, req.getEmail(), textBody, htmlBody);
            }
            log.info("Contact notification email sent to {} on behalf of {}",
                    destinationEmail, req.getEmail());
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to send contact notification email for sender: {}", req.getEmail(), e);
            throw new ApiException(ErrorCode.EMAIL_SEND_FAILURE, 500,
                    "Failed to send email via SES: " + e.getMessage(), e);
        }
    }

    @Override
    public void sendJobApplicationEmail(ContactRequest req) {
        log.info("Sending job-application notification email via SES on behalf of: {}", req.getEmail());
        try {
            String subject = Constants.JOB_APPLICATION_EMAIL_SUBJECT_PREFIX + req.getFullName();
            String htmlBody = emailTemplateService.buildJobApplicationEmailHtml(req);
            String textBody = emailTemplateService.buildJobApplicationEmailText(req);

            byte[] rawMime = buildRawMimeMessage(subject, req.getEmail(),
                    textBody, htmlBody, req.getFileBytes(), req.getAttachmentFileName());
            sesClient.sendRawEmail(SendRawEmailRequest.builder()
                    .rawMessage(RawMessage.builder()
                            .data(SdkBytes.fromByteArray(rawMime))
                            .build())
                    .build());

            log.info("Job-application notification email sent to {} on behalf of {}",
                    destinationEmail, req.getEmail());
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to send job-application notification email for sender: {}",
                    req.getEmail(), e);
            throw new ApiException(ErrorCode.EMAIL_SEND_FAILURE, 500,
                    "Failed to send email via SES: " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------
    // EmailService – confirmation emails (sent to user)
    // -------------------------------------------------------------------------

    @Override
    public void sendContactConfirmationEmail(ContactRequest req) {
        log.info("Sending contact confirmation email to: {}", req.getEmail());
        try {
            String subject = Constants.CONTACT_CONFIRMATION_EMAIL_SUBJECT;
            String htmlBody = emailTemplateService.buildContactConfirmationHtml(req);
            String textBody = emailTemplateService.buildContactConfirmationText(req);
            sendConfirmationEmail(subject, req.getEmail(), textBody, htmlBody);
            log.info("Contact confirmation email sent to: {}", req.getEmail());
        } catch (Exception e) {
            log.warn("Failed to send contact confirmation email to {} – continuing without it: {}",
                    req.getEmail(), e.getMessage(), e);
        }
    }

    @Override
    public void sendJobApplicationConfirmationEmail(ContactRequest req) {
        log.info("Sending job-application confirmation email to: {}", req.getEmail());
        try {
            String positionOrFallback = (req.getSubject() != null && !req.getSubject().isBlank())
                    ? req.getSubject() : req.getFullName();
            String subject = Constants.JOB_APPLICATION_CONFIRMATION_EMAIL_SUBJECT_PREFIX + positionOrFallback;
            String htmlBody = emailTemplateService.buildJobApplicationConfirmationHtml(req);
            String textBody = emailTemplateService.buildJobApplicationConfirmationText(req);
            sendConfirmationEmail(subject, req.getEmail(), textBody, htmlBody);
            log.info("Job-application confirmation email sent to: {}", req.getEmail());
        } catch (Exception e) {
            log.warn("Failed to send job-application confirmation email to {} – continuing without it: {}",
                    req.getEmail(), e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers – simple email (no attachment)
    // -------------------------------------------------------------------------

    /**
     * Sends a notification email to the company destination address.
     * Reply-To is set to the submitter's email so the team can reply directly.
     */
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

    /**
     * Sends a confirmation email directly to the user's own address.
     */
    private void sendConfirmationEmail(String subject, String toEmail,
                                       String textBody, String htmlBody) {
        Content subjectContent = Content.builder().data(subject).charset("UTF-8").build();
        Content textContent = Content.builder().data(textBody).charset("UTF-8").build();
        Content htmlContent = Content.builder().data(htmlBody).charset("UTF-8").build();

        Body body = Body.builder().text(textContent).html(htmlContent).build();
        software.amazon.awssdk.services.ses.model.Message message =
                software.amazon.awssdk.services.ses.model.Message.builder()
                        .subject(subjectContent).body(body).build();

        SendEmailRequest emailRequest = SendEmailRequest.builder()
                .source(fromEmail)
                .destination(Destination.builder().toAddresses(toEmail).build())
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
        MimeBodyPart attachmentPart = buildAttachmentPart(attachmentBytes, attachmentName);
        mixed.addBodyPart(attachmentPart);

        mimeMessage.setContent(mixed);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        mimeMessage.writeTo(out);
        return out.toByteArray();
    }

    private static MimeBodyPart buildAttachmentPart(byte[] attachmentBytes, String attachmentName)
            throws MessagingException {
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
        return attachmentPart;
    }

    // -------------------------------------------------------------------------
    // Private helpers – shared
    // -------------------------------------------------------------------------

    /**
     * Resolves the email subject: uses the caller-supplied {@code subject} when non-blank,
     * otherwise falls back to the default prefix plus the sender's full name.
     */
    private static String resolveContactSubject(String subject, String fullName) {
        return (subject != null && !subject.isBlank())
                ? subject
                : Constants.CONTACT_EMAIL_SUBJECT_PREFIX + fullName;
    }

    /**
     * Reads a required environment variable or throws {@link ApiException}.
     *
     * @param name the environment variable name
     * @return the value of the environment variable
     * @throws ApiException if the variable is not set or blank
     */
    private static String requireEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new ApiException(ErrorCode.INTERNAL_ERROR, 500,
                    "Missing required environment variable: " + name);
        }
        return value;
    }
}
