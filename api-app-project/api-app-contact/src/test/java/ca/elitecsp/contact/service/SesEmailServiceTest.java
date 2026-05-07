package ca.elitecsp.contact.service;

import ca.elitecsp.common.exception.ApiException;
import ca.elitecsp.contact.model.ContactRequest;
import ca.elitecsp.contact.model.ContactType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SendRawEmailRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SesEmailServiceTest {

    @Mock
    private SesClient sesClient;

    private SesEmailService emailService;
    private EmailTemplateService templateService;

    private static final String FROM = "noreply@example.com";
    private static final String DEST  = "team@example.com";
    private static final byte[] PDF_HEADER_BYTES = {0x25, 0x50, 0x44, 0x46, 0x01};

    @BeforeEach
    void setUp() {
        templateService = new EmailTemplateService();
        emailService = new SesEmailService(FROM, DEST, sesClient, templateService);
    }

    // -------------------------------------------------------------------------
    // sendContactEmail – no attachment
    // -------------------------------------------------------------------------

    @Test
    void sendContactEmail_callsSendEmail_whenNoAttachment() {
        ContactRequest req = buildContactRequest();

        emailService.sendContactEmail(req);

        verify(sesClient, times(1)).sendEmail(any(SendEmailRequest.class));
        verify(sesClient, never()).sendRawEmail(any(SendRawEmailRequest.class));
    }

    @Test
    void sendContactEmail_setsCorrectDestinationAndSource_whenNoAttachment() {
        ContactRequest req = buildContactRequest();
        ArgumentCaptor<SendEmailRequest> captor = ArgumentCaptor.forClass(SendEmailRequest.class);

        emailService.sendContactEmail(req);

        verify(sesClient).sendEmail(captor.capture());
        SendEmailRequest sent = captor.getValue();
        assertEquals(FROM, sent.source());
        assertTrue(sent.destination().toAddresses().contains(DEST));
        assertTrue(sent.replyToAddresses().contains(req.getEmail()));
    }

    @Test
    void sendContactEmail_usesRequestSubject_whenSubjectIsPresent() {
        ContactRequest req = buildContactRequest();
        req.setSubject("Custom Subject");
        ArgumentCaptor<SendEmailRequest> captor = ArgumentCaptor.forClass(SendEmailRequest.class);

        emailService.sendContactEmail(req);

        verify(sesClient).sendEmail(captor.capture());
        assertEquals("Custom Subject", captor.getValue().message().subject().data());
    }

    @Test
    void sendContactEmail_usesFallbackSubject_whenSubjectIsBlank() {
        ContactRequest req = buildContactRequest();
        req.setSubject("");
        ArgumentCaptor<SendEmailRequest> captor = ArgumentCaptor.forClass(SendEmailRequest.class);

        emailService.sendContactEmail(req);

        verify(sesClient).sendEmail(captor.capture());
        String subject = captor.getValue().message().subject().data();
        assertTrue(subject.contains(req.getFullName()),
                "Subject should contain full name when subject field is blank");
    }

    // -------------------------------------------------------------------------
    // sendContactEmail – with attachment
    // -------------------------------------------------------------------------

    @Test
    void sendContactEmail_callsSendRawEmail_whenAttachmentPresent() {
        ContactRequest req = buildContactRequest();
        req.setFileBytes(PDF_HEADER_BYTES);
        req.setAttachmentFileName("resume.pdf");

        emailService.sendContactEmail(req);

        verify(sesClient, times(1)).sendRawEmail(any(SendRawEmailRequest.class));
        verify(sesClient, never()).sendEmail(any(SendEmailRequest.class));
    }

    // -------------------------------------------------------------------------
    // sendContactEmail – SES failure
    // -------------------------------------------------------------------------

    @Test
    void sendContactEmail_throwsApiException_whenSesFails() {
        doThrow(new RuntimeException("SES unavailable"))
                .when(sesClient).sendEmail(any(SendEmailRequest.class));
        ContactRequest req = buildContactRequest();

        ApiException ex = assertThrows(ApiException.class,
                () -> emailService.sendContactEmail(req));
        assertTrue(ex.getMessage().contains("SES"));
    }

    // -------------------------------------------------------------------------
    // sendJobApplicationEmail
    // -------------------------------------------------------------------------

    @Test
    void sendJobApplicationEmail_callsSendRawEmail() {
        ContactRequest req = buildJobApplicationRequest();

        emailService.sendJobApplicationEmail(req);

        verify(sesClient, times(1)).sendRawEmail(any(SendRawEmailRequest.class));
        verify(sesClient, never()).sendEmail(any(SendEmailRequest.class));
    }

    @Test
    void sendJobApplicationEmail_throwsApiException_whenSesFails() {
        doThrow(new RuntimeException("SES unavailable"))
                .when(sesClient).sendRawEmail(any(SendRawEmailRequest.class));
        ContactRequest req = buildJobApplicationRequest();

        ApiException ex = assertThrows(ApiException.class,
                () -> emailService.sendJobApplicationEmail(req));
        assertTrue(ex.getMessage().contains("SES"));
    }

    // -------------------------------------------------------------------------
    // sendContactConfirmationEmail
    // -------------------------------------------------------------------------

    @Test
    void sendContactConfirmationEmail_sendsToUserEmail() {
        ContactRequest req = buildContactRequest();
        ArgumentCaptor<SendEmailRequest> captor = ArgumentCaptor.forClass(SendEmailRequest.class);

        emailService.sendContactConfirmationEmail(req);

        verify(sesClient).sendEmail(captor.capture());
        SendEmailRequest sent = captor.getValue();
        assertTrue(sent.destination().toAddresses().contains(req.getEmail()),
                "Confirmation email must be sent to the user's own address");
        assertEquals(FROM, sent.source());
    }

    @Test
    void sendContactConfirmationEmail_subjectContainsConfirmation() {
        ContactRequest req = buildContactRequest();
        ArgumentCaptor<SendEmailRequest> captor = ArgumentCaptor.forClass(SendEmailRequest.class);

        emailService.sendContactConfirmationEmail(req);

        verify(sesClient).sendEmail(captor.capture());
        String subject = captor.getValue().message().subject().data();
        assertTrue(subject.toLowerCase().contains("confirm") || subject.toLowerCase().contains("received"),
                "Confirmation subject should reference confirmation or receipt");
    }

    @Test
    void sendContactConfirmationEmail_doesNotThrow_whenSesFails() {
        doThrow(new RuntimeException("SES unavailable"))
                .when(sesClient).sendEmail(any(SendEmailRequest.class));
        ContactRequest req = buildContactRequest();

        // Must not propagate the exception
        assertDoesNotThrow(() -> emailService.sendContactConfirmationEmail(req));
    }

    // -------------------------------------------------------------------------
    // sendJobApplicationConfirmationEmail
    // -------------------------------------------------------------------------

    @Test
    void sendJobApplicationConfirmationEmail_sendsToApplicantEmail() {
        ContactRequest req = buildJobApplicationRequest();
        ArgumentCaptor<SendEmailRequest> captor = ArgumentCaptor.forClass(SendEmailRequest.class);

        emailService.sendJobApplicationConfirmationEmail(req);

        verify(sesClient).sendEmail(captor.capture());
        SendEmailRequest sent = captor.getValue();
        assertTrue(sent.destination().toAddresses().contains(req.getEmail()),
                "Confirmation email must be sent to the applicant's own address");
        assertEquals(FROM, sent.source());
    }

    @Test
    void sendJobApplicationConfirmationEmail_subjectContainsPosition_whenSubjectProvided() {
        ContactRequest req = buildJobApplicationRequest();
        req.setSubject("Cloud Architect");
        ArgumentCaptor<SendEmailRequest> captor = ArgumentCaptor.forClass(SendEmailRequest.class);

        emailService.sendJobApplicationConfirmationEmail(req);

        verify(sesClient).sendEmail(captor.capture());
        String subject = captor.getValue().message().subject().data();
        assertTrue(subject.contains("Cloud Architect"),
                "Subject should contain the position when provided");
    }

    @Test
    void sendJobApplicationConfirmationEmail_fallsBackToName_whenSubjectBlank() {
        ContactRequest req = buildJobApplicationRequest();
        req.setSubject("");
        ArgumentCaptor<SendEmailRequest> captor = ArgumentCaptor.forClass(SendEmailRequest.class);

        emailService.sendJobApplicationConfirmationEmail(req);

        verify(sesClient).sendEmail(captor.capture());
        String subject = captor.getValue().message().subject().data();
        assertTrue(subject.contains(req.getFullName()),
                "Subject should fall back to full name when position is blank");
    }

    @Test
    void sendJobApplicationConfirmationEmail_doesNotThrow_whenSesFails() {
        doThrow(new RuntimeException("SES unavailable"))
                .when(sesClient).sendEmail(any(SendEmailRequest.class));
        ContactRequest req = buildJobApplicationRequest();

        assertDoesNotThrow(() -> emailService.sendJobApplicationConfirmationEmail(req));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static ContactRequest buildContactRequest() {
        ContactRequest req = new ContactRequest();
        req.setType(ContactType.CONTACT);
        req.setFullName("Alice Smith");
        req.setEmail("alice@example.com");
        req.setPhone("555-1234");
        req.setCity("Montreal");
        req.setCompany("Acme Corp");
        req.setSubject("General Inquiry");
        req.setMessage("Hello, I would like more information.");
        return req;
    }

    private static ContactRequest buildJobApplicationRequest() {
        ContactRequest req = new ContactRequest();
        req.setType(ContactType.JOB_APPLICATION);
        req.setFullName("Bob Martin");
        req.setEmail("bob@example.com");
        req.setPhone("555-5678");
        req.setCity("Toronto");
        req.setSubject("Senior Developer");
        req.setMessage("Please find my CV attached.");
        req.setFileBytes(PDF_HEADER_BYTES);
        req.setAttachmentFileName("bob-cv.pdf");
        return req;
    }
}
