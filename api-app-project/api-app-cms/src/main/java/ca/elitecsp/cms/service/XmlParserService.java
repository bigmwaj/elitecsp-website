package ca.elitecsp.cms.service;

import ca.elitecsp.cms.model.JobDto;
import ca.elitecsp.cms.model.JobListXml;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Service for parsing XML job data into a list of {@link JobDto} objects.
 *
 * <p>Expected XML structure:
 * <pre>{@code
 * <jobs>
 *   <job>
 *     <jobId>001</jobId>
 *     <title>Java Developer</title>
 *     <department>IT</department>
 *     <location>Montreal</location>
 *   </job>
 * </jobs>
 * }</pre>
 *
 * <p>Handles malformed XML, empty documents, and missing nodes gracefully
 * by returning an empty list and logging a warning.
 */
@Slf4j
public class XmlParserService {

    private final XmlMapper xmlMapper;

    /** Default constructor using a shared {@link XmlMapper} instance. */
    public XmlParserService() {
        this.xmlMapper = new XmlMapper();
    }

    /** Package-private constructor for testing with a custom mapper. */
    XmlParserService(XmlMapper xmlMapper) {
        this.xmlMapper = xmlMapper;
    }

    /**
     * Parses XML content and returns the list of jobs.
     *
     * @param xmlContent raw XML string; must not be {@code null}
     * @return a non-null, possibly empty list of {@link JobDto}
     */
    public List<JobDto> parse(String xmlContent) {
        if (xmlContent == null || xmlContent.isBlank()) {
            log.warn("XML content is null or blank; returning empty job list");
            return Collections.emptyList();
        }

        try {
            JobListXml jobList = xmlMapper.readValue(xmlContent.trim(), JobListXml.class);
            if (jobList == null || jobList.getJobs() == null) {
                log.warn("Parsed XML produced a null job list; returning empty list");
                return Collections.emptyList();
            }
            log.info("Parsed {} job(s) from XML", jobList.getJobs().size());
            return jobList.getJobs();
        } catch (IOException e) {
            log.error("Failed to parse XML job data: {}", e.getMessage(), e);
            throw new XmlParsingException("Failed to parse XML job data: " + e.getMessage(), e);
        }
    }
}
