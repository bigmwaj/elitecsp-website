package ca.elitecsp.job.handler;

import ca.elitecsp.common.util.Constants;
import ca.elitecsp.job.model.JobDetailsDto;
import ca.elitecsp.job.model.JobSummaryDto;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

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

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private APIGatewayProxyRequestEvent requestWithQuery(String lang) {
        APIGatewayProxyRequestEvent req = new APIGatewayProxyRequestEvent();
        if (lang != null) {
            req.setQueryStringParameters(Map.of("lang", lang));
        }
        return req;
    }

    private APIGatewayProxyRequestEvent requestWithJobId(String lang, String jobId) {
        APIGatewayProxyRequestEvent req = requestWithQuery(lang);
        req.setPathParameters(Map.of("jobId", jobId));
        return req;
    }

    // -------------------------------------------------------------------------
    // List jobs
    // -------------------------------------------------------------------------

    @Test
    void handleRequest_returns200_withJobList() {
        JobSummaryDto job = JobSummaryDto.builder().jobId("001").title("Developer").build();
        when(jobService.getJobs("en")).thenReturn(List.of(job));

        APIGatewayProxyResponseEvent resp = handler.handleRequest(requestWithQuery("en"), context);

        assertEquals(200, resp.getStatusCode());
        assertTrue(resp.getBody().contains("001"));
        verify(jobService).getJobs("en");
    }

    @Test
    void handleRequest_returns200_forFrenchLang() {
        when(jobService.getJobs("fr")).thenReturn(List.of());
        APIGatewayProxyResponseEvent resp = handler.handleRequest(requestWithQuery("fr"), context);
        assertEquals(200, resp.getStatusCode());
        verify(jobService).getJobs("fr");
    }

    @Test
    void handleRequest_defaultsToEnglish_whenLangMissing() {
        when(jobService.getJobs("en")).thenReturn(List.of());
        APIGatewayProxyResponseEvent resp = handler.handleRequest(
                new APIGatewayProxyRequestEvent(), context);
        assertEquals(200, resp.getStatusCode());
        verify(jobService).getJobs("en");
    }

    @Test
    void handleRequest_defaultsToEnglish_forUnsupportedLang() {
        when(jobService.getJobs("en")).thenReturn(List.of());
        APIGatewayProxyResponseEvent resp = handler.handleRequest(requestWithQuery("es"), context);
        assertEquals(200, resp.getStatusCode());
        verify(jobService).getJobs("en");
    }

    // -------------------------------------------------------------------------
    // Job details
    // -------------------------------------------------------------------------

    @Test
    void handleRequest_returns200_forExistingJobId() {
        JobDetailsDto details = JobDetailsDto.builder().jobId("001").title("Developer").build();
        when(jobService.getJobDetails("en", "001")).thenReturn(Optional.of(details));

        APIGatewayProxyResponseEvent resp = handler.handleRequest(
                requestWithJobId("en", "001"), context);

        assertEquals(200, resp.getStatusCode());
        assertTrue(resp.getBody().contains("001"));
    }

    @Test
    void handleRequest_returns404_forNonExistentJobId() {
        when(jobService.getJobDetails(anyString(), anyString())).thenReturn(Optional.empty());

        APIGatewayProxyResponseEvent resp = handler.handleRequest(
                requestWithJobId("en", "999"), context);

        assertEquals(404, resp.getStatusCode());
    }

    // -------------------------------------------------------------------------
    // Error handling
    // -------------------------------------------------------------------------

    @Test
    void handleRequest_returns500_whenServiceThrows() {
        when(jobService.getJobs(anyString()))
                .thenThrow(new RuntimeException("unexpected"));

        APIGatewayProxyResponseEvent resp = handler.handleRequest(requestWithQuery("en"), context);

        assertEquals(500, resp.getStatusCode());
    }

    @Test
    void handleRequest_responseHasCorsHeader() {
        when(jobService.getJobs("en")).thenReturn(List.of());
        APIGatewayProxyResponseEvent resp = handler.handleRequest(requestWithQuery("en"), context);
        assertEquals(Constants.getCorsAllow(), resp.getHeaders().get("Access-Control-Allow-Origin"));
    }
}
