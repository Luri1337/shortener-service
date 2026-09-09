package com.dima.shortener_service.client;

import com.dima.shortener_service.dto.AnalyticsResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsClientTest {

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestHeadersUriSpec<?> requestHeadersUriSpec;

    @Mock
    private RestClient.RequestHeadersSpec<?> requestHeadersSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @InjectMocks
    private AnalyticsClient analyticsClient;

    @Test
    void getAnalytics_shouldReturnResponse_whenServiceAvailable() {
        AnalyticsResponse expected = new AnalyticsResponse("abc12345", 42L, null);

        doReturn(requestHeadersUriSpec).when(restClient).get();
        doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(anyString(), any(Object.class));
        doReturn(responseSpec).when(requestHeadersSpec).retrieve();
        doReturn(expected).when(responseSpec).body(AnalyticsResponse.class);

        AnalyticsResponse result = analyticsClient.getLinkAnalytics("abc12345");

        assertNotNull(result);
        assertEquals(42L, result.totalClicks());
        assertEquals("abc12345", result.shortCode());
    }

    @Test
    void getAnalytics_shouldReturnDefaultResponse_whenServiceUnavailable() {
        doReturn(requestHeadersUriSpec).when(restClient).get();
        doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(anyString(), any(Object.class));
        doReturn(responseSpec).when(requestHeadersSpec).retrieve();
        doThrow(new RuntimeException("Service unavailable")).when(responseSpec).body(AnalyticsResponse.class);

        AnalyticsResponse result = analyticsClient.getLinkAnalytics("abc12345");

        assertNotNull(result);
        assertEquals(0L, result.totalClicks());
        assertNull(result.lastClickAt());
        assertEquals("abc12345", result.shortCode());
    }
}