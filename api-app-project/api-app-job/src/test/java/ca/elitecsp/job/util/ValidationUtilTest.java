package ca.elitecsp.job.util;

import ca.elitecsp.common.exception.ApiException;
import ca.elitecsp.common.exception.ErrorCode;
import ca.elitecsp.job.model.JobParams;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilTest {

    @Test
    void validateJobParams_doesNotThrow_forValidParams() {
        JobParams params = new JobParams();
        params.setJobId("001");
        params.setLang("en");
        assertDoesNotThrow(() -> ValidationUtil.validateJobParams(params));
    }

    @Test
    void validateJobParams_doesNotThrow_whenJobIdIsNull() {
        JobParams params = new JobParams();
        params.setLang("fr");
        assertDoesNotThrow(() -> ValidationUtil.validateJobParams(params));
    }

    @Test
    void validateJobParams_throws_whenParamsIsNull() {
        ApiException ex = assertThrows(ApiException.class,
                () -> ValidationUtil.validateJobParams(null));
        assertEquals(ErrorCode.MISSING_REQUIRED_PARAM, ex.getErrorCode());
        assertEquals(400, ex.getHttpStatus());
    }
}
