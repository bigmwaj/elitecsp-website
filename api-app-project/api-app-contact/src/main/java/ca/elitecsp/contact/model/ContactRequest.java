package ca.elitecsp.contact.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContactRequest {

    /**
     * Contact type that determines the processing path.
     * Defaults to {@link ContactType#CONTACT} when omitted.
     */
    private ContactType type;

    @JsonAlias("name")
    private String fullName;

    private String phone;

    private String email;

    private String company;

    private String city;

    private String subject;

    private String message;

    /**
     * Optional Base64-encoded file to include with the submission.
     * A data-URI prefix such as {@code data:application/pdf;base64,} is
     * automatically stripped before processing.
     * Accepts {@code "attachmentFile"} as an alias for backward compatibility.
     * <p>Required when {@link #type} is {@link ContactType#JOB_APPLICATION}.
     */
    @JsonAlias("attachmentFile")
    private String attachment;

    @JsonIgnore
    private byte[] fileBytes;

    /**
     * Original filename for the attachment (e.g. {@code "resume.pdf"} or
     * {@code "cv.docx"}).  Required when {@link #attachment} is provided.
     */
    private String attachmentFileName;

    /**
     * Returns the effective contact type, defaulting to {@link ContactType#CONTACT}
     * when the {@link #type} field was omitted from the JSON payload.
     *
     * @return the non-null contact type to apply
     */
    public ContactType getEffectiveType() {
        return type != null ? type : ContactType.CONTACT;
    }
}

