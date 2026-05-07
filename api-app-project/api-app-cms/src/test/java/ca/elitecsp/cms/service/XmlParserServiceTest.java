package ca.elitecsp.cms.service;

import ca.elitecsp.cms.model.JobDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class XmlParserServiceTest {

    private XmlParserService service;

    @BeforeEach
    void setUp() {
        service = new XmlParserService();
    }

    @Test
    void parse_validXml_returnsJobs() {
        String xml = "<jobs>"
                + "<job><jobId>001</jobId><title>Java Developer</title><department>IT</department><location>Montreal</location></job>"
                + "<job><jobId>002</jobId><title>Cloud Architect</title><department>Engineering</department><location>Toronto</location></job>"
                + "</jobs>";

        List<JobDto> jobs = service.parse(xml);

        assertEquals(2, jobs.size());
        assertEquals("001", jobs.get(0).getJobId());
        assertEquals("Java Developer", jobs.get(0).getTitle());
        assertEquals("IT", jobs.get(0).getDepartment());
        assertEquals("Montreal", jobs.get(0).getLocation());
        assertEquals("002", jobs.get(1).getJobId());
    }

    @Test
    void parse_emptyXmlContent_returnsEmptyList() {
        List<JobDto> jobs = service.parse("");
        assertNotNull(jobs);
        assertTrue(jobs.isEmpty());
    }

    @Test
    void parse_nullXmlContent_returnsEmptyList() {
        List<JobDto> jobs = service.parse(null);
        assertNotNull(jobs);
        assertTrue(jobs.isEmpty());
    }

    @Test
    void parse_emptyJobsElement_returnsEmptyList() {
        String xml = "<jobs></jobs>";
        List<JobDto> jobs = service.parse(xml);
        assertNotNull(jobs);
        assertTrue(jobs.isEmpty());
    }

    @Test
    void parse_jobWithMissingFields_returnsDtoWithNulls() {
        String xml = "<jobs><job><jobId>003</jobId></job></jobs>";
        List<JobDto> jobs = service.parse(xml);
        assertEquals(1, jobs.size());
        assertEquals("003", jobs.get(0).getJobId());
        assertNull(jobs.get(0).getTitle());
    }

    @Test
    void parse_malformedXml_throwsXmlParsingException() {
        String xml = "<jobs><job><jobId>broken</jobs>";
        assertThrows(XmlParsingException.class, () -> service.parse(xml));
    }
}
