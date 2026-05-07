package ca.elitecsp.cms.service;

/**
 * Unchecked exception thrown when an S3 object cannot be downloaded.
 */
public class S3DownloadException extends RuntimeException {

    public S3DownloadException(String message, Throwable cause) {
        super(message, cause);
    }
}
