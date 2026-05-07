package ca.elitecsp.job.parser;

import ca.elitecsp.job.model.JobDetailsDto;
import ca.elitecsp.job.model.JobSummaryDto;
import ca.elitecsp.job.model.ParsedJobWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ExcelJobParserService")
class ExcelJobParserServiceTest {

    private final ExcelJobParserService parserService = new ExcelJobParserService();

    @Test
    @DisplayName("parses jobs and job-details sheets")
    void parseWorkbook_validWorkbook_returnsParsedData() {
        byte[] workbookBytes = buildWorkbook();

        ParsedJobWorkbook result = parserService.parseWorkbook(workbookBytes);

        assertEquals(2, result.getJobs().size());
        JobSummaryDto first = result.getJobs().getFirst();
        assertEquals("001", first.getJobId());
        assertEquals("Java Developer", first.getTitle());

        JobDetailsDto details = result.getDetailsById().get("001");
        assertNotNull(details);
        assertEquals(2, details.getResponsibilities().size());
        assertTrue(details.getRequirements().contains("AWS"));
    }

    @Test
    @DisplayName("throws for missing required sheet")
    void parseWorkbook_missingSheet_throws() {
        byte[] workbookBytes;
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            XSSFSheet jobsSheet = workbook.createSheet("jobs");
            Row header = jobsSheet.createRow(0);
            header.createCell(0).setCellValue("jobId");
            workbook.write(output);
            workbookBytes = output.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        ExcelParsingException ex = assertThrows(ExcelParsingException.class,
                () -> parserService.parseWorkbook(workbookBytes));

        assertTrue(ex.getMessage().contains("job-details"));
    }

    @Test
    @DisplayName("ignores empty rows and rows with blank jobId")
    void parseWorkbook_emptyRowsAreIgnored() {
        byte[] workbookBytes;
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            XSSFSheet jobsSheet = workbook.createSheet("jobs");
            Row jobsHeader = jobsSheet.createRow(0);
            jobsHeader.createCell(0).setCellValue("jobId");
            jobsHeader.createCell(1).setCellValue("title");
            jobsSheet.createRow(1).createCell(1).setCellValue("Missing id");
            Row validRow = jobsSheet.createRow(2);
            validRow.createCell(0).setCellValue("123");
            validRow.createCell(1).setCellValue("Analyst");

            XSSFSheet detailsSheet = workbook.createSheet("job-details");
            Row detailsHeader = detailsSheet.createRow(0);
            detailsHeader.createCell(0).setCellValue("jobId");
            detailsHeader.createCell(1).setCellValue("description");
            Row detailsRow = detailsSheet.createRow(1);
            detailsRow.createCell(0).setCellValue("123");
            detailsRow.createCell(1).setCellValue("Details");

            workbook.write(output);
            workbookBytes = output.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        ParsedJobWorkbook parsed = parserService.parseWorkbook(workbookBytes);
        assertEquals(1, parsed.getJobs().size());
        assertEquals("123", parsed.getJobs().getFirst().getJobId());
    }

    private byte[] buildWorkbook() {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            XSSFSheet jobsSheet = workbook.createSheet("jobs");
            Row jobsHeader = jobsSheet.createRow(0);
            jobsHeader.createCell(0).setCellValue("jobId");
            jobsHeader.createCell(1).setCellValue("title");
            jobsHeader.createCell(2).setCellValue("location");
            jobsHeader.createCell(3).setCellValue("department");
            jobsHeader.createCell(4).setCellValue("summary");
            jobsHeader.createCell(5).setCellValue("postedDate");

            Row row1 = jobsSheet.createRow(1);
            row1.createCell(0).setCellValue("001");
            row1.createCell(1).setCellValue("Java Developer");
            row1.createCell(2).setCellValue("Montreal");
            row1.createCell(3).setCellValue("Engineering");
            row1.createCell(4).setCellValue("Backend services");
            row1.createCell(5).setCellValue("2026-05-01");

            Row row2 = jobsSheet.createRow(2);
            row2.createCell(0).setCellValue("002");
            row2.createCell(1).setCellValue("Cloud Architect");
            row2.createCell(2).setCellValue("Toronto");
            row2.createCell(3).setCellValue("Platform");
            row2.createCell(4).setCellValue("Cloud strategy");
            row2.createCell(5).setCellValue("2026-05-02");

            XSSFSheet detailsSheet = workbook.createSheet("job-details");
            Row detailsHeader = detailsSheet.createRow(0);
            detailsHeader.createCell(0).setCellValue("jobId");
            detailsHeader.createCell(1).setCellValue("description");
            detailsHeader.createCell(2).setCellValue("responsibilities");
            detailsHeader.createCell(3).setCellValue("requirements");
            detailsHeader.createCell(4).setCellValue("benefits");

            Row details1 = detailsSheet.createRow(1);
            details1.createCell(0).setCellValue("001");
            details1.createCell(1).setCellValue("Detailed description");
            details1.createCell(2).setCellValue("Build APIs|Write tests");
            details1.createCell(3).setCellValue("Java;AWS");
            details1.createCell(4).setCellValue("Health,Dental");

            workbook.write(output);
            return output.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
