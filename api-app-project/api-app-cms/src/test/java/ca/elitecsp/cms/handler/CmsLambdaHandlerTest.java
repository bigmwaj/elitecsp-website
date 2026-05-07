package ca.elitecsp.cms.handler;

import ca.elitecsp.cms.model.JobDto;
import ca.elitecsp.cms.service.S3DownloadException;
import ca.elitecsp.cms.service.S3Service;
import ca.elitecsp.cms.service.XmlParserService;
import ca.elitecsp.cms.service.XmlParsingException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CmsLambdaHandlerTest {

    @Mock
    private S3Service s3Service;

    @Mock
    private XmlParserService xmlParserService;

    @Mock
    private Context context;

    private CmsLambdaHandler handler;

    @BeforeEach
    void setUp() {
        handler = new CmsLambdaHandler(s3Service, xmlParserService, new ObjectMapper());
    }

    @Test
    void handleRequest_validRequest_returns200WithJobArray() throws Exception {
        String requestBody = "{\"bucketName\":\"my-bucket\",\"fileKey\":\"jobs/jobs.xml\"}";
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent().withBody(requestBody);

        String xmlContent = "<jobs><job><jobId>001</jobId><title>Java Developer</title></job></jobs>";
        JobDto job = new JobDto("001", "Java Developer", "IT", "Montreal");
        when(s3Service.downloadAsString("my-bucket", "jobs/jobs.xml")).thenReturn(xmlContent);
        when(xmlParserService.parse(xmlContent)).thenReturn(List.of(job));

        APIGatewayProxyResponseEvent response = handler.handleRequest(request, context);

        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Java Developer"));
        verify(s3Service).downloadAsString("my-bucket", "jobs/jobs.xml");
        verify(xmlParserService).parse(xmlContent);
    }

    @Test
    void handleRequest_emptyBody_returns400() {
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent().withBody("");

        APIGatewayProxyResponseEvent response = handler.handleRequest(request, context);

        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("must not be empty"));
    }

    @Test
    void handleRequest_missingBucketName_returns400() {
        String requestBody = "{\"bucketName\":\"\",\"fileKey\":\"jobs/jobs.xml\"}";
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent().withBody(requestBody);

        APIGatewayProxyResponseEvent response = handler.handleRequest(request, context);

        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("bucketName"));
    }

    @Test
    void handleRequest_s3DownloadFails_returns500() {
        String requestBody = "{\"bucketName\":\"my-bucket\",\"fileKey\":\"jobs/jobs.xml\"}";
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent().withBody(requestBody);

        when(s3Service.downloadAsString("my-bucket", "jobs/jobs.xml"))
                .thenThrow(new S3DownloadException("Not found", new RuntimeException()));

        APIGatewayProxyResponseEvent response = handler.handleRequest(request, context);

        assertEquals(500, response.getStatusCode());
        assertTrue(response.getBody().contains("Failed to retrieve XML file from S3"));
    }

    @Test
    void handleRequest_xmlParsingFails_returns500() {
        String requestBody = "{\"bucketName\":\"my-bucket\",\"fileKey\":\"jobs/jobs.xml\"}";
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent().withBody(requestBody);

        String xmlContent = "<bad xml>";
        when(s3Service.downloadAsString("my-bucket", "jobs/jobs.xml")).thenReturn(xmlContent);
        when(xmlParserService.parse(xmlContent))
                .thenThrow(new XmlParsingException("Parse error", new RuntimeException()));

        APIGatewayProxyResponseEvent response = handler.handleRequest(request, context);

        assertEquals(500, response.getStatusCode());
        assertTrue(response.getBody().contains("Failed to parse XML job data"));
    }
}
