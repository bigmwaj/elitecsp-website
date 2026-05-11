package ca.elitecsp.contact.service;

import ca.elitecsp.contact.model.ContactRequest;
import ca.elitecsp.contact.model.ContactType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmailTemplateServiceTest {

    private EmailTemplateService service;

    @BeforeEach
    void setUp() {
        service = new EmailTemplateService();
    }

    // -------------------------------------------------------------------------
    // Notification templates – contact form
    // -------------------------------------------------------------------------

    @Test
    void buildContactEmailHtml_containsName() {
        ContactRequest req = buildContactRequest();
        String html = service.buildContactEmailHtml(req);
        assertTrue(html.contains("Alice Smith"), "HTML should contain the sender's name");
    }

    @Test
    void buildContactEmailHtml_containsEmail() {
        ContactRequest req = buildContactRequest();
        String html = service.buildContactEmailHtml(req);
        assertTrue(html.contains("alice@example.com"), "HTML should contain the sender's email");
    }

    @Test
    void buildContactEmailHtml_containsCompany() {
        ContactRequest req = buildContactRequest();
        String html = service.buildContactEmailHtml(req);
        assertTrue(html.contains("Acme Corp"), "HTML should contain the company name");
    }

    @Test
    void buildContactEmailText_containsName() {
        ContactRequest req = buildContactRequest();
        String text = service.buildContactEmailText(req);
        assertTrue(text.contains("Alice Smith"), "Plain text should contain the sender's name");
    }

    @Test
    void buildContactEmailText_containsMessage() {
        ContactRequest req = buildContactRequest();
        String text = service.buildContactEmailText(req);
        assertTrue(text.contains("Hello there"), "Plain text should contain the message body");
    }

    // -------------------------------------------------------------------------
    // Notification templates – job application
    // -------------------------------------------------------------------------

    @Test
    void buildJobApplicationEmailHtml_containsApplicantName() {
        ContactRequest req = buildJobApplicationRequest();
        String html = service.buildJobApplicationEmailHtml(req);
        assertTrue(html.contains("Bob Martin"), "HTML should contain the applicant's name");
    }

    @Test
    void buildJobApplicationEmailHtml_containsAttachmentName() {
        ContactRequest req = buildJobApplicationRequest();
        String html = service.buildJobApplicationEmailHtml(req);
        assertTrue(html.contains("bob-cv.pdf"), "HTML should contain the CV filename");
    }

    @Test
    void buildJobApplicationEmailText_containsApplicantEmail() {
        ContactRequest req = buildJobApplicationRequest();
        String text = service.buildJobApplicationEmailText(req);
        assertTrue(text.contains("bob@example.com"), "Plain text should contain the applicant's email");
    }

    // -------------------------------------------------------------------------
    // Confirmation templates – contact form
    // -------------------------------------------------------------------------

    @Test
    void buildContactConfirmationHtml_containsName() {
        ContactRequest req = buildContactRequest();
        String html = service.buildContactConfirmationHtml(req);
        assertTrue(html.contains("Alice Smith"), "Confirmation HTML should address the user by name");
    }

    @Test
    void buildContactConfirmationHtml_containsCompany() {
        ContactRequest req = buildContactRequest();
        String html = service.buildContactConfirmationHtml(req);
        assertTrue(html.contains("Acme Corp"), "Confirmation HTML should include company");
    }

    @Test
    void buildContactConfirmationHtml_doesNotContainUnresolvedPlaceholders() {
        ContactRequest req = buildContactRequest();
        String html = service.buildContactConfirmationHtml(req);
        assertFalse(html.contains("{{"), "All placeholders should be resolved in the HTML");
    }

    @Test
    void buildContactConfirmationText_containsName() {
        ContactRequest req = buildContactRequest();
        String text = service.buildContactConfirmationText(req);
        assertTrue(text.contains("Alice Smith"), "Confirmation text should address the user by name");
    }

    @Test
    void buildContactConfirmationText_doesNotContainUnresolvedPlaceholders() {
        ContactRequest req = buildContactRequest();
        String text = service.buildContactConfirmationText(req);
        assertFalse(text.contains("{{"), "All placeholders should be resolved in the text");
    }

    // -------------------------------------------------------------------------
    // Confirmation templates – job application
    // -------------------------------------------------------------------------

    @Test
    void buildJobApplicationConfirmationHtml_containsApplicantName() {
        ContactRequest req = buildJobApplicationRequest();
        String html = service.buildJobApplicationConfirmationHtml(req);
        assertTrue(html.contains("Bob Martin"),
                "Job confirmation HTML should address the applicant by name");
    }

    @Test
    void buildJobApplicationConfirmationHtml_containsPosition() {
        ContactRequest req = buildJobApplicationRequest();
        String html = service.buildJobApplicationConfirmationHtml(req);
        assertTrue(html.contains("Senior Developer"),
                "Job confirmation HTML should include the position title");
    }

    @Test
    void buildJobApplicationConfirmationHtml_doesNotContainUnresolvedPlaceholders() {
        ContactRequest req = buildJobApplicationRequest();
        String html = service.buildJobApplicationConfirmationHtml(req);
        assertFalse(html.contains("{{"), "All placeholders should be resolved in the HTML");
    }

    @Test
    void buildJobApplicationConfirmationText_containsApplicantName() {
        ContactRequest req = buildJobApplicationRequest();
        String text = service.buildJobApplicationConfirmationText(req);
        assertTrue(text.contains("Bob Martin"),
                "Job confirmation text should address the applicant by name");
    }

    @Test
    void buildJobApplicationConfirmationText_doesNotContainUnresolvedPlaceholders() {
        ContactRequest req = buildJobApplicationRequest();
        String text = service.buildJobApplicationConfirmationText(req);
        assertFalse(text.contains("{{"), "All placeholders should be resolved in the text");
    }

    // -------------------------------------------------------------------------
    // HTML escaping
    // -------------------------------------------------------------------------

    @Test
    void htmlEscape_escapesLtGtAmpQuotApos() {
        assertEquals("&lt;b&gt;Hi &amp; &quot;there&quot; &#x27;you&#x27;",
                EmailTemplateService.htmlEscape("<b>Hi & \"there\" 'you'"));
    }

    @Test
    void htmlEscape_returnsEmptyString_whenInputNull() {
        assertEquals("", EmailTemplateService.htmlEscape(null));
    }

    @Test
    void buildContactConfirmationHtml_escapesXssPayload() {
        ContactRequest req = buildContactRequest();
        req.setFullName("<script>alert('xss')</script>");
        String html = service.buildContactConfirmationHtml(req);
        assertFalse(html.contains("<script>"),
                "XSS payload must be escaped in the rendered HTML");
        assertTrue(html.contains("&lt;script&gt;"),
                "The escaped version of the payload should appear in the HTML");
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
        req.setMessage("Hello there");
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
        req.setAttachmentFileName("bob-cv.pdf");
        return req;
    }
}
