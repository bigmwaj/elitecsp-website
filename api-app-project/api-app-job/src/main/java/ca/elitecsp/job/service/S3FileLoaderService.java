package ca.elitecsp.job.service;

import ca.elitecsp.common.exception.ApiException;
import ca.elitecsp.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

@Slf4j
public class S3FileLoaderService {

    private static final String ENV_JOB_EXCEL_BUCKET = "JOB_EXCEL_BUCKET";
    private static final String ENV_JOB_EXCEL_KEY = "JOB_EXCEL_KEY";

    private final S3Client s3Client;
    private final String bucket;
    private final String key;

    public S3FileLoaderService() {
        this(S3Client.builder().build(), requireEnv(ENV_JOB_EXCEL_BUCKET), requireEnv(ENV_JOB_EXCEL_KEY));
    }

    S3FileLoaderService(S3Client s3Client, String bucket, String key) {
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.key = key;
    }

    public byte[] loadExcelFile() {
        try {
            log.info("Loading Excel file from s3://{}/{}", bucket, key);
            ResponseBytes<GetObjectResponse> response = s3Client.getObjectAsBytes(GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());
            return response.asByteArray();
        } catch (NoSuchKeyException e) {
            throw new ApiException(ErrorCode.INTERNAL_ERROR, 500, "Configured Excel file not found in S3: " + bucket + "/" + key, e);
        } catch (Exception e) {
            throw new ApiException(ErrorCode.INTERNAL_ERROR, 500, "Failed to load Excel file from S3: " + e.getMessage(), e);
        }
    }

    private static String requireEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required environment variable: " + name);
        }
        return value;
    }
}
