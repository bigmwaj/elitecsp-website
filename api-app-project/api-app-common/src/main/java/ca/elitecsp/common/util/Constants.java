package ca.elitecsp.common.util;

public final class Constants {

    // -------------------------------------------------------------------------
    // CV / file upload limits
    // -------------------------------------------------------------------------

    /** Maximum allowed CV file size in bytes (5 MB). */
    public static final int MAX_CV_SIZE_BYTES = 5 * 1024 * 1024;

    /**
     * PDF magic bytes ({@code %PDF}).
     * Used to verify that an uploaded file is genuinely a PDF document.
     */
    public static final byte[] PDF_MAGIC_BYTES = {0x25, 0x50, 0x44, 0x46};

    /**
     * DOCX magic bytes (ZIP PK signature: {@code PK\x03\x04}).
     * DOCX files are ZIP-based OOXML archives and share this signature with other ZIP
     * formats (e.g. XLSX, JAR).  This check is combined with an extension check in
     * {@link ca.elitecsp.common.util.ValidationUtils#requireAllowedFileType} to
     * reduce false positives.
     */
    public static final byte[] DOCX_MAGIC_BYTES = {0x50, 0x4B, 0x03, 0x04};

    // -------------------------------------------------------------------------
    // MIME / Content-Type values
    // -------------------------------------------------------------------------

    /** MIME type for JSON payloads. */
    public static final String CONTENT_TYPE_JSON = "application/json";

    /** MIME type for PDF documents. */
    public static final String CONTENT_TYPE_PDF = "application/pdf";

    /** MIME type for DOCX (Office Open XML word processing) documents. */
    public static final String CONTENT_TYPE_DOCX =
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

    // -------------------------------------------------------------------------
    // HTTP header names and values
    // -------------------------------------------------------------------------

    /** HTTP {@code Content-Type} header name. */
    public static final String HEADER_CONTENT_TYPE = "Content-Type";

    /** HTTP {@code Access-Control-Allow-Origin} header name. */
    public static final String HEADER_CORS_ORIGIN = "Access-Control-Allow-Origin";

    /** JVM system property used to override the allowed CORS origin. */
    public static final String PROP_CORS_ALLOW = "cors.allow";

    /** Environment variable used to override the allowed CORS origin. */
    public static final String ENV_CORS_ALLOW = "CORS_ALLOW";

    /** Default CORS origin used when no external configuration is provided. */
    public static final String DEFAULT_CORS_ALLOW = "http://localhost:4200";

    // -------------------------------------------------------------------------
    // Email / SES constants
    // -------------------------------------------------------------------------

    /** MIME type for plain-text email content. */
    public static final String CONTENT_TYPE_TEXT_PLAIN = "text/plain; charset=UTF-8";

    /** MIME type for HTML email content. */
    public static final String CONTENT_TYPE_TEXT_HTML = "text/html; charset=UTF-8";

    /** Subject prefix for contact-form emails sent via Amazon SES. */
    public static final String CONTACT_EMAIL_SUBJECT_PREFIX = "Elite CSP – Contact Form: ";

    /** Subject prefix for job-application emails sent via Amazon SES. */
    public static final String JOB_APPLICATION_EMAIL_SUBJECT_PREFIX = "Elite CSP – Job Application: ";

    /** Subject for contact-form confirmation emails sent to the user. */
    public static final String CONTACT_CONFIRMATION_EMAIL_SUBJECT = "Confirmation of your contact request – Elite CSP";

    /** Subject prefix for job-application confirmation emails sent to the applicant. */
    public static final String JOB_APPLICATION_CONFIRMATION_EMAIL_SUBJECT_PREFIX = "Application received – ";

    // -------------------------------------------------------------------------
    // Email format pattern
    // -------------------------------------------------------------------------

    /**
     * Regular expression for basic email address validation.
     * Requires at least one non-whitespace character before and after {@code @},
     * and a dot in the domain part.
     */
    public static final String EMAIL_REGEX = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";

    /**
     * Resolves the allowed CORS origin from external configuration.
     *
     * <p>Resolution order:
     * <ol>
     *   <li>JVM system property {@value #PROP_CORS_ALLOW}</li>
     *   <li>Environment variable {@value #ENV_CORS_ALLOW}</li>
     *   <li>{@link #DEFAULT_CORS_ALLOW}</li>
     * </ol>
     *
     * @return the configured CORS origin value
     */
    public static String getCorsAllow() {
        String propertyValue = System.getProperty(PROP_CORS_ALLOW);
        if (propertyValue != null && !propertyValue.isBlank()) {
            return propertyValue;
        }

        String envValue = System.getenv(ENV_CORS_ALLOW);
        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }

        return DEFAULT_CORS_ALLOW;
    }

    private Constants() {
        // Utility class – do not instantiate
    }
}
