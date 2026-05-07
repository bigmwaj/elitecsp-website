package ca.elitecsp.job.service;

public class S3DownloadException extends RuntimeException {
    public S3DownloadException(String message, Throwable cause) {
        super(message, cause);
    }
}
