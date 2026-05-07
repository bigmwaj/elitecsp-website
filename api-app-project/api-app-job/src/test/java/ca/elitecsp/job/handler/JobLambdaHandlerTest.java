package ca.elitecsp.job.handler;

import ca.elitecsp.job.model.JobDetailDto;
import ca.elitecsp.job.model.JobSummaryDto;
import ca.elitecsp.job.service.JobNotFoundException;
import ca.elitecsp.job.service.JobService;
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobLambdaHandlerTest {

    @Mock
    private JobService jobService;

    @Mock
    private Context context;

    private JobLambdaHandler handler;

    @BeforeEach
    void setUp() {
        handler = new JobLambdaHandler(jobService, new ObjectMapper());
    }

    @Test
    void handleRequest_getJobs_returns200() {
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent()
                .withHttpMethod("GET")
                .withPath("/jobs");

        when(jobService.listJobs()).thenReturn(List.of(new JobSummaryDto("001", "Java Developer", "Montreal", "IT", null, null)));

        APIGatewayProxyResponseEvent response = handler.handleRequest(request, context);

        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("Java Developer"));
    }

    @Test
    void handleRequest_getJobById_returns200() {
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent()
                .withHttpMethod("GET")
                .withPath("/jobs/001")
                .withPathParameters(Map.of("jobId", "001"));

        when(jobService.getJobDetail("001")).thenReturn(new JobDetailDto("001", "Java Developer", "Montreal", "IT", null, null,
                "Description", List.of("A"), List.of("B"), List.of("C")));

        APIGatewayProxyResponseEvent response = handler.handleRequest(request, context);

        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("Description"));
    }

    @Test
    void handleRequest_invalidJobId_returns404() {
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent()
                .withHttpMethod("GET")
                .withPath("/jobs/999")
                .withPathParameters(Map.of("jobId", "999"));

        when(jobService.getJobDetail("999")).thenThrow(new JobNotFoundException("Job not found"));

        APIGatewayProxyResponseEvent response = handler.handleRequest(request, context);

        assertEquals(404, response.getStatusCode());
        assertTrue(response.getBody().contains("Job not found"));
    }

    @Test
    void handleRequest_nonGet_returns405() {
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent()
                .withHttpMethod("POST")
                .withPath("/jobs");

        APIGatewayProxyResponseEvent response = handler.handleRequest(request, context);

        assertEquals(405, response.getStatusCode());
    }
}
