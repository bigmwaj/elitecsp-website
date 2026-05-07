package ca.elitecsp.job.parser;

import ca.elitecsp.job.model.JobDetailsDto;
import ca.elitecsp.job.model.JobSummaryDto;
import ca.elitecsp.job.model.ParsedJobWorkbook;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
public class ExcelJobParserService {

    private static final String JOBS_SHEET = "jobs";
    private static final String JOB_DETAILS_SHEET = "job-details";

    private static final String COL_JOB_ID = "jobid";
    private static final String COL_TITLE = "title";
    private static final String COL_LOCATION = "location";
    private static final String COL_DEPARTMENT = "department";
    private static final String COL_SUMMARY = "summary";
    private static final String COL_POSTED_DATE = "posteddate";
    private static final String COL_DESCRIPTION = "description";
    private static final String COL_RESPONSIBILITIES = "responsibilities";
    private static final String COL_REQUIREMENTS = "requirements";
    private static final String COL_BENEFITS = "benefits";

    private final DataFormatter formatter = new DataFormatter(Locale.ENGLISH);

    public ParsedJobWorkbook parseWorkbook(byte[] fileBytes) {
        if (fileBytes == null || fileBytes.length == 0) {
            throw new ExcelParsingException("Excel file content is empty");
        }

        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(fileBytes))) {
            Sheet jobsSheet = workbook.getSheet(JOBS_SHEET);
            Sheet detailsSheet = workbook.getSheet(JOB_DETAILS_SHEET);

            if (jobsSheet == null) {
                throw new ExcelParsingException("Missing required sheet: jobs");
            }
            if (detailsSheet == null) {
                throw new ExcelParsingException("Missing required sheet: job-details");
            }

            List<JobSummaryDto> summaries = parseJobSummaries(jobsSheet);
            Map<String, JobDetailsDto> detailsById = parseJobDetails(detailsSheet);

            Map<String, JobSummaryDto> jobsById = new LinkedHashMap<>();
            for (JobSummaryDto summary : summaries) {
                jobsById.put(summary.getJobId(), summary);
            }

            log.info("Parsed workbook: jobs={}, details={}", summaries.size(), detailsById.size());
            return ParsedJobWorkbook.builder()
                    .jobs(summaries)
                    .jobsById(jobsById)
                    .detailsById(detailsById)
                    .build();
        } catch (ExcelParsingException e) {
            throw e;
        } catch (Exception e) {
            throw new ExcelParsingException("Failed to parse Excel workbook: " + e.getMessage(), e);
        }
    }

    private List<JobSummaryDto> parseJobSummaries(Sheet sheet) {
        Map<String, Integer> headerIndex = readHeader(sheet);
        List<JobSummaryDto> jobs = new ArrayList<>();

        for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
            Row row = sheet.getRow(rowNum);
            if (isRowEmpty(row)) {
                continue;
            }

            String jobId = readCell(row, headerIndex.get(COL_JOB_ID));
            if (jobId.isBlank()) {
                log.warn("Skipping jobs row {} because jobId is blank", rowNum + 1);
                continue;
            }

            Map<String, String> attributes = parseAttributes(row, headerIndex, Arrays.asList(
                    COL_JOB_ID, COL_TITLE, COL_LOCATION, COL_DEPARTMENT, COL_SUMMARY, COL_POSTED_DATE));

            jobs.add(JobSummaryDto.builder()
                    .jobId(jobId)
                    .title(readCell(row, headerIndex.get(COL_TITLE)))
                    .location(readCell(row, headerIndex.get(COL_LOCATION)))
                    .department(readCell(row, headerIndex.get(COL_DEPARTMENT)))
                    .summary(readCell(row, headerIndex.get(COL_SUMMARY)))
                    .postedDate(readCell(row, headerIndex.get(COL_POSTED_DATE)))
                    .attributes(attributes)
                    .build());
        }
        return jobs;
    }

    private Map<String, JobDetailsDto> parseJobDetails(Sheet sheet) {
        Map<String, Integer> headerIndex = readHeader(sheet);
        Map<String, JobDetailsDto> result = new LinkedHashMap<>();

        for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
            Row row = sheet.getRow(rowNum);
            if (isRowEmpty(row)) {
                continue;
            }

            String jobId = readCell(row, headerIndex.get(COL_JOB_ID));
            if (jobId.isBlank()) {
                log.warn("Skipping job-details row {} because jobId is blank", rowNum + 1);
                continue;
            }

            Map<String, String> attributes = parseAttributes(row, headerIndex, Arrays.asList(
                    COL_JOB_ID, COL_DESCRIPTION, COL_RESPONSIBILITIES, COL_REQUIREMENTS, COL_BENEFITS));

            result.put(jobId, JobDetailsDto.builder()
                    .jobId(jobId)
                    .description(readCell(row, headerIndex.get(COL_DESCRIPTION)))
                    .responsibilities(splitList(readCell(row, headerIndex.get(COL_RESPONSIBILITIES))))
                    .requirements(splitList(readCell(row, headerIndex.get(COL_REQUIREMENTS))))
                    .benefits(splitList(readCell(row, headerIndex.get(COL_BENEFITS))))
                    .attributes(attributes)
                    .build());
        }

        return result;
    }

    private Map<String, Integer> readHeader(Sheet sheet) {
        Row header = sheet.getRow(0);
        if (header == null) {
            throw new ExcelParsingException("Sheet '" + sheet.getSheetName() + "' is missing a header row");
        }

        Map<String, Integer> index = new LinkedHashMap<>();
        for (int col = 0; col < header.getLastCellNum(); col++) {
            String raw = readCell(header, col);
            if (!raw.isBlank()) {
                index.put(normalizeColumn(raw), col);
            }
        }

        if (!index.containsKey(COL_JOB_ID)) {
            throw new ExcelParsingException("Sheet '" + sheet.getSheetName() + "' is missing required column: jobId");
        }

        return index;
    }

    private Map<String, String> parseAttributes(Row row,
                                                Map<String, Integer> headerIndex,
                                                List<String> excludedColumns) {
        Map<String, String> attributes = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : headerIndex.entrySet()) {
            if (excludedColumns.contains(entry.getKey())) {
                continue;
            }
            String value = readCell(row, entry.getValue());
            if (!value.isBlank()) {
                attributes.put(entry.getKey(), value);
            }
        }
        return attributes;
    }

    private String readCell(Row row, Integer index) {
        if (row == null || index == null || index < 0) {
            return "";
        }
        Cell cell = row.getCell(index, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) {
            return "";
        }
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toLocalDate().toString();
        }
        return formatter.formatCellValue(cell).trim();
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }
        for (int col = row.getFirstCellNum(); col < row.getLastCellNum(); col++) {
            if (!readCell(row, col).isBlank()) {
                return false;
            }
        }
        return true;
    }

    private List<String> splitList(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        return Arrays.stream(raw.split("\\r?\\n|\\||;|,"))
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .toList();
    }

    private String normalizeColumn(String input) {
        return input.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }
}
