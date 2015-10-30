package no.spid.api.connection.client.apache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashMap;

import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthClientResponse;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import no.spid.api.connection.client.apache.helpers.HttpResponseProcessor;
import no.spid.api.connection.client.apache.helpers.HttpUriRequestBuilder;
import no.spid.api.connection.client.util.OAuthClientResponseFactoryWrapper;

@RunWith(MockitoJUnitRunner.class)
public class TestApacheHttpClient {

    @Mock
    private CloseableHttpClient httpClient;

    @Mock
    private OAuthClientResponseFactoryWrapper oAuthClientResponseFactoryWrapper;

    @Mock
    private HttpUriRequestBuilder httpUriRequestBuilder;

    @Mock
    private HttpResponseProcessor httpResponseProcessor;

    @Mock
    private CloseableHttpResponse closeableHttpResponse;

    @Mock
    private OAuthClientRequest oAuthClientRequest;

    @Mock
    private OAuthClientResponse oAuthClientResponse;

    @Mock
    private HttpUriRequest httpUriRequest;

    private HashMap<String, String> headers = new HashMap<String, String>();

    @InjectMocks
    private ApacheHttpClient apacheHttpClient;

    @Before
    public void setup() throws Exception {
        when(httpUriRequestBuilder.buildHttpUriRequest(any(OAuthClientRequest.class), anyString(), anyMapOf(String.class, String.class))).thenReturn(httpUriRequest);
        when(httpResponseProcessor.getBody(any(CloseableHttpResponse.class))).thenReturn("body");
        when(httpResponseProcessor.getContentType(any(CloseableHttpResponse.class))).thenReturn("content_type");
        when(oAuthClientResponseFactoryWrapper.createCustomResponse(anyString(), anyString(), anyInt(), any(Class.class))).thenReturn(oAuthClientResponse);
        StatusLine statusLine = mock(StatusLine.class);
        when(statusLine.getStatusCode()).thenReturn(200);
        when(closeableHttpResponse.getStatusLine()).thenReturn(statusLine);
        when(httpClient.execute(any(HttpUriRequest.class))).thenReturn(closeableHttpResponse);
    }

    @Test
    public void shouldReturnResult() throws Exception {
        // given
        // when
        OAuthClientResponse resp = apacheHttpClient.execute(oAuthClientRequest, headers, "POST", OAuthClientResponse.class);
        // then
        assertNotNull(resp);
        assertEquals(oAuthClientResponse, resp);
        verify(httpUriRequestBuilder, times(1)).buildHttpUriRequest(eq(oAuthClientRequest), eq("POST"), eq(headers));
        verify(httpClient, times(1)).execute(eq(httpUriRequest));
        verify(httpResponseProcessor, times(1)).getBody(eq(closeableHttpResponse));
        verify(httpResponseProcessor, times(1)).getContentType(eq(closeableHttpResponse));
        verify(oAuthClientResponseFactoryWrapper, times(1)).createCustomResponse(eq("body"), eq("content_type"), eq(200), eq(OAuthClientResponse.class));
    }

    @Test
    public void shouldShutdownInternalClient() throws Exception {
        // given

        // when
        apacheHttpClient.shutdown();
        // then
        verify(httpClient, times(1)).close();
    }

    @Test
    public void shouldCloseResponseOnSuccess() throws Exception {
        // given
        // when
        apacheHttpClient.execute(oAuthClientRequest, headers, "POST", OAuthClientResponse.class);
        // then
        verify(closeableHttpResponse, times(1)).close();
    }

    @Test(expected = OAuthSystemException.class)
    public void shouldNotTryToCloseWhenResponseIsNotYetInitialized() throws Exception {
        // given
        when(httpClient.execute(any(HttpUriRequest.class))).thenThrow(new IOException());
        // when
        try {
            apacheHttpClient.execute(oAuthClientRequest, headers, "POST", OAuthClientResponse.class);
            // then
        } catch (Exception e) {
            verifyZeroInteractions(closeableHttpResponse);
            throw e;
        }
        fail("Should fail!");
    }

    @Test(expected = OAuthSystemException.class)
    public void shouldCloseResponseOnIOException() throws Exception {
        // given
        when(httpResponseProcessor.getBody(any(CloseableHttpResponse.class))).thenThrow(new IOException());
        // when
        try {
            apacheHttpClient.execute(oAuthClientRequest, headers, "POST", OAuthClientResponse.class);
            // then
        } catch (Exception e) {
            verify(closeableHttpResponse, times(1)).close();
            throw e;
        }
        fail("Should fail!");
    }

    @Test(expected = OAuthSystemException.class)
    public void shouldCloseResponseOnRuntimeException() throws Exception {
        // given
        when(httpResponseProcessor.getBody(any(CloseableHttpResponse.class))).thenThrow(new RuntimeException());
        // when
        try {
            apacheHttpClient.execute(oAuthClientRequest, headers, "POST", OAuthClientResponse.class);
            // then
        } catch (Exception e) {
            verify(closeableHttpResponse, times(1)).close();
            throw e;
        }
        fail("Should fail!");
    }
}
