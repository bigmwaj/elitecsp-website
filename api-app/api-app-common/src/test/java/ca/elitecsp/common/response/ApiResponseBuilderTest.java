package ca.elitecsp.common.response;

import ca.elitecsp.common.util.Constants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiResponseBuilderTest {

    @AfterEach
    void tearDown() {
        System.clearProperty(Constants.PROP_CORS_ALLOW);
    }

    @Test
    void success_usesDefaultCorsOrigin_whenNoOverrideIsConfigured() {
        System.clearProperty(Constants.PROP_CORS_ALLOW);

        assertEquals(Constants.DEFAULT_CORS_ALLOW,
                ApiResponseBuilder.success("ok").getHeaders().get(Constants.HEADER_CORS_ORIGIN));
    }

    @Test
    void success_usesSystemPropertyCorsOrigin_whenConfigured() {
        System.setProperty(Constants.PROP_CORS_ALLOW, "https://staging.eliteproservice-consulting.ca");

        assertEquals("https://staging.eliteproservice-consulting.ca",
                ApiResponseBuilder.success("ok").getHeaders().get(Constants.HEADER_CORS_ORIGIN));
    }
}

