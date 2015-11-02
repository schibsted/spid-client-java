package no.spid.api.connection.client.apache.helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.Header;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TestHeadersCollector {

    @Mock
    private OAuthClientRequest request;

    @InjectMocks
    private HeadersCollector headersCollector;

    @Test
    public void shouldReturnOnlyContenttypeHeaderWhenHeadersAreEmpty() throws Exception {
        // given

        // when
        Header[] headers = headersCollector.getHeaders(request, null);
        // then
        assertNotNull(headers);
        assertEquals(1, headers.length);
        assertEquals("Content-Type", headers[0].getName());
        assertEquals("application/x-www-form-urlencoded", headers[0].getValue());
    }

    @Test
    public void shouldNotAttachDefaultContentTypeHeader() throws Exception {
        // given
        Map<String, String> headersMap = new HashMap<String, String>();
        headersMap.put("Content-Type", "application/json");
        // when
        Header[] headers = headersCollector.getHeaders(request, headersMap);
        // then
        assertNotNull(headers);
        assertEquals(1, headers.length);
        assertEquals("Content-Type", headers[0].getName());
        assertEquals("application/json", headers[0].getValue());
    }

    @Test
    public void shouldRewriteHeadersFromOAuthClientRequest() throws Exception {
        // given
        Map<String, String> headersMap = new HashMap<String, String>();
        headersMap.put("h1", "v1");
        when(request.getHeaders()).thenReturn(headersMap);
        // when
        Header[] headers = headersCollector.getHeaders(request, null);
        // then
        assertNotNull(headers);
        assertEquals(2, headers.length);
        assertEquals("h1", headers[0].getName());
        assertEquals("v1", headers[0].getValue());
    }

    @Test
    public void shouldRewriteHeadersFromHeadersMap() throws Exception {
        // given
        Map<String, String> headersMap = new HashMap<String, String>();
        headersMap.put("h1", "v1");
        // when
        Header[] headers = headersCollector.getHeaders(request, headersMap);
        // then
        assertNotNull(headers);
        assertEquals(2, headers.length);
        assertEquals("h1", headers[0].getName());
        assertEquals("v1", headers[0].getValue());
    }

}
