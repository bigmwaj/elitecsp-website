package ca.elitecsp.job.handler;

import ca.elitecsp.job.model.JobDetailsDto;
import ca.elitecsp.job.model.JobSummaryDto;
import ca.elitecsp.job.service.JobService;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("JobLambdaHandler")
class JobLambdaHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private JobService jobService;
    private JobLambdaHandler handler;

    @BeforeEach
    void setUp() {
        jobService = mock(JobService.class);
        handler = new JobLambdaHandler(jobService, objectMapper);
    }

    @Test
    @DisplayName("GET /jobs returns job listing")
    void handleRequest_jobListing_returns200() throws Exception {
        when(jobService.getJobs()).thenReturn(List.of(
                JobSummaryDto.builder().jobId("001").title("Java Developer").location("Montreal").build()
        ));

        APIGatewayProxyResponseEvent response = handler.handleRequest(new APIGatewayProxyRequestEvent(), mock(Context.class));

        assertEquals(200, response.getStatusCode());
        JsonNode body = objectMapper.readTree(response.getBody());
        assertTrue(body.isArray());
        assertEquals("001", body.get(0).get("jobId").asText());
    }

    @Test
    @DisplayName("GET /jobs/{jobId} returns details")
    void handleRequest_jobDetails_returns200() throws Exception {
        JobDetailsDto details = JobDetailsDto.builder()
                .jobId("001")
                .title("Java Developer")
                .description("Detailed job")
                .build();
        when(jobService.getJobDetails("001")).thenReturn(Optional.of(details));

        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent()
                .withPathParameters(Map.of("jobId", "001"));

        APIGatewayProxyResponseEvent response = handler.handleRequest(request, mock(Context.class));

        assertEquals(200, response.getStatusCode());
        JsonNode body = objectMapper.readTree(response.getBody());
        assertEquals("001", body.get("jobId").asText());
        assertEquals("Detailed job", body.get("description").asText());
    }

    @Test
    @DisplayName("GET /jobs/{jobId} returns 404 for unknown jobId")
    void handleRequest_unknownJob_returns404() throws Exception {
        when(jobService.getJobDetails("999")).thenReturn(Optional.empty());

        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent()
                .withPathParameters(Map.of("jobId", "999"));

        APIGatewayProxyResponseEvent response = handler.handleRequest(request, mock(Context.class));

        assertEquals(404, response.getStatusCode());
        JsonNode body = objectMapper.readTree(response.getBody());
        assertFalse(body.get("success").asBoolean());
    }

    @Test
    @DisplayName("blank explicit jobId returns 400")
    void handleRequest_invalidJobId_returns400() throws Exception {
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent()
                .withQueryStringParameters(Map.of("jobId", " "));

        APIGatewayProxyResponseEvent response = handler.handleRequest(request, mock(Context.class));

        assertEquals(400, response.getStatusCode());
        JsonNode body = objectMapper.readTree(response.getBody());
        assertFalse(body.get("success").asBoolean());
        verifyNoInteractions(jobService);
    }
}
