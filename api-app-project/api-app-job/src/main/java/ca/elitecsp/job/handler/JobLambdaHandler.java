package ca.elitecsp.job.handler;

import ca.elitecsp.common.exception.ApiException;
import ca.elitecsp.common.handler.CommonLambdaHandler;
import ca.elitecsp.common.response.ApiResponseBuilder;
import ca.elitecsp.job.model.JobDetailsDto;
import ca.elitecsp.job.model.JobParams;
import ca.elitecsp.job.model.JobSummaryDto;
import ca.elitecsp.job.service.JobService;
import ca.elitecsp.job.util.ValidationUtil;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class JobLambdaHandler extends CommonLambdaHandler {

    private static final String DEFAULT_LANG = "en";

    private static final String[] ACCEPTED_LANGS = {"fr", "en"};

    private final JobService jobService;

    private final ObjectMapper objectMapper;

    public JobLambdaHandler() {
        this(new JobService(), new ObjectMapper());
    }

    JobLambdaHandler(JobService jobService, ObjectMapper objectMapper) {
        this.jobService = jobService;
        this.objectMapper = objectMapper;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        try {
            JobParams params = parseRequest(request);
            ValidationUtil.validateContactRequest(params);
            String jobId = params.getJobId();
            String lang = params.getLang();

            if (jobId == null || jobId.isBlank()) {
                List<JobSummaryDto> jobs = jobService.getJobs(lang);
                return ApiResponseBuilder.success(objectMapper.writeValueAsString(jobs));
            }

            Optional<JobDetailsDto> details = jobService.getJobDetails(lang, jobId);
            if (details.isEmpty()) {
                return ApiResponseBuilder.notFoundError("Job not found for jobId: " + jobId, "JOB_NOT_FOUND");
            }
            return ApiResponseBuilder.success(objectMapper.writeValueAsString(details.get()));
        } catch (ApiException e) {
            log.error("API error in JobLambdaHandler: {}", e.getMessage(), e);
            return ApiResponseBuilder.fromApiException(e);
        } catch (IllegalArgumentException e) {
            return ApiResponseBuilder.internalError("Invalid request parameters: " + e.getMessage(), "INVALID_REQUEST");
        } catch (Exception e) {
            log.error("Unexpected error in JobLambdaHandler", e);
            return ApiResponseBuilder.internalError("An unexpected error occurred. Please try again later.", "INTERNAL_ERROR");
        }
    }

    private String extractParamFromRequest(APIGatewayProxyRequestEvent request, String paramName) {
        if (request == null) {
            return null;
        }

        Map<String, String> pathParameters = request.getPathParameters();
        if (pathParameters != null && pathParameters.containsKey(paramName)) {
            return pathParameters.get(paramName);
        }

        Map<String, String> queryParameters = request.getQueryStringParameters();
        if (queryParameters != null && queryParameters.containsKey(paramName)) {
            return queryParameters.get(paramName);
        }

        return null;
    }

    private JobParams parseRequest(APIGatewayProxyRequestEvent request) {
        JobParams jobParams = new JobParams();
        jobParams.setJobId(extractParamFromRequest(request, "jobId"));
        jobParams.setLang(extractParamFromRequest(request, "lang"));
        if (!Arrays.asList(ACCEPTED_LANGS).contains(jobParams.getLang())) {
            jobParams.setLang(DEFAULT_LANG);
        }

        return jobParams;
    }
}
