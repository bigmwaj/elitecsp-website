package ca.elitecsp.job.service;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@Slf4j
public class S3FileLoader {

    private static final String ENV_AWS_REGION = "AWS_REGION";

    private final S3Client s3Client;

    public S3FileLoader() {
        this.s3Client = S3Client.builder()
                .region(Region.of(requireEnv(ENV_AWS_REGION)))
                .build();
    }

    S3FileLoader(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public byte[] loadFile(String bucketName, String fileKey) {
        try {
            log.info("Downloading job Excel from s3://{}/{}", bucketName, fileKey);
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build();
            ResponseBytes<GetObjectResponse> bytes = s3Client.getObjectAsBytes(request);
            return bytes.asByteArray();
        } catch (Exception e) {
            log.error("Failed to load s3://{}/{}: {}", bucketName, fileKey, e.getMessage(), e);
            throw new S3DownloadException("Failed to download job Excel file from S3", e);
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
