package ca.elitecsp.job.util;

import ca.elitecsp.common.exception.ApiException;
import ca.elitecsp.common.exception.ErrorCode;
import ca.elitecsp.job.model.JobParams;

public final class ValidationUtil {

    private ValidationUtil() {
        // Utility class – do not instantiate
    }

    public static void validateJobParams(JobParams params) {
        if (params == null) {
            throw new ApiException(ErrorCode.MISSING_REQUIRED_PARAM, 400,
                    "Request param must not be null");
        }
    }
}
