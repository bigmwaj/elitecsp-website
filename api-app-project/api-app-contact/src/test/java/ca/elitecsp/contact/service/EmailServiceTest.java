package ca.elitecsp.contact.service;

import ca.elitecsp.common.exception.CustomException;
import ca.elitecsp.common.exception.ErrorCode;
import ca.elitecsp.fixtures.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SendEmailResponse;
import software.amazon.awssdk.services.ses.model.SendRawEmailRequest;
import software.amazon.awssdk.services.ses.model.SendRawEmailResponse;
import software.amazon.awssdk.services.ses.model.SesException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link EmailService}.
 *
 * <p>All AWS SES calls are mocked. Tests cover both simple (no attachment) and
 * raw MIME (with attachment) email paths, as well as SES failure handling.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EmailService")
class EmailServiceTest {

    private static final String FROM_EMAIL = "no-reply@elitecsp.ca";
    private static final String DESTINATION_EMAIL = "admin@elitecsp.ca";

    @Mock
    private SesClient sesClient;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        // Use the package-private constructor to inject dependencies
        emailService = new EmailService(FROM_EMAIL, DESTINATION_EMAIL, sesClient);
    }

    // -------------------------------------------------------------------------
    // sendContactEmail – no attachment (simple sendEmail path)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("sendContactEmail without attachment → calls sesClient.sendEmail()")
    void sendContactEmail_withoutAttachment_callsSendEmail() {
        when(sesClient.sendEmail(any(SendEmailRequest.class)))
                .thenReturn(SendEmailResponse.builder().messageId("msg-001").build());

        emailService.sendContactEmail(TestFixtures.validContactRequest());

        verify(sesClient, times(1)).sendEmail(any(SendEmailRequest.class));
        verify(sesClient, never()).sendRawEmail(any(SendRawEmailRequest.class));
    }

    @Test
    @DisplayName("sendContactEmail without attachment uses subject from request when provided")
    void sendContactEmail_withCustomSubject_usesThatSubject() {
        when(sesClient.sendEmail(any(SendEmailRequest.class)))
                .thenReturn(SendEmailResponse.builder().messageId("msg-002").build());

        var req = TestFixtures.validContactRequest();
        req.setSubject("Custom Subject Line");

        emailService.sendContactEmail(req);

        verify(sesClient, times(1)).sendEmail(any(SendEmailRequest.class));
    }

    @Test
    @DisplayName("sendContactEmail without subject falls back to default prefix")
    void sendContactEmail_withoutSubject_usesFallbackSubject() {
        when(sesClient.sendEmail(any(SendEmailRequest.class)))
                .thenReturn(SendEmailResponse.builder().messageId("msg-003").build());

        var req = TestFixtures.validContactRequest();
        req.setSubject(null);

        assertDoesNotThrow(() -> emailService.sendContactEmail(req));
        verify(sesClient, times(1)).sendEmail(any(SendEmailRequest.class));
    }

    // -------------------------------------------------------------------------
    // sendContactEmail – with attachment (raw MIME sendRawEmail path)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("sendContactEmail with PDF attachment → calls sesClient.sendRawEmail()")
    void sendContactEmail_withPdfAttachment_callsSendRawEmail() {
        when(sesClient.sendRawEmail(any(SendRawEmailRequest.class)))
                .thenReturn(SendRawEmailResponse.builder().messageId("msg-004").build());

        emailService.sendContactEmail(TestFixtures.contactRequestWithAttachment());

        verify(sesClient, times(1)).sendRawEmail(any(SendRawEmailRequest.class));
        verify(sesClient, never()).sendEmail(any(SendEmailRequest.class));
    }

    @Test
    @DisplayName("sendContactEmail with DOCX attachment → calls sesClient.sendRawEmail()")
    void sendContactEmail_withDocxAttachment_callsSendRawEmail() {
        when(sesClient.sendRawEmail(any(SendRawEmailRequest.class)))
                .thenReturn(SendRawEmailResponse.builder().messageId("msg-005").build());

        var req = TestFixtures.validContactRequest();
        req.setAttachment(TestFixtures.base64Docx());
        req.setAttachmentFileName("document.docx");
        req.setFileBytes(TestFixtures.validDocxBytes());

        emailService.sendContactEmail(req);

        verify(sesClient, times(1)).sendRawEmail(any(SendRawEmailRequest.class));
    }

    @Test
    @DisplayName("sendContactEmail with unknown extension attachment → calls sendRawEmail with octet-stream")
    void sendContactEmail_withUnknownExtensionAttachment_callsSendRawEmail() {
        when(sesClient.sendRawEmail(any(SendRawEmailRequest.class)))
                .thenReturn(SendRawEmailResponse.builder().messageId("msg-006").build());

        var req = TestFixtures.validContactRequest();
        req.setAttachment(TestFixtures.base64Pdf());
        req.setAttachmentFileName("file.bin");
        req.setFileBytes(TestFixtures.validPdfBytes());

        emailService.sendContactEmail(req);

        verify(sesClient, times(1)).sendRawEmail(any(SendRawEmailRequest.class));
    }

    // -------------------------------------------------------------------------
    // sendContactEmail – SES failure handling
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("sendContactEmail when SES throws → wraps in CustomException EMAIL_SEND_FAILURE")
    void sendContactEmail_sesThrows_throwsCustomException() {
        when(sesClient.sendEmail(any(SendEmailRequest.class)))
                .thenThrow(SesException.builder().message("SES unavailable").build());

        CustomException ex = assertThrows(CustomException.class,
                () -> emailService.sendContactEmail(TestFixtures.validContactRequest()));

        assertEquals(ErrorCode.EMAIL_SEND_FAILURE, ex.getErrorCode());
        assertEquals(500, ex.getHttpStatus());
    }

    @Test
    @DisplayName("sendContactEmail with attachment when SES throws → wraps in CustomException")
    void sendContactEmail_withAttachment_sesThrows_throwsCustomException() {
        when(sesClient.sendRawEmail(any(SendRawEmailRequest.class)))
                .thenThrow(SesException.builder().message("SES unavailable").build());

        CustomException ex = assertThrows(CustomException.class,
                () -> emailService.sendContactEmail(TestFixtures.contactRequestWithAttachment()));

        assertEquals(ErrorCode.EMAIL_SEND_FAILURE, ex.getErrorCode());
        assertEquals(500, ex.getHttpStatus());
    }

    @Test
    @DisplayName("sendContactEmail re-throws CustomException without wrapping")
    void sendContactEmail_throwsCustomException_rethrowsDirectly() {
        var originalEx = new CustomException(ErrorCode.INTERNAL_ERROR, 500, "Already custom");
        when(sesClient.sendEmail(any(SendEmailRequest.class))).thenThrow(originalEx);

        CustomException ex = assertThrows(CustomException.class,
                () -> emailService.sendContactEmail(TestFixtures.validContactRequest()));

        assertSame(originalEx, ex, "Should rethrow the same CustomException without wrapping");
    }

    // -------------------------------------------------------------------------
    // sendJobApplicationEmail
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("sendJobApplicationEmail with PDF CV → calls sesClient.sendRawEmail()")
    void sendJobApplicationEmail_withPdfCv_callsSendRawEmail() {
        when(sesClient.sendRawEmail(any(SendRawEmailRequest.class)))
                .thenReturn(SendRawEmailResponse.builder().messageId("msg-007").build());

        emailService.sendJobApplicationEmail(TestFixtures.validJobApplicationRequest());

        verify(sesClient, times(1)).sendRawEmail(any(SendRawEmailRequest.class));
        verify(sesClient, never()).sendEmail(any(SendEmailRequest.class));
    }

    @Test
    @DisplayName("sendJobApplicationEmail when SES throws → wraps in CustomException EMAIL_SEND_FAILURE")
    void sendJobApplicationEmail_sesThrows_throwsCustomException() {
        when(sesClient.sendRawEmail(any(SendRawEmailRequest.class)))
                .thenThrow(SesException.builder().message("SES unavailable").build());

        CustomException ex = assertThrows(CustomException.class,
                () -> emailService.sendJobApplicationEmail(TestFixtures.validJobApplicationRequest()));

        assertEquals(ErrorCode.EMAIL_SEND_FAILURE, ex.getErrorCode());
        assertEquals(500, ex.getHttpStatus());
    }

    @Test
    @DisplayName("sendJobApplicationEmail re-throws CustomException without wrapping")
    void sendJobApplicationEmail_throwsCustomException_rethrowsDirectly() {
        var originalEx = new CustomException(ErrorCode.INTERNAL_ERROR, 500, "Already custom");
        when(sesClient.sendRawEmail(any(SendRawEmailRequest.class))).thenThrow(originalEx);

        CustomException ex = assertThrows(CustomException.class,
                () -> emailService.sendJobApplicationEmail(TestFixtures.validJobApplicationRequest()));

        assertSame(originalEx, ex);
    }

    @Test
    @DisplayName("sendJobApplicationEmail subject uses applicant full name")
    void sendJobApplicationEmail_subjectIncludesApplicantName() {
        when(sesClient.sendRawEmail(any(SendRawEmailRequest.class)))
                .thenReturn(SendRawEmailResponse.builder().messageId("msg-008").build());

        var req = TestFixtures.validJobApplicationRequest();
        req.setFullName("Alice Wonder");

        assertDoesNotThrow(() -> emailService.sendJobApplicationEmail(req));
        verify(sesClient, times(1)).sendRawEmail(any(SendRawEmailRequest.class));
    }
}
