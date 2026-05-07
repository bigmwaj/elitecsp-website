package ca.elitecsp.job.service;

import ca.elitecsp.job.model.JobDetailsDto;
import ca.elitecsp.job.model.JobSummaryDto;
import ca.elitecsp.job.model.ParsedJobWorkbook;
import ca.elitecsp.job.parser.ExcelJobParserService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class JobService {

    private final S3FileLoaderService s3FileLoaderService;
    private final ExcelJobParserService excelJobParserService;

    public JobService() {
        this(new S3FileLoaderService(), new ExcelJobParserService());
    }

    JobService(S3FileLoaderService s3FileLoaderService, ExcelJobParserService excelJobParserService) {
        this.s3FileLoaderService = s3FileLoaderService;
        this.excelJobParserService = excelJobParserService;
    }

    public List<JobSummaryDto> getJobs() {
        ParsedJobWorkbook workbook = loadWorkbook();
        return workbook.getJobs();
    }

    public Optional<JobDetailsDto> getJobDetails(String jobId) {
        String normalizedId = normalizeJobId(jobId);
        ParsedJobWorkbook workbook = loadWorkbook();

        JobSummaryDto summary = workbook.getJobsById().get(normalizedId);
        JobDetailsDto details = workbook.getDetailsById().get(normalizedId);

        if (summary == null && details == null) {
            return Optional.empty();
        }

        return Optional.of(merge(summary, details, normalizedId));
    }

    private ParsedJobWorkbook loadWorkbook() {
        byte[] fileBytes = s3FileLoaderService.loadExcelFile();
        return excelJobParserService.parseWorkbook(fileBytes);
    }

    private JobDetailsDto merge(JobSummaryDto summary, JobDetailsDto details, String jobId) {
        JobDetailsDto response = details == null ? new JobDetailsDto() : details;
        response.setJobId(jobId);

        if (summary != null) {
            response.setTitle(summary.getTitle());
            response.setLocation(summary.getLocation());
            response.setDepartment(summary.getDepartment());
            response.setSummary(summary.getSummary());
            response.setPostedDate(summary.getPostedDate());

            Map<String, String> attributes = new LinkedHashMap<>(summary.getAttributes());
            if (response.getAttributes() != null) {
                attributes.putAll(response.getAttributes());
            }
            response.setAttributes(attributes);
        }

        return response;
    }

    private String normalizeJobId(String jobId) {
        if (jobId == null || jobId.isBlank()) {
            throw new IllegalArgumentException("jobId must not be blank");
        }
        return jobId.trim();
    }
}
