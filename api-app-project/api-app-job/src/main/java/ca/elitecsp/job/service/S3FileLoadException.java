package ca.elitecsp.job.service;

public class S3FileLoadException extends RuntimeException {

    public S3FileLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
