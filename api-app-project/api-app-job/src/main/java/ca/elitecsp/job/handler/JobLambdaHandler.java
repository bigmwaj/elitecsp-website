package ca.elitecsp.job.handler;

import ca.elitecsp.job.model.JobDetailsDto;
import ca.elitecsp.job.model.JobSummaryDto;
import ca.elitecsp.job.parser.ExcelParsingException;
import ca.elitecsp.job.service.JobService;
import ca.elitecsp.job.service.S3FileLoadException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class JobLambdaHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final Map<String, String> DEFAULT_HEADERS = Map.of(
            "Content-Type", "application/json",
            "Access-Control-Allow-Origin", "*"
    );

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
            String jobId = extractJobId(request);
            if (hasJobIdParameter(request) && (jobId == null || jobId.isBlank())) {
                return buildErrorResponse(400, "jobId must not be blank");
            }
            if (jobId == null || jobId.isBlank()) {
                List<JobSummaryDto> jobs = jobService.getJobs();
                return buildResponse(200, objectMapper.writeValueAsString(jobs));
            }

            Optional<JobDetailsDto> details = jobService.getJobDetails(jobId);
            if (details.isEmpty()) {
                return buildErrorResponse(404, "Job not found for jobId: " + jobId);
            }

            return buildResponse(200, objectMapper.writeValueAsString(details.get()));
        } catch (IllegalArgumentException e) {
            return buildErrorResponse(400, e.getMessage());
        } catch (S3FileLoadException | ExcelParsingException e) {
            log.error("Failed to process jobs request", e);
            return buildErrorResponse(500, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error in JobLambdaHandler", e);
            return buildErrorResponse(500, "An unexpected error occurred. Please try again later.");
        }
    }

    private String extractJobId(APIGatewayProxyRequestEvent request) {
        if (request == null) {
            return null;
        }

        Map<String, String> pathParameters = request.getPathParameters();
        if (pathParameters != null && pathParameters.containsKey("jobId")) {
            return pathParameters.get("jobId");
        }

        Map<String, String> queryParameters = request.getQueryStringParameters();
        if (queryParameters != null && queryParameters.containsKey("jobId")) {
            return queryParameters.get("jobId");
        }

        return null;
    }

    private boolean hasJobIdParameter(APIGatewayProxyRequestEvent request) {
        if (request == null) {
            return false;
        }
        Map<String, String> pathParameters = request.getPathParameters();
        if (pathParameters != null && pathParameters.containsKey("jobId")) {
            return true;
        }
        Map<String, String> queryParameters = request.getQueryStringParameters();
        return queryParameters != null && queryParameters.containsKey("jobId");
    }

    private static APIGatewayProxyResponseEvent buildResponse(int statusCode, String body) {
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(statusCode)
                .withHeaders(DEFAULT_HEADERS)
                .withBody(body);
    }

    private static APIGatewayProxyResponseEvent buildErrorResponse(int statusCode, String message) {
        String body = "{\"success\":false,\"message\":\"" + escapeJson(message) + "\"}";
        return buildResponse(statusCode, body);
    }

    private static String escapeJson(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
