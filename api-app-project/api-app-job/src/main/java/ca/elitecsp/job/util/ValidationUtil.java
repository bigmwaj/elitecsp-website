package ca.elitecsp.job.util;

import ca.elitecsp.common.exception.ApiException;
import ca.elitecsp.common.exception.ErrorCode;
import ca.elitecsp.common.util.ValidationUtils;
import ca.elitecsp.job.model.JobParams;

import java.util.Arrays;

public final class ValidationUtil {

    private static final String[] ACCEPTED_LANGS = {"fr", "en"};

    private ValidationUtil() {
        // Utility class – do not instantiate
    }

    public static void validateContactRequest(JobParams params) {
        if (params == null) {
            throw new ApiException(ErrorCode.MISSING_REQUIRED_PARAM, 400,
                    "Request param must not be null");
        }

        ValidationUtils.requireNonBlank(params.getLang(), "Lang");

        if(!Arrays.asList(ACCEPTED_LANGS).contains(params.getLang())) {
            throw new ApiException(ErrorCode.UNSUPPORTED_LANGUAGE, 400,
                    params.getLang() + " not supported");
        }


    }
}
