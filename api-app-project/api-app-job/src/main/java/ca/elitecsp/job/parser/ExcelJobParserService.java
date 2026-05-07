package ca.elitecsp.job.parser;

import ca.elitecsp.job.model.JobDetailsDto;
import ca.elitecsp.job.model.JobSummaryDto;
import ca.elitecsp.job.model.ParsedJobWorkbook;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;

import java.io.ByteArrayInputStream;
import java.time.ZoneId;
import java.util.*;

@Slf4j
public class ExcelJobParserService {

    private static final String JOBS_SHEET = "job-summaries";
    private static final String JOB_DETAILS_SHEET = "job-details";

    private static final String COL_JOB_ID = "id";
    private static final String COL_CATEGORY = "category";
    private static final String COL_TYPE = "type";
    private static final String COL_ICON = "category";
    private static final String COL_POSTED_DATE = "posted_date";
    private static final String COL_EXPIRATION_DATE = "expiration_date";
    private static final String COL_LOCATION = "location";
    private static final String COL_TITLE = "title";
    private static final String COL_SUMMARY = "summary";
    private static final String COL_DESCRIPTION = "description";
    private static final String COL_RESPONSIBILITIES = "responsibilities";
    private static final String COL_REQUIREMENTS = "requirements";
    private static final String COL_BENEFITS = "benefits";

    private static final String[] JOB_TRANSLATED_COLS = {
            COL_TITLE,
            COL_SUMMARY,
    };

    private final DataFormatter formatter = new DataFormatter(Locale.ENGLISH);

    public ParsedJobWorkbook parseWorkbook(String lang, byte[] fileBytes) {
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

            List<JobSummaryDto> summaries = parseJobSummaries(lang, jobsSheet);
            Map<String, JobDetailsDto> detailsById = parseJobDetails(lang, detailsSheet);

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

    private boolean isTranslatedColumn(String columnName) {
        return Arrays.asList(JOB_TRANSLATED_COLS).contains(columnName);
    }

    private int getColumnIndex(String lang, Map<String, Integer> headerIndex, String columnName) {
        Integer index;
        if (isTranslatedColumn(columnName)) {
            index = headerIndex.get(columnName + "_" + lang);
        } else {
            index = headerIndex.get(columnName);
        }
        if (index == null) {
            throw new ExcelParsingException("Missing required column '" + columnName + "' in sheet for lang: " + lang);
        }
        return index;
    }

    private List<JobSummaryDto> parseJobSummaries(String lang, Sheet sheet) {
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


            jobs.add(JobSummaryDto.builder()
                    .jobId(jobId)
                    .category(readCell(row, headerIndex.get(COL_CATEGORY)))
                    .type(readCell(row, headerIndex.get(COL_TYPE)))
                    .icon(readCell(row, headerIndex.get(COL_ICON)))
                    .title(readCell(row, getColumnIndex(lang, headerIndex, COL_TITLE)))
                    .location(readCell(row, headerIndex.get(COL_LOCATION)))
                    .summary(readCell(row, getColumnIndex(lang, headerIndex, COL_SUMMARY)))
                    .postedDate(readCell(row, headerIndex.get(COL_POSTED_DATE)))
                    .expirationDate(readCell(row, headerIndex.get(COL_EXPIRATION_DATE)))
                    .build());
        }
        return jobs;
    }

    private Map<String, JobDetailsDto> parseJobDetails(String lang, Sheet sheet) {
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

            result.put(jobId, JobDetailsDto.builder()
                    .jobId(jobId)
                    .description(readCell(row, getColumnIndex(lang, headerIndex, COL_DESCRIPTION)))
                    .responsibilities(splitList(readCell(row, getColumnIndex(lang, headerIndex, COL_RESPONSIBILITIES))))
                    .requirements(splitList(readCell(row, getColumnIndex(lang, headerIndex, COL_REQUIREMENTS))))
                    .benefits(splitList(readCell(row, getColumnIndex(lang, headerIndex, COL_BENEFITS))))
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

    private String readCell(Row row, Integer index) {
        if (row == null || index == null || index < 0) {
            return "";
        }
        Cell cell = row.getCell(index, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) {
            return "";
        }
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            try {
                return cell.getDateCellValue()
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                        .toString();
            } catch (Exception e) {
                log.warn("Failed to parse date cell at row={}, col={}; falling back to formatter",
                        row.getRowNum() + 1, index + 1, e);
            }
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
        return input.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_]", "");
    }
}
