package ca.elitecsp.cms.service;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.nio.charset.StandardCharsets;

@Slf4j
public class S3Service {

    private static final String ENV_AWS_REGION = "AWS_REGION";

    private static final String ENV_BUCKET_NAME = "AWS_BUCKET_NAME";

    private static final String ENV_FILE_KEY = "AWS_FILE_KEY";

    private final S3Client s3Client;

    private final String bucketName;

    private final String fileKey;

    public S3Service() {
        String region = requireEnv(ENV_AWS_REGION);
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .build();

        this.bucketName = requireEnv(ENV_BUCKET_NAME);
        this.fileKey = requireEnv(ENV_FILE_KEY);
    }

    /** Package-private constructor for dependency injection in tests. */
    S3Service(S3Client s3Client,  String bucketName, String fileKey) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
        this.fileKey = fileKey;
    }

    public String downloadJobFileAsString() {
        log.info("Downloading s3://{}/{}", bucketName, fileKey);
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build();
            ResponseBytes<GetObjectResponse> responseBytes = s3Client.getObjectAsBytes(request);
            String content = responseBytes.asString(StandardCharsets.UTF_8);
            log.info("Downloaded {} byte(s) from s3://{}/{}", content.length(), bucketName, fileKey);
            return content;
        } catch (NoSuchKeyException e) {
            log.warn("S3 object not found: s3://{}/{}", bucketName, fileKey);
            throw new S3DownloadException("S3 object not found: " + bucketName + "/" + fileKey, e);
        } catch (Exception e) {
            log.error("Failed to download s3://{}/{}: {}", bucketName, fileKey, e.getMessage(), e);
            throw new S3DownloadException("Failed to download S3 object: " + e.getMessage(), e);
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
