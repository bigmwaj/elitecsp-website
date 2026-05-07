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

@Slf4j
public class CmsLambdaHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final Map<String, String> DEFAULT_HEADERS = Map.of(
            "Content-Type", "application/json",
            "Access-Control-Allow-Origin", "*"
    );

    private final S3Service s3Service;
    private final XmlParserService xmlParserService;
    private final ObjectMapper objectMapper;

    /**
     * Default constructor used by the Lambda runtime.
     */
    public CmsLambdaHandler() {
        this.s3Service = new S3Service();
        this.xmlParserService = new XmlParserService();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Package-private constructor for dependency injection in tests.
     */
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

            String xmlContent = s3Service.downloadJobFileAsString();
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
        Map<String, String> queryParam = request.getQueryStringParameters();
        CmsRequest cmsRequest = new CmsRequest();
        if (queryParam.containsKey("jobId")) {
            cmsRequest.setJobId(queryParam.get("jobId"));
        }
        return cmsRequest;
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
