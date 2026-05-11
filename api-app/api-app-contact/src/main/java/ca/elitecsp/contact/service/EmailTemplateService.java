package ca.elitecsp.contact.service;

import ca.elitecsp.common.util.EmailTemplateLoader;
import ca.elitecsp.contact.model.ContactRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Builds HTML and plain-text email bodies for all transactional email types.
 *
 * <p>This service is responsible solely for template rendering. It delegates loading
 * and placeholder substitution to {@link EmailTemplateLoader}, keeping template concerns
 * entirely separate from SES transport logic.
 *
 * <p>All user-supplied values are HTML-escaped before insertion into HTML templates to
 * prevent injection.
 */
public class EmailTemplateService {

    // -------------------------------------------------------------------------
    // Notification emails (sent to the company / destination address)
    // -------------------------------------------------------------------------

    /** Returns the HTML body for a contact-form notification email. */
    public String buildContactEmailHtml(ContactRequest req) {
        return EmailTemplateLoader.load("contact-email.html", buildContactPlaceholders(req));
    }

    /** Returns the plain-text body for a contact-form notification email. */
    public String buildContactEmailText(ContactRequest req) {
        return EmailTemplateLoader.load("contact-email.txt", buildContactPlaceholders(req));
    }

    /** Returns the HTML body for a job-application notification email. */
    public String buildJobApplicationEmailHtml(ContactRequest req) {
        return EmailTemplateLoader.load("job-application-email.html", buildJobApplicationPlaceholders(req));
    }

    /** Returns the plain-text body for a job-application notification email. */
    public String buildJobApplicationEmailText(ContactRequest req) {
        return EmailTemplateLoader.load("job-application-email.txt", buildJobApplicationPlaceholders(req));
    }

    // -------------------------------------------------------------------------
    // Confirmation emails (sent to the user / applicant)
    // -------------------------------------------------------------------------

    /** Returns the HTML body for a contact-form confirmation email sent to the user. */
    public String buildContactConfirmationHtml(ContactRequest req) {
        return EmailTemplateLoader.load("contact-confirmation.html", buildConfirmationPlaceholders(req));
    }

    /** Returns the plain-text body for a contact-form confirmation email sent to the user. */
    public String buildContactConfirmationText(ContactRequest req) {
        return EmailTemplateLoader.load("contact-confirmation.txt", buildConfirmationPlaceholders(req));
    }

    /** Returns the HTML body for a job-application confirmation email sent to the applicant. */
    public String buildJobApplicationConfirmationHtml(ContactRequest req) {
        return EmailTemplateLoader.load("job-application-confirmation.html",
                buildJobApplicationConfirmationPlaceholders(req));
    }

    /** Returns the plain-text body for a job-application confirmation email sent to the applicant. */
    public String buildJobApplicationConfirmationText(ContactRequest req) {
        return EmailTemplateLoader.load("job-application-confirmation.txt",
                buildJobApplicationConfirmationPlaceholders(req));
    }

    // -------------------------------------------------------------------------
    // Private helpers – placeholder builders
    // -------------------------------------------------------------------------

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

    private Map<String, String> buildContactPlaceholders(ContactRequest req) {
        Map<String, String> map = buildCommonPlaceholders(req);
        map.put("{{COMPANY}}", htmlEscape(req.getCompany() != null ? req.getCompany() : ""));
        return map;
    }

    private Map<String, String> buildJobApplicationPlaceholders(ContactRequest req) {
        Map<String, String> map = buildCommonPlaceholders(req);
        map.put("{{ATTACHMENT_NAME}}",
                htmlEscape(req.getAttachmentFileName() != null ? req.getAttachmentFileName() : ""));
        return map;
    }

    private Map<String, String> buildConfirmationPlaceholders(ContactRequest req) {
        Map<String, String> map = new HashMap<>();
        map.put("{{NAME}}", htmlEscape(req.getFullName()));
        map.put("{{COMPANY}}", htmlEscape(req.getCompany() != null ? req.getCompany() : ""));
        map.put("{{SUBJECT}}", htmlEscape(req.getSubject() != null ? req.getSubject() : ""));
        return map;
    }

    private Map<String, String> buildJobApplicationConfirmationPlaceholders(ContactRequest req) {
        Map<String, String> map = new HashMap<>();
        map.put("{{NAME}}", htmlEscape(req.getFullName()));
        map.put("{{SUBJECT}}", htmlEscape(req.getSubject() != null ? req.getSubject() : ""));
        return map;
    }

    // -------------------------------------------------------------------------
    // Private helpers – HTML escaping
    // -------------------------------------------------------------------------

    /**
     * Escapes HTML special characters to prevent injection in the email body.
     *
     * @param input raw input string
     * @return HTML-escaped string, or an empty string if {@code input} is {@code null}
     */
    static String htmlEscape(String input) {
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
}
