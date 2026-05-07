package ca.elitecsp.job.service;

import ca.elitecsp.job.model.JobDetailDto;
import ca.elitecsp.job.model.JobSummaryDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class ExcelParserService {

    static final String JOBS_SHEET = "jobs";
    static final String JOB_DETAILS_SHEET = "job-details";

    private final DataFormatter dataFormatter = new DataFormatter(Locale.CANADA);

    public List<JobSummaryDto> parseJobs(byte[] fileBytes) {
        if (fileBytes == null || fileBytes.length == 0) {
            return Collections.emptyList();
        }

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(fileBytes))) {
            Sheet sheet = requireSheet(workbook, JOBS_SHEET);
            return parseJobRows(sheet);
        } catch (IOException e) {
            throw new JobDataException("Malformed Excel file", e);
        }
    }

    public Optional<JobDetailDto> parseJobDetailById(byte[] fileBytes, String jobId) {
        if (fileBytes == null || fileBytes.length == 0) {
            return Optional.empty();
        }

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(fileBytes))) {
            Sheet detailsSheet = requireSheet(workbook, JOB_DETAILS_SHEET);
            Map<String, JobSummaryDto> summariesById = parseJobsInternal(requireSheet(workbook, JOBS_SHEET));
            return parseDetailRows(detailsSheet, summariesById, jobId);
        } catch (IOException e) {
            throw new JobDataException("Malformed Excel file", e);
        }
    }

    private List<JobSummaryDto> parseJobRows(Sheet sheet) {
        return new ArrayList<>(parseJobsInternal(sheet).values());
    }

    private Map<String, JobSummaryDto> parseJobsInternal(Sheet sheet) {
        Map<String, Integer> headers = readHeaders(sheet);
        validateHeaders(headers, "jobs", List.of("jobid", "title"));

        Map<String, JobSummaryDto> jobs = new LinkedHashMap<>();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (isRowEmpty(row)) {
                continue;
            }
            String jobId = readCell(row, headers.get("jobid"));
            if (jobId == null || jobId.isBlank()) {
                continue;
            }

            JobSummaryDto dto = new JobSummaryDto(
                    jobId,
                    readCell(row, headers.get("title")),
                    readCell(row, headers.get("location")),
                    readCell(row, headers.get("department")),
                    readCell(row, headers.get("summary")),
                    readCell(row, headers.get("posteddate"))
            );
            jobs.put(jobId, dto);
        }
        return jobs;
    }

    private Optional<JobDetailDto> parseDetailRows(Sheet sheet, Map<String, JobSummaryDto> summariesById, String wantedJobId) {
        Map<String, Integer> headers = readHeaders(sheet);
        validateHeaders(headers, "job-details", List.of("jobid", "description"));

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (isRowEmpty(row)) {
                continue;
            }

            String jobId = readCell(row, headers.get("jobid"));
            if (jobId == null || jobId.isBlank() || !jobId.equals(wantedJobId)) {
                continue;
            }

            JobSummaryDto summary = summariesById.get(jobId);
            JobDetailDto details = new JobDetailDto();
            details.setJobId(jobId);
            details.setTitle(summary != null ? summary.getTitle() : null);
            details.setLocation(summary != null ? summary.getLocation() : null);
            details.setDepartment(summary != null ? summary.getDepartment() : null);
            details.setSummary(summary != null ? summary.getSummary() : null);
            details.setPostedDate(summary != null ? summary.getPostedDate() : null);
            details.setDescription(readCell(row, headers.get("description")));
            details.setResponsibilities(splitMultiValue(readCell(row, headers.get("responsibilities"))));
            details.setRequirements(splitMultiValue(readCell(row, headers.get("requirements"))));
            details.setBenefits(splitMultiValue(readCell(row, headers.get("benefits"))));
            return Optional.of(details);
        }

        return Optional.empty();
    }

    private Sheet requireSheet(Workbook workbook, String name) {
        Sheet sheet = workbook.getSheet(name);
        if (sheet == null) {
            throw new JobDataException("Missing required sheet: " + name);
        }
        return sheet;
    }

    private Map<String, Integer> readHeaders(Sheet sheet) {
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            throw new JobDataException("Missing header row in sheet: " + sheet.getSheetName());
        }

        Map<String, Integer> headers = new HashMap<>();
        for (Cell cell : headerRow) {
            String header = dataFormatter.formatCellValue(cell);
            if (header != null && !header.isBlank()) {
                headers.put(normalize(header), cell.getColumnIndex());
            }
        }
        return headers;
    }

    private void validateHeaders(Map<String, Integer> headers, String sheetName, List<String> requiredHeaders) {
        for (String required : requiredHeaders) {
            if (!headers.containsKey(required)) {
                throw new JobDataException("Missing required column '" + required + "' in sheet: " + sheetName);
            }
        }
    }

    private String readCell(Row row, Integer columnIndex) {
        if (row == null || columnIndex == null) {
            return null;
        }
        Cell cell = row.getCell(columnIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) {
            return null;
        }
        String value = dataFormatter.formatCellValue(cell);
        return value == null || value.isBlank() ? null : value.trim();
    }

    private List<String> splitMultiValue(String value) {
        if (value == null || value.isBlank()) {
            return Collections.emptyList();
        }

        String[] parts = value.split("\\r?\\n|;");
        List<String> items = new ArrayList<>();
        for (String part : parts) {
            if (part != null && !part.isBlank()) {
                items.add(part.trim());
            }
        }
        return items;
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }
        for (Cell cell : row) {
            if (cell != null && !dataFormatter.formatCellValue(cell).isBlank()) {
                return false;
            }
        }
        return true;
    }

    private String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT).replace("_", "").replace(" ", "");
    }
}
