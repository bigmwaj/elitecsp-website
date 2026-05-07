package ca.elitecsp.job.service;

public class JobDataException extends RuntimeException {
    public JobDataException(String message) {
        super(message);
    }

    public JobDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
