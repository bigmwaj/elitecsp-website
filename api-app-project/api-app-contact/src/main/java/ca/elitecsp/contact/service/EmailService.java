package ca.elitecsp.contact.service;

import ca.elitecsp.contact.model.ContactRequest;

/**
 * Contract for all transactional email operations.
 *
 * <p>Two categories of emails are distinguished:
 * <ul>
 *   <li><b>Notification emails</b> – sent to the company's destination address to inform
 *       the team of a new contact request or job application.</li>
 *   <li><b>Confirmation emails</b> – sent to the submitting user's own address to
 *       acknowledge receipt and set expectations for follow-up.</li>
 * </ul>
 *
 * <p>The primary production implementation is {@link SesEmailService}.
 */
public interface EmailService {

    /**
     * Sends a contact-form notification email to the configured destination address.
     *
     * @param req the validated contact request
     */
    void sendContactEmail(ContactRequest req);

    /**
     * Sends a job-application notification email (with CV attachment) to the configured
     * destination address.
     *
     * @param req the validated job-application request
     */
    void sendJobApplicationEmail(ContactRequest req);

    /**
     * Sends a confirmation email to the user who submitted the contact form,
     * acknowledging receipt and indicating that the team will respond soon.
     *
     * <p>Implementations must never throw checked exceptions; any failure should be
     * logged and swallowed so that the API response is not affected.
     *
     * @param req the validated contact request
     */
    void sendContactConfirmationEmail(ContactRequest req);

    /**
     * Sends a confirmation email to the applicant who submitted a job application,
     * acknowledging receipt and describing the next steps.
     *
     * <p>Implementations must never throw checked exceptions; any failure should be
     * logged and swallowed so that the API response is not affected.
     *
     * @param req the validated job-application request
     */
    void sendJobApplicationConfirmationEmail(ContactRequest req);
}
