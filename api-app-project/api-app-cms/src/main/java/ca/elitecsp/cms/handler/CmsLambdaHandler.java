package ca.elitecsp.cms.handler;

import ca.elitecsp.cms.model.CmsRequest;
import ca.elitecsp.cms.model.JobDto;
import ca.elitecsp.cms.service.S3DownloadException;
import ca.elitecsp.cms.service.S3Service;
import ca.elitecsp.cms.service.XmlParserService;
import ca.elitecsp.cms.service.XmlParsingException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * AWS Lambda handler for the CMS module.
 *
 * <p>Reads an XML job listing from S3, parses it, and returns the data as a
 * JSON array suitable for rendering a table in the front-end.
 *
 * <p>Expected request body:
 * <pre>{@code
 * {
 *   "bucketName": "my-bucket",
 *   "fileKey":    "jobs/jobs.xml"
 * }
 * }</pre>
 *
 * <p>Successful response body (HTTP 200):
 * <pre>{@code
 * [
 *   { "jobId": "001", "title": "Java Developer", "department": "IT", "location": "Montreal" }
 * ]
 * }</pre>
 */
@Slf4j
public class CmsLambdaHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final Map<String, String> DEFAULT_HEADERS = Map.of(
            "Content-Type", "application/json",
            "Access-Control-Allow-Origin", "*"
    );

    private final S3Service s3Service;
    private final XmlParserService xmlParserService;
    private final ObjectMapper objectMapper;

    /** Default constructor used by the Lambda runtime. */
    public CmsLambdaHandler() {
        this.s3Service = new S3Service();
        this.xmlParserService = new XmlParserService();
        this.objectMapper = new ObjectMapper();
    }

    /** Package-private constructor for dependency injection in tests. */
    CmsLambdaHandler(S3Service s3Service, XmlParserService xmlParserService, ObjectMapper objectMapper) {
        this.s3Service = s3Service;
        this.xmlParserService = xmlParserService;
        this.objectMapper = objectMapper;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        log.info("CmsLambdaHandler invoked");
        try {
            CmsRequest cmsRequest = parseRequest(request);
            validateRequest(cmsRequest);

            String xmlContent = s3Service.downloadAsString(cmsRequest.getBucketName(), cmsRequest.getFileKey());
            List<JobDto> jobs = xmlParserService.parse(xmlContent);

            String responseBody = objectMapper.writeValueAsString(jobs);
            log.info("Returning {} job(s)", jobs.size());
            return buildResponse(200, responseBody);

        } catch (IllegalArgumentException e) {
            log.warn("Validation error: {}", e.getMessage());
            return buildErrorResponse(400, e.getMessage());
        } catch (S3DownloadException e) {
            log.error("S3 download failed: {}", e.getMessage());
            return buildErrorResponse(500, "Failed to retrieve XML file from S3: " + e.getMessage());
        } catch (XmlParsingException e) {
            log.error("XML parsing failed: {}", e.getMessage());
            return buildErrorResponse(500, "Failed to parse XML job data: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error in CmsLambdaHandler", e);
            return buildErrorResponse(500, "An unexpected error occurred. Please try again later.");
        }
    }

    private CmsRequest parseRequest(APIGatewayProxyRequestEvent request) throws Exception {
        String body = request.getBody();
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("Request body must not be empty");
        }
        return objectMapper.readValue(body, CmsRequest.class);
    }

    private static void validateRequest(CmsRequest req) {
        if (req.getBucketName() == null || req.getBucketName().isBlank()) {
            throw new IllegalArgumentException("bucketName must not be blank");
        }
        if (req.getFileKey() == null || req.getFileKey().isBlank()) {
            throw new IllegalArgumentException("fileKey must not be blank");
        }
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
