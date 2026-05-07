package ca.elitecsp.cms.service;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.nio.charset.StandardCharsets;

/**
 * Service for downloading objects from Amazon S3.
 *
 * <p>Required environment variable:
 * <ul>
 *   <li>{@code AWS_REGION} – the AWS region where the bucket resides (e.g. {@code ca-central-1}).</li>
 * </ul>
 *
 * <p>AWS credentials are resolved automatically by the SDK's default credential chain
 * (Lambda execution role → environment variables → instance profile). No secrets are
 * hardcoded.
 */
@Slf4j
public class S3Service {

    private static final String ENV_AWS_REGION = "AWS_REGION";

    private final S3Client s3Client;

    /**
     * Default constructor used by the Lambda runtime.
     * Reads {@code AWS_REGION} from the environment.
     */
    public S3Service() {
        String region = requireEnv(ENV_AWS_REGION);
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .build();
    }

    /** Package-private constructor for dependency injection in tests. */
    S3Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    /**
     * Downloads the content of an S3 object as a UTF-8 string.
     *
     * @param bucketName the S3 bucket name
     * @param fileKey    the S3 object key (path)
     * @return the object content as a string
     * @throws S3DownloadException if the object does not exist or the download fails
     */
    public String downloadAsString(String bucketName, String fileKey) {
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
