package ca.elitecsp.job.service;

import ca.elitecsp.job.model.JobDetailDto;
import ca.elitecsp.job.model.JobSummaryDto;

import java.util.List;

public class JobService {

    private static final String ENV_JOB_EXCEL_BUCKET = "JOB_EXCEL_BUCKET";
    private static final String ENV_JOB_EXCEL_KEY = "JOB_EXCEL_KEY";

    private final S3FileLoader s3FileLoader;
    private final ExcelParserService excelParserService;

    public JobService() {
        this.s3FileLoader = new S3FileLoader();
        this.excelParserService = new ExcelParserService();
    }

    JobService(S3FileLoader s3FileLoader, ExcelParserService excelParserService) {
        this.s3FileLoader = s3FileLoader;
        this.excelParserService = excelParserService;
    }

    public List<JobSummaryDto> listJobs() {
        byte[] bytes = s3FileLoader.loadFile(requireEnv(ENV_JOB_EXCEL_BUCKET), requireEnv(ENV_JOB_EXCEL_KEY));
        return excelParserService.parseJobs(bytes);
    }

    public JobDetailDto getJobDetail(String jobId) {
        if (jobId == null || jobId.isBlank()) {
            throw new IllegalArgumentException("jobId must not be blank");
        }

        byte[] bytes = s3FileLoader.loadFile(requireEnv(ENV_JOB_EXCEL_BUCKET), requireEnv(ENV_JOB_EXCEL_KEY));
        return excelParserService.parseJobDetailById(bytes, jobId)
                .orElseThrow(() -> new JobNotFoundException("Job not found for jobId: " + jobId));
    }

    private static String requireEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required environment variable: " + name);
        }
        return value;
    }
}
