package ca.elitecsp.cms.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Root wrapper element for an XML jobs document.
 *
 * <p>Expected XML format:
 * <pre>{@code
 * <jobs>
 *   <job>…</job>
 *   <job>…</job>
 * </jobs>
 * }</pre>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JacksonXmlRootElement(localName = "jobs")
public class JobListXml {

    @JacksonXmlProperty(localName = "job")
    @JacksonXmlElementWrapper(useWrapping = false)
    @JsonIgnoreProperties(ignoreUnknown = true)
    private List<JobDto> jobs;
}
