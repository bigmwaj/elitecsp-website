package ca.elitecsp.job.handler;

import ca.elitecsp.job.model.JobDetailDto;
import ca.elitecsp.job.model.JobSummaryDto;
import ca.elitecsp.job.service.JobDataException;
import ca.elitecsp.job.service.JobNotFoundException;
import ca.elitecsp.job.service.JobService;
import ca.elitecsp.job.service.S3DownloadException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
public class JobLambdaHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final Map<String, String> DEFAULT_HEADERS = Map.of(
            "Content-Type", "application/json",
            "Access-Control-Allow-Origin", "*"
    );

    private final JobService jobService;
    private final ObjectMapper objectMapper;

    public JobLambdaHandler() {
        this.jobService = new JobService();
        this.objectMapper = new ObjectMapper();
    }

    JobLambdaHandler(JobService jobService, ObjectMapper objectMapper) {
        this.jobService = jobService;
        this.objectMapper = objectMapper;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        try {
            if (!"GET".equalsIgnoreCase(request.getHttpMethod())) {
                return buildErrorResponse(405, "Method not allowed");
            }

            String jobId = extractJobId(request);
            if (jobId == null || jobId.isBlank()) {
                List<JobSummaryDto> jobs = jobService.listJobs();
                return buildResponse(200, objectMapper.writeValueAsString(jobs));
            }

            JobDetailDto details = jobService.getJobDetail(jobId);
            return buildResponse(200, objectMapper.writeValueAsString(details));
        } catch (JobNotFoundException e) {
            return buildErrorResponse(404, e.getMessage());
        } catch (IllegalArgumentException e) {
            return buildErrorResponse(400, e.getMessage());
        } catch (IllegalStateException | S3DownloadException | JobDataException e) {
            log.error("Job processing error: {}", e.getMessage(), e);
            return buildErrorResponse(500, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error in JobLambdaHandler", e);
            return buildErrorResponse(500, "An unexpected error occurred. Please try again later.");
        }
    }

    private String extractJobId(APIGatewayProxyRequestEvent request) {
        Map<String, String> pathParameters = request.getPathParameters();
        if (pathParameters != null && pathParameters.containsKey("jobId")) {
            return pathParameters.get("jobId");
        }

        String path = request.getPath();
        if (path != null && path.startsWith("/jobs/") && path.length() > "/jobs/".length()) {
            return path.substring("/jobs/".length());
        }
        return null;
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
