package ca.elitecsp.job.parser;

import ca.elitecsp.job.model.JobDetailsDto;
import ca.elitecsp.job.model.JobSummaryDto;
import ca.elitecsp.job.model.ParsedJobWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExcelJobParserServiceTest {

    private ExcelJobParserService parser;

    @BeforeEach
    void setUp() {
        parser = new ExcelJobParserService();
    }

    // -------------------------------------------------------------------------
    // Builder helpers
    // -------------------------------------------------------------------------

    /**
     * Creates a minimal valid workbook with both required sheets.
     * Returns its raw bytes so the parser can read it.
     */
    private byte[] buildWorkbook(WorkbookCustomizer customizer) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            customizer.customize(wb);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        }
    }

    @FunctionalInterface
    interface WorkbookCustomizer {
        void customize(Workbook wb);
    }

    /** Adds the job-summaries sheet with standard headers + one data row. */
    private void addSummarySheet(Workbook wb, String lang) {
        Sheet sheet = wb.createSheet("job-summaries");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("id");
        header.createCell(1).setCellValue("category");
        header.createCell(2).setCellValue("type");
        header.createCell(3).setCellValue("location");
        header.createCell(4).setCellValue("title_" + lang);     // translated column
        header.createCell(5).setCellValue("summary_" + lang);   // translated column
        header.createCell(6).setCellValue("posted_date");
        header.createCell(7).setCellValue("expiration_date");

        Row row = sheet.createRow(1);
        row.createCell(0).setCellValue("001");
        row.createCell(1).setCellValue("IT");
        row.createCell(2).setCellValue("Full-time");
        row.createCell(3).setCellValue("Montreal");
        row.createCell(4).setCellValue("Java Developer");
        row.createCell(5).setCellValue("Build APIs");
        row.createCell(6).setCellValue("2026-01-01");
        row.createCell(7).setCellValue("2026-12-31");
    }

    /** Adds the job-details sheet with standard headers + one data row.
     *  Note: only title/summary are translated; description, responsibilities,
     *  requirements, and benefits use non-suffixed column names. */
    private void addDetailsSheet(Workbook wb, String lang) {
        Sheet sheet = wb.createSheet("job-details");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("id");
        header.createCell(1).setCellValue("description");
        header.createCell(2).setCellValue("responsibilities");
        header.createCell(3).setCellValue("requirements");
        header.createCell(4).setCellValue("benefits");

        Row row = sheet.createRow(1);
        row.createCell(0).setCellValue("001");
        row.createCell(1).setCellValue("Great role");
        row.createCell(2).setCellValue("Design APIs|Write tests");
        row.createCell(3).setCellValue("Java,AWS");
        row.createCell(4).setCellValue("Health\nDental");
    }

    // -------------------------------------------------------------------------
    // Happy path
    // -------------------------------------------------------------------------

    @Test
    void parseWorkbook_returnsSummaries_forEnglish() throws Exception {
        byte[] bytes = buildWorkbook(wb -> {
            addSummarySheet(wb, "en");
            addDetailsSheet(wb, "en");
        });

        ParsedJobWorkbook result = parser.parseWorkbook("en", bytes);

        assertEquals(1, result.getJobs().size());
        JobSummaryDto job = result.getJobs().get(0);
        assertEquals("001", job.getJobId());
        assertEquals("Java Developer", job.getTitle());
        assertEquals("Montreal", job.getLocation());
    }

    @Test
    void parseWorkbook_populatesJobsById() throws Exception {
        byte[] bytes = buildWorkbook(wb -> {
            addSummarySheet(wb, "en");
            addDetailsSheet(wb, "en");
        });

        ParsedJobWorkbook result = parser.parseWorkbook("en", bytes);

        assertTrue(result.getJobsById().containsKey("001"));
    }

    @Test
    void parseWorkbook_parsesDetailsCorrectly() throws Exception {
        byte[] bytes = buildWorkbook(wb -> {
            addSummarySheet(wb, "en");
            addDetailsSheet(wb, "en");
        });

        ParsedJobWorkbook result = parser.parseWorkbook("en", bytes);

        JobDetailsDto details = result.getDetailsById().get("001");
        assertNotNull(details);
        assertEquals("Great role", details.getDescription());
        assertEquals(List.of("Design APIs", "Write tests"), details.getResponsibilities());
        assertEquals(List.of("Java", "AWS"), details.getRequirements());
        assertEquals(List.of("Health", "Dental"), details.getBenefits());
    }

    @Test
    void parseWorkbook_worksFrench() throws Exception {
        byte[] bytes = buildWorkbook(wb -> {
            addSummarySheet(wb, "fr");
            addDetailsSheet(wb, "fr");
        });

        ParsedJobWorkbook result = parser.parseWorkbook("fr", bytes);
        assertFalse(result.getJobs().isEmpty());
    }

    @Test
    void parseWorkbook_skipsRowsWithBlankJobId() throws Exception {
        byte[] bytes = buildWorkbook(wb -> {
            addSummarySheet(wb, "en");
            // Add a blank row to summary sheet
            Sheet summarySheet = wb.getSheet("job-summaries");
            Row blankRow = summarySheet.createRow(2);
            blankRow.createCell(0).setCellValue("");  // blank jobId
            addDetailsSheet(wb, "en");
        });

        ParsedJobWorkbook result = parser.parseWorkbook("en", bytes);
        assertEquals(1, result.getJobs().size()); // blank row skipped
    }

    // -------------------------------------------------------------------------
    // Error cases
    // -------------------------------------------------------------------------

    @Test
    void parseWorkbook_throws_whenBytesEmpty() {
        ExcelParsingException ex = assertThrows(ExcelParsingException.class,
                () -> parser.parseWorkbook("en", new byte[0]));
        assertTrue(ex.getMessage().contains("empty"));
    }

    @Test
    void parseWorkbook_throws_whenBytesNull() {
        assertThrows(ExcelParsingException.class,
                () -> parser.parseWorkbook("en", null));
    }

    @Test
    void parseWorkbook_throws_whenJobSummariesSheetMissing() throws Exception {
        byte[] bytes = buildWorkbook(wb -> {
            // Only add details sheet – no summaries sheet
            addDetailsSheet(wb, "en");
        });

        ExcelParsingException ex = assertThrows(ExcelParsingException.class,
                () -> parser.parseWorkbook("en", bytes));
        assertTrue(ex.getMessage().contains("Missing required sheet"));
    }

    @Test
    void parseWorkbook_throws_whenJobDetailsSheetMissing() throws Exception {
        byte[] bytes = buildWorkbook(wb -> {
            addSummarySheet(wb, "en");
            // No details sheet
        });

        ExcelParsingException ex = assertThrows(ExcelParsingException.class,
                () -> parser.parseWorkbook("en", bytes));
        assertTrue(ex.getMessage().contains("Missing required sheet"));
    }

    @Test
    void parseWorkbook_throws_forInvalidBytes() {
        byte[] garbage = "not an excel file".getBytes();
        assertThrows(ExcelParsingException.class,
                () -> parser.parseWorkbook("en", garbage));
    }
}
