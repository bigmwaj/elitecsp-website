package ca.elitecsp.cms.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing a single job entry parsed from an XML document.
 *
 * <p>Expected XML format:
 * <pre>{@code
 * <job>
 *   <jobId>001</jobId>
 *   <title>Java Developer</title>
 *   <department>IT</department>
 *   <location>Montreal</location>
 * </job>
 * }</pre>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "job")
public class JobDto {

    @JacksonXmlProperty(localName = "jobId")
    private String jobId;

    @JacksonXmlProperty(localName = "title")
    private String title;

    @JacksonXmlProperty(localName = "department")
    private String department;

    @JacksonXmlProperty(localName = "location")
    private String location;
}
