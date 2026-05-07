package ca.elitecsp.job.service;

import ca.elitecsp.job.model.JobDetailDto;
import ca.elitecsp.job.model.JobSummaryDto;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ExcelParserServiceTest {

    private ExcelParserService parser;

    @BeforeEach
    void setUp() {
        parser = new ExcelParserService();
    }

    @Test
    void parseJobs_validWorkbook_returnsSummaries() throws Exception {
        byte[] workbook = workbookBytesWithJobsAndDetails();

        List<JobSummaryDto> jobs = parser.parseJobs(workbook);

        assertEquals(2, jobs.size());
        assertEquals("001", jobs.get(0).getJobId());
        assertEquals("Java Developer", jobs.get(0).getTitle());
        assertEquals("Montreal", jobs.get(0).getLocation());
    }

    @Test
    void parseJobDetailById_validId_returnsDetail() throws Exception {
        byte[] workbook = workbookBytesWithJobsAndDetails();

        Optional<JobDetailDto> details = parser.parseJobDetailById(workbook, "001");

        assertTrue(details.isPresent());
        assertEquals("Java Developer", details.get().getTitle());
        assertEquals("Build APIs", details.get().getResponsibilities().get(0));
        assertEquals("Java 21", details.get().getRequirements().get(0));
    }

    @Test
    void parseJobDetailById_unknownId_returnsEmpty() throws Exception {
        byte[] workbook = workbookBytesWithJobsAndDetails();
        assertTrue(parser.parseJobDetailById(workbook, "999").isEmpty());
    }

    @Test
    void parseJobs_missingSheet_throwsJobDataException() throws Exception {
        byte[] bytes;
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            wb.createSheet("something-else");
            wb.write(out);
            bytes = out.toByteArray();
        }

        assertThrows(JobDataException.class, () -> parser.parseJobs(bytes));
    }

    @Test
    void parseJobs_emptyRows_areSkipped() throws Exception {
        byte[] bytes;
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet jobs = wb.createSheet("jobs");
            Row header = jobs.createRow(0);
            header.createCell(0).setCellValue("jobId");
            header.createCell(1).setCellValue("title");
            jobs.createRow(1);
            Row row = jobs.createRow(2);
            row.createCell(0).setCellValue("001");
            row.createCell(1).setCellValue("Java Developer");

            Sheet details = wb.createSheet("job-details");
            Row detailsHeader = details.createRow(0);
            detailsHeader.createCell(0).setCellValue("jobId");
            detailsHeader.createCell(1).setCellValue("description");

            wb.write(out);
            bytes = out.toByteArray();
        }

        List<JobSummaryDto> jobs = parser.parseJobs(bytes);
        assertEquals(1, jobs.size());
        assertEquals("001", jobs.get(0).getJobId());
    }

    private byte[] workbookBytesWithJobsAndDetails() throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet jobs = wb.createSheet("jobs");
            Row jobsHeader = jobs.createRow(0);
            jobsHeader.createCell(0).setCellValue("jobId");
            jobsHeader.createCell(1).setCellValue("title");
            jobsHeader.createCell(2).setCellValue("location");
            jobsHeader.createCell(3).setCellValue("department");
            jobsHeader.createCell(4).setCellValue("summary");
            jobsHeader.createCell(5).setCellValue("postedDate");

            Row j1 = jobs.createRow(1);
            j1.createCell(0).setCellValue("001");
            j1.createCell(1).setCellValue("Java Developer");
            j1.createCell(2).setCellValue("Montreal");
            j1.createCell(3).setCellValue("IT");
            j1.createCell(4).setCellValue("Build backend APIs");
            j1.createCell(5).setCellValue("2026-01-15");

            Row j2 = jobs.createRow(2);
            j2.createCell(0).setCellValue("002");
            j2.createCell(1).setCellValue("Cloud Architect");
            j2.createCell(2).setCellValue("Toronto");

            Sheet details = wb.createSheet("job-details");
            Row detailsHeader = details.createRow(0);
            detailsHeader.createCell(0).setCellValue("jobId");
            detailsHeader.createCell(1).setCellValue("description");
            detailsHeader.createCell(2).setCellValue("responsibilities");
            detailsHeader.createCell(3).setCellValue("requirements");
            detailsHeader.createCell(4).setCellValue("benefits");

            Row d1 = details.createRow(1);
            d1.createCell(0).setCellValue("001");
            d1.createCell(1).setCellValue("Design and implement services");
            d1.createCell(2).setCellValue("Build APIs;Write tests");
            d1.createCell(3).setCellValue("Java 21;AWS Lambda");
            d1.createCell(4).setCellValue("Health plan\nRemote work");

            wb.write(out);
            return out.toByteArray();
        }
    }
}
