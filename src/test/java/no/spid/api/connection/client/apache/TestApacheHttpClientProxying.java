package no.spid.api.connection.client.apache;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.SocketTimeoutException;
import java.util.HashMap;

import org.apache.http.conn.ConnectTimeoutException;
import org.apache.oltu.oauth2.client.HttpClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthResourceResponse;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

import no.spid.api.connection.SpidApacheHttpClientFactory;
import no.spid.api.connection.configuration.SystemPropertiesSpidClientConnectionConfiguration;
import no.spid.api.oauth.SpidOAuthBearerClientRequest;

public class TestApacheHttpClientProxying {

    @Rule
    public WireMockRule mockSpidRule = new WireMockRule(0);

    @Rule
    public WireMockRule proxyRule = new WireMockRule(0);

    @Before
    public void setup() throws Exception {
        System.setProperty(SystemPropertiesSpidClientConnectionConfiguration.SPID_PROXY_HOST, "127.0.0.1");
        System.setProperty(SystemPropertiesSpidClientConnectionConfiguration.SPID_PROXY_PORT, String.valueOf(proxyRule.port()));
    }

    @After
    public void tearDown() {
        System.clearProperty(SystemPropertiesSpidClientConnectionConfiguration.SPID_READ_TIMEOUT);
        System.clearProperty(SystemPropertiesSpidClientConnectionConfiguration.SPID_CONNECTION_TIMEOUT);
        System.clearProperty(SystemPropertiesSpidClientConnectionConfiguration.SPID_PROXY_HOST);
        System.clearProperty(SystemPropertiesSpidClientConnectionConfiguration.SPID_PROXY_PORT);
    }

    @Test
    public void shouldGoToSpidThroughProxy() throws Exception {
        // given
        proxyRule.stubFor(get(urlMatching(".*"))
                .willReturn(aResponse().proxiedFrom("http://127.0.0.1:" + mockSpidRule.port())));

        mockSpidRule.stubFor(
                get(urlEqualTo("/test?oauth_token=token"))
                        .willReturn(aResponse().withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody("some_body")));

        HttpClient client = new SpidApacheHttpClientFactory().getClient();
        OAuthClientRequest request = new SpidOAuthBearerClientRequest("http://127.0.0.1:" + mockSpidRule.port() + "/test")
                .setAccessToken("token")
                .buildQueryMessage();
        // when
        OAuthResourceResponse response = client.execute(request, new HashMap<String, String>(), "GET", OAuthResourceResponse.class);
        // then
        assertEquals(200, response.getResponseCode());
        assertEquals("some_body", response.getBody());
        assertEquals("Content-Type: application/json", response.getContentType());
        proxyRule.verify(1, getRequestedFor(urlEqualTo("/test?oauth_token=token")));
        mockSpidRule.verify(1, getRequestedFor(urlEqualTo("/test?oauth_token=token")));
    }

    @Test(expected = OAuthSystemException.class)
    public void shouldTimeoutRequestOnProxy() throws Exception {
        // given
        proxyRule.stubFor(get(urlMatching(".*"))
                .willReturn(aResponse().withFixedDelay(10000).proxiedFrom("http://127.0.0.1:" + mockSpidRule.port())));
        System.setProperty(SystemPropertiesSpidClientConnectionConfiguration.SPID_READ_TIMEOUT, "1000");
        HttpClient client = new SpidApacheHttpClientFactory().getClient();
        OAuthClientRequest request = new SpidOAuthBearerClientRequest("http://127.0.0.1:" + mockSpidRule.port() + "/test")
                .setAccessToken("token")
                .buildQueryMessage();
        Long start = System.currentTimeMillis();
        // when
        try {
            client.execute(request, new HashMap<String, String>(), "GET", OAuthResourceResponse.class);
            // then
        } catch (OAuthSystemException e) {
            assertTrue(System.currentTimeMillis() - start < 1500);
            assertTrue(System.currentTimeMillis() - start > 900);
            proxyRule.verify(1, getRequestedFor(urlEqualTo("/test?oauth_token=token")));
            mockSpidRule.verify(0, getRequestedFor(urlEqualTo("/test?oauth_token=token")));
            assertTrue(e.getCause() instanceof SocketTimeoutException);
            throw e;
        }
        fail("Should fail!");
    }

    @Test(timeout = 1500, expected = OAuthSystemException.class)
    public void shouldTimeoutConnectionOnProxy() throws Exception {
        // given
        proxyRule.stop();
        System.setProperty(SystemPropertiesSpidClientConnectionConfiguration.SPID_CONNECTION_TIMEOUT, "1000");
        HttpClient client = new SpidApacheHttpClientFactory().getClient();
        OAuthClientRequest request = new SpidOAuthBearerClientRequest("http://127.0.0.1:" + mockSpidRule.port() + "/test")
                .setAccessToken("token")
                .buildQueryMessage();
        Long start = System.currentTimeMillis();
        // when
        try {
            client.execute(request, new HashMap<String, String>(), "GET", OAuthResourceResponse.class);
            // then
        } catch (OAuthSystemException e) {
            assertTrue(System.currentTimeMillis() - start > 900);
            mockSpidRule.verify(0, getRequestedFor(urlEqualTo("/test?oauth_token=token")));
            assertTrue(e.getCause() instanceof ConnectTimeoutException);
            throw e;
        }
        fail("Should fail!");
    }
}
