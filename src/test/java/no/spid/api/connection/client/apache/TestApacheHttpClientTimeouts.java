package no.spid.api.connection.client.apache;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
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
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

import no.spid.api.connection.SpidApacheHttpClientFactory;
import no.spid.api.connection.configuration.SystemPropertiesSpidClientConnectionConfiguration;
import no.spid.api.oauth.SpidOAuthBearerClientRequest;

public class TestApacheHttpClientTimeouts {

    @Rule
    public WireMockRule mockSpidRule = new WireMockRule(0);

    @After
    public void tearDown() {
        System.clearProperty(SystemPropertiesSpidClientConnectionConfiguration.SPID_READ_TIMEOUT);
        System.clearProperty(SystemPropertiesSpidClientConnectionConfiguration.SPID_CONNECTION_TIMEOUT);
    }

    @Test
    public void shouldSuccessfulyCallService() throws Exception {
        // given
        mockSpidRule.stubFor(
                get(urlEqualTo("/test?oauth_token=token"))
                        .willReturn(aResponse().withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody("some_body")));

        HttpClient client = new SpidApacheHttpClientFactory().getClient();
        OAuthClientRequest request = new SpidOAuthBearerClientRequest("http://localhost:" + mockSpidRule.port() + "/test")
                .setAccessToken("token")
                .buildQueryMessage();
        // when
        OAuthResourceResponse response = client.execute(request, new HashMap<String, String>(), "GET", OAuthResourceResponse.class);
        // then
        assertEquals(200, response.getResponseCode());
        assertEquals("some_body", response.getBody());
        assertEquals("Content-Type: application/json", response.getContentType());
        mockSpidRule.verify(1, getRequestedFor(urlEqualTo("/test?oauth_token=token")));
    }

    @Test(timeout = 1500, expected = OAuthSystemException.class)
    public void shouldTimeoutRequestAfterGivenTime() throws Exception {
        // given
        mockSpidRule.stubFor(
                get(urlEqualTo("/test?oauth_token=token"))
                        .willReturn(aResponse().withStatus(200)
                                .withFixedDelay(100000)
                                .withHeader("Content-Type", "application/json")
                                .withBody("some_body")));
        // when
        System.setProperty(SystemPropertiesSpidClientConnectionConfiguration.SPID_READ_TIMEOUT, "1000");
        HttpClient client = new SpidApacheHttpClientFactory().getClient();
        OAuthClientRequest request = new SpidOAuthBearerClientRequest("http://127.0.0.1:" + mockSpidRule.port() + "/test")
                .setAccessToken("token")
                .buildQueryMessage();
        // when
        try {
            client.execute(request, new HashMap<String, String>(), "GET", OAuthResourceResponse.class);
            // then
        } catch (OAuthSystemException e) {
            mockSpidRule.verify(1, getRequestedFor(urlEqualTo("/test?oauth_token=token")));
            assertTrue(e.getCause() instanceof SocketTimeoutException);
            throw e;
        }
        fail("Should fail!");
    }

    @Test(timeout = 1500, expected = OAuthSystemException.class)
    public void shouldTimeoutConnectionAfterGivenTime() throws Exception {
        // given
        int port = mockSpidRule.port();
        mockSpidRule.stop();
        // when
        System.setProperty(SystemPropertiesSpidClientConnectionConfiguration.SPID_CONNECTION_TIMEOUT, "1000");
        HttpClient client = new SpidApacheHttpClientFactory().getClient();
        OAuthClientRequest request = new SpidOAuthBearerClientRequest("http://127.0.0.1:" + port + "/test")
                .setAccessToken("token")
                .buildQueryMessage();
        // when
        try {
            client.execute(request, new HashMap<String, String>(), "GET", OAuthResourceResponse.class);
            // then
        } catch (OAuthSystemException e) {
            assertTrue(e.getCause() instanceof ConnectTimeoutException);
            throw e;
        }
        fail("Should fail!");
    }
}
