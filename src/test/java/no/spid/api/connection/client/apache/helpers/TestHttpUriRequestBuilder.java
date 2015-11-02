package no.spid.api.connection.client.apache.helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TestHttpUriRequestBuilder {

    @Mock
    private HeadersCollector headersRewriter;

    @Mock
    private OAuthClientRequest oAuthClientRequest;

    @Mock
    private Map<String, String> headers;

    @InjectMocks
    private HttpUriRequestBuilder httpUriRequestBuilder;

    @Before
    public void setup() throws Exception {
        when(oAuthClientRequest.getLocationUri()).thenReturn("host");
        when(oAuthClientRequest.getBody()).thenReturn("some_body");
    }

    @Test
    public void shouldBuildPostRequest() throws Exception {
        // given

        // when
        HttpUriRequest request = httpUriRequestBuilder.buildHttpUriRequest(oAuthClientRequest, "POST", headers);
        // then
        assertNotNull(request);
        assertTrue(request instanceof HttpPost);
        assertEquals("host", request.getURI().toString());
        verify(headersRewriter, times(1)).getHeaders(eq(oAuthClientRequest), eq(headers));
    }

    @Test
    public void shouldBuildGetRequest() throws Exception {
        // given

        // when
        HttpUriRequest request = httpUriRequestBuilder.buildHttpUriRequest(oAuthClientRequest, "GET", headers);
        // then
        assertNotNull(request);
        assertTrue(request instanceof HttpGet);
        assertEquals("host", request.getURI().toString());
        verify(headersRewriter, times(1)).getHeaders(eq(oAuthClientRequest), eq(headers));
    }

    @Test
    public void shouldBuildDeleteRequest() throws Exception {
        // given

        // when
        HttpUriRequest request = httpUriRequestBuilder.buildHttpUriRequest(oAuthClientRequest, "DELETE", headers);
        // then
        assertNotNull(request);
        assertTrue(request instanceof HttpDelete);
        assertEquals("host", request.getURI().toString());
        verify(headersRewriter, times(1)).getHeaders(eq(oAuthClientRequest), eq(headers));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenMethodNotSupported() throws Exception {
        // given

        // when
        httpUriRequestBuilder.buildHttpUriRequest(oAuthClientRequest, "PUT", headers);
        // then
    }
}
