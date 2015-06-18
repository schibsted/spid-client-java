package no.spid.api.client;

import no.spid.api.connection.SpidUrlConnectionClientFactory;
import no.spid.api.exceptions.SpidApiException;
import no.spid.api.exceptions.SpidOAuthException;
import no.spid.api.oauth.SpidOAuthToken;
import no.spid.api.oauth.SpidOAuthTokenType;
import org.apache.oltu.oauth2.client.HttpClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthClientResponse;
import org.apache.oltu.oauth2.client.response.OAuthClientResponseFactory;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.client.response.OAuthResourceResponse;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.apache.oltu.oauth2.common.token.BasicOAuthToken;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import java.net.URL;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class SpidApiClientTest {

    private String SUCCESSFUL_ACCESS_TOKEN = "44005d748f89a86b6b1ad9d1ef833dc55e0d6244";
    private String SUCCESSFUL_REFRESH_TOKEN = "90bc261093c26bbdd0cab54a891f7e06298b7556";
    private String SUCCESSFUL_TOKEN_RESPONSE = "{\"access_token\":\"" + SUCCESSFUL_ACCESS_TOKEN + "\",\"expires_in\":2419200,\"scope\":null,\"user_id\":false,\"is_admin\":false,\"refresh_token\":\"" + SUCCESSFUL_REFRESH_TOKEN + "\",\"server_time\":1398246344}";
    private String SUCCESSFUL_RENEWED_ACCESS_TOKEN = "44005d748f89a86b6b1ad9d1ef833dc55e0d6244";
    private String SUCCESSFUL_RENEWED_TOKEN_RESPONSE = "{\"access_token\":\"" + SUCCESSFUL_RENEWED_ACCESS_TOKEN + "\",\"expires_in\":2419200,\"scope\":null,\"user_id\":false,\"is_admin\":false,\"refresh_token\":\"" + SUCCESSFUL_REFRESH_TOKEN + "\",\"server_time\":1398246344}";
    private String ACCESS_DENIED_ERROR = "{\"error\":\"expired_token\",\"error_code\":\"401\",\"type\":\"OAuthException\",\"error_description\":\"401 Unauthorized access!\"}";
    private String FLOW_BASE_URL = "https://fooBaseUrl/flow";
    private String FLOW_PARAMS = "?response_type=code&redirect_uri=https%3A%2F%2Ffooserver%2Flogin&client_id=fooClient";

    @Test
    public void getLoginUrl() throws Exception {
        String expectedLoginUrl = FLOW_BASE_URL + "/login" + FLOW_PARAMS;
        assertEqualURLs(expectedLoginUrl, getFooClient().getFlowUrl("login", "https://fooserver/login"));
    }

    @Test
    public void getSignupUrl() throws Exception {
        String expectedLoginUrl = FLOW_BASE_URL + "/signup" + FLOW_PARAMS;
        assertEqualURLs(expectedLoginUrl, getFooClient().getFlowUrl("signup", "https://fooserver/login"));
    }

    @Test
    public void getCheckoutUrl() throws Exception {
        String expectedLoginUrl = FLOW_BASE_URL + "/checkout" + FLOW_PARAMS;
        assertEqualURLs(expectedLoginUrl, getFooClient().getFlowUrl("checkout", "https://fooserver/login"));
    }

    @Test
    public void getLogoutUrl() throws Exception {
        assertEqualURLs("https://fooBaseUrl/logout?redirect_uri=https%3A%2F%2Ffooserver%2Flogout&oauth_token=accesstoken", getFooClient().getLogoutURL(getMockToken(), "https://fooserver/logout"));
    }

    @Test
    public void deleteRequest() throws Exception {
        SpidUrlConnectionClientFactory connectionClientFactory = getMockedConnectionClientFactoryWithFixedResponse(
                "{\"name\":\"SPP Container\",\"version\":\"0.2\",\"api\":2,\"code\":200,\"data\":\"VGhpcyBkYXRhIHdhcyBlbmNyeXB0ZWQh\",\"algorithm\":\"HMAC-SHA256\",\"sig\":\"9epFW_MQKbRUPSmKLY_tShahRxtddL9JY-vGVEOf_IA\"}",
                "application/json",
                200,
                OAuthResourceResponse.class);

        SpidApiClient spidClient = new SpidApiClient.ClientBuilder("ID", "SECRET", "SIGNSECRET", "https://redirect.uri", "https://spiddomain.no")
                .connectionClientFactory(connectionClientFactory)
                .build();

        SpidApiResponse response = spidClient.DELETE(getMockToken(), "/paylink/1", null);
        assertEquals(200, response.getResponseCode());

    }

    @Test
    public void getServerToken() throws Exception {
        //Create a mock http client that gives a fixed response
        SpidUrlConnectionClientFactory connectionClientFactory = getMockedConnectionClientFactoryWithFixedResponse(
                SUCCESSFUL_TOKEN_RESPONSE,
                "application/json",
                200,
                OAuthJSONAccessTokenResponse.class);

        SpidApiClient spidClient = new SpidApiClient.ClientBuilder("ID", "SECRET", "SIGNSECRET", "https://redirect.uri", "https://spiddomain.no")
                .connectionClientFactory(connectionClientFactory)
                .build();

        SpidOAuthToken token = spidClient.getServerToken();
        assertEquals(SpidOAuthTokenType.CLIENT, token.getType());
        assertEquals(SUCCESSFUL_ACCESS_TOKEN, token.getAccessToken());
        assertEquals(SUCCESSFUL_REFRESH_TOKEN, token.getRefreshToken());
        assertEquals(Long.valueOf(2419200), token.getExpiresIn());
    }

    @Test
    public void readAndDecryptSignedResponse() throws Exception {
        SpidUrlConnectionClientFactory connectionClientFactory = getMockedConnectionClientFactoryWithFixedResponse(
                "{\"name\":\"SPP Container\",\"version\":\"0.2\",\"api\":2,\"data\":\"VGhpcyBkYXRhIHdhcyBlbmNyeXB0ZWQh\",\"algorithm\":\"HMAC-SHA256\",\"sig\":\"9epFW_MQKbRUPSmKLY_tShahRxtddL9JY-vGVEOf_IA\"}",
                "application/json",
                200,
                OAuthResourceResponse.class);

        SpidApiClient spidClient = new SpidApiClient.ClientBuilder("ID", "SECRET", "SIGNSECRET", "https://redirect.uri", "https://spiddomain.no")
                .connectionClientFactory(connectionClientFactory)
                .build();

        SpidApiResponse response = spidClient.GET(getMockToken(), "/user/1", null);
        assertEquals("This data was encrypted!", response.getRawData());
    }

    @Test(expected = SpidApiException.class)
    public void testApiError() throws Exception {
        SpidUrlConnectionClientFactory connectionClientFactory = getMockedConnectionClientFactoryWithFixedResponse(
                "NOT IMPORTANT",
                "application/json",
                403,
                OAuthResourceResponse.class);

        SpidApiClient spidClient = new SpidApiClient.ClientBuilder("ID", "SECRET", "SIGNSECRET", "https://redirect.uri", "https://spiddomain.no")
                .connectionClientFactory(connectionClientFactory)
                .build();

        SpidApiResponse response = spidClient.GET(getMockToken(), "/me", null);
        fail("Exception shoudlve been thrown!");
    }

    @Test
    public void handleApiError() {
        SpidUrlConnectionClientFactory connectionClientFactory = getMockedConnectionClientFactoryWithFixedResponse(
                ACCESS_DENIED_ERROR,
                "application/json",
                403,
                OAuthResourceResponse.class);

        SpidApiClient spidClient = new SpidApiClient.ClientBuilder("ID", "SECRET", "SIGNSECRET", "https://redirect.uri", "https://spiddomain.no")
                .connectionClientFactory(connectionClientFactory)
                .build();

        try {
            SpidApiResponse response = spidClient.GET(getMockToken(), "/me", null);
        } catch ( SpidOAuthException e) {
            fail("This exception was not expected!");
        } catch (SpidApiException e) {
            assertEquals("403:" + ACCESS_DENIED_ERROR, e.getMessage());
            assertEquals(new Integer(403), e.getResponseCode());
            assertEquals(ACCESS_DENIED_ERROR, e.getResponseBody());
        }
    }

    @Test
    public void testRefreshToken() throws Exception {
        SpidUrlConnectionClientFactory connectionClientFactory = getMockedConnectionClientFactoryWithFixedResponseAndToken(
                "{\"VALUE\":\"NOT IMPORTANT\"}",
                "application/json",
                200,
                OAuthResourceResponse.class);

        SpidApiClient spidClient = new SpidApiClient.ClientBuilder("ID", "SECRET", "SIGNSECRET", "https://redirect.uri", "https://spiddomain.no")
                .connectionClientFactory(connectionClientFactory)
                .build();

        SpidOAuthToken token = getExpiredMockToken();
        Long expiresAt = token.getExpiresAt();
        SpidApiResponse response = spidClient.GET(token, "/me", null);

        // Make sure the token is refreshed properly by checking it has gotten a new expiresAt
        assertNotEquals(expiresAt, token.getExpiresAt());
    }

    @Test
    public void testRenewOnFailedTokenRefresh() throws Exception {
        SpidUrlConnectionClientFactory connectionClientFactory = getMockedConnectionClientFactoryForAutoRenew(
                "{\"VALUE\":\"NOT IMPORTANT\"}",
                "application/json",
                200,
                OAuthResourceResponse.class);

        SpidApiClient spidClient = new SpidApiClient.ClientBuilder("ID", "SECRET", "SIGNSECRET", "https://redirect.uri", "https://spiddomain.no")
                .connectionClientFactory(connectionClientFactory)
                .build();

        SpidOAuthToken token = getExpiredMockToken();
        SpidApiResponse response = spidClient.GET(token, "/me", null);

        assertThat(token.getAccessToken(), is(SUCCESSFUL_RENEWED_ACCESS_TOKEN));
    }

    /** END OF TESTS **/

    private SpidApiClient getFooClient() {
        return new SpidApiClient.ClientBuilder("fooClient", "fooSecret", "fooSignatureSecret", "https://fooserver", "https://fooBaseUrl").build();
    }

    /**
     * Create a mock token
     *
     * @return a mocked token
     */
    private SpidOAuthToken getMockToken() {
        return new SpidOAuthToken(new BasicOAuthToken("accesstoken", 3600L, "refreshtoken", null), SpidOAuthTokenType.CLIENT);
    }

    /**
     * Create an expired mock token
     *
     * @return a mocked token
     */
    private SpidOAuthToken getExpiredMockToken() {
        return new SpidOAuthToken(new BasicOAuthToken("accesstoken", -1L, "refreshtoken", null), SpidOAuthTokenType.CLIENT);
    }

    /**
     * Creates a mock http client that gives a fixed response.
     *
     * @param responseBody the response
     * @param contentType  the content type
     * @param responseCode the response code
     * @param responseClass the response type (class)
     * @param <T> must be of type OAuthClientResponse
     * @return a http client that will give the supplied response to all execute calls
     */
    private <T extends OAuthClientResponse> SpidUrlConnectionClientFactory getMockedConnectionClientFactoryWithFixedResponse(String responseBody, String contentType, Integer responseCode, Class<T> responseClass) {
        HttpClient httpClient;

        try {
            httpClient = mock(HttpClient.class);
            T response = OAuthClientResponseFactory.createCustomResponse(responseBody, contentType, responseCode, responseClass);
            when(httpClient.execute(any(OAuthClientRequest.class), anyMapOf(String.class, String.class), anyString(), any(Class.class))).thenReturn(response);
        } catch (Exception e) {
            return null;
        }

        // Build spid client with mocked http client
        SpidUrlConnectionClientFactory connectionClientFactory = mock(SpidUrlConnectionClientFactory.class);
        when(connectionClientFactory.getClient()).thenReturn(httpClient);

        return connectionClientFactory;
    }

    /**
     * Creates a mock http client that gives a fixed response and in addition always responds successfully to a token
     * request.
     *
     * @param responseBody the response
     * @param contentType  the content type
     * @param responseCode the response code
     * @param responseClass the response type (class)
     * @param <T> must be of type OAuthClientResponse
     * @return a http client that will give the supplied response to all execute calls
     */
    private <T extends OAuthClientResponse> SpidUrlConnectionClientFactory getMockedConnectionClientFactoryWithFixedResponseAndToken(String responseBody, String contentType, Integer responseCode, Class<T> responseClass) {
        HttpClient httpClient;

        try {
            httpClient = mock(HttpClient.class);
            OAuthJSONAccessTokenResponse responseToken = OAuthClientResponseFactory.createCustomResponse(SUCCESSFUL_TOKEN_RESPONSE, "application/json", 200, OAuthJSONAccessTokenResponse.class);
            T response = OAuthClientResponseFactory.createCustomResponse(responseBody, contentType, responseCode, responseClass);
            // NB Order matters on matching
            when(httpClient.execute(any(OAuthClientRequest.class), anyMapOf(String.class, String.class), anyString(), any(Class.class))).thenReturn(response);
            when(httpClient.execute(any(OAuthClientRequest.class), anyMapOf(String.class, String.class), anyString(), eq(OAuthJSONAccessTokenResponse.class))).thenReturn(responseToken);
        } catch (Exception e) {
            return null;
        }

        // Build spid client with mocked http client
        SpidUrlConnectionClientFactory connectionClientFactory = mock(SpidUrlConnectionClientFactory.class);
        when(connectionClientFactory.getClient()).thenReturn(httpClient);

        return connectionClientFactory;
    }

    /**
     * Creates a mock http client that gives a fixed response and in addition responds successfully to a new token request while failing refresh token request
     * request.
     *
     * @param responseBody the response
     * @param contentType  the content type
     * @param responseCode the response code
     * @param responseClass the response type (class)
     * @param <T> must be of type OAuthClientResponse
     * @return a http client that will give the supplied response to all execute calls
     */
    private <T extends OAuthClientResponse> SpidUrlConnectionClientFactory getMockedConnectionClientFactoryForAutoRenew(String responseBody, String contentType, Integer responseCode, Class<T> responseClass) {
        HttpClient httpClient;

        try {
            httpClient = mock(HttpClient.class);
            OAuthJSONAccessTokenResponse responseToken = OAuthClientResponseFactory.createCustomResponse(SUCCESSFUL_RENEWED_TOKEN_RESPONSE, "application/json", 200, OAuthJSONAccessTokenResponse.class);
            T response = OAuthClientResponseFactory.createCustomResponse(responseBody, contentType, responseCode, responseClass);
            // NB Order matters on matching
            when(httpClient.execute(any(OAuthClientRequest.class), anyMapOf(String.class, String.class), anyString(), any(Class.class))).thenReturn(response);
            when(httpClient.execute(argThat(new OauthGrantTypeMatcher(GrantType.REFRESH_TOKEN)), anyMapOf(String.class, String.class), anyString(), eq(OAuthJSONAccessTokenResponse.class))).thenThrow(OAuthProblemException.error("Unable to refresh"));
            when(httpClient.execute(argThat(new OauthGrantTypeMatcher(GrantType.CLIENT_CREDENTIALS)), anyMapOf(String.class, String.class), anyString(), eq(OAuthJSONAccessTokenResponse.class))).thenReturn(responseToken);
        } catch (Exception e) {
            return null;
        }

        // Build spid client with mocked http client
        SpidUrlConnectionClientFactory connectionClientFactory = mock(SpidUrlConnectionClientFactory.class);
        when(connectionClientFactory.getClient()).thenReturn(httpClient);

        return connectionClientFactory;
    }

    /**
     * Utility method to compare two urls. Ignoring order on parameters.
     *
     * @param urlExpected expected url
     * @param urlActual actual url
     * @throws Exception any error
     */
    private void assertEqualURLs(String urlExpected, String urlActual) throws Exception {
        URL uExpected = new URL(urlExpected);
        URL uActual = new URL(urlActual);

        // Check path
        assertEquals(uExpected.getProtocol(), uActual.getProtocol());
        assertEquals(uExpected.getHost(), uActual.getHost());
        assertEquals(uExpected.getPort(), uActual.getPort());
        assertEquals(uExpected.getPath(), uActual.getPath());

        // Check parameters, ignore order
        String[] actualParameters = uActual.getQuery().split("&");
        String[] expectedParameters = uExpected.getQuery().split("&");

        Arrays.sort(actualParameters);
        Arrays.sort(expectedParameters);
        if (!Arrays.equals(actualParameters, expectedParameters)) {
            fail("URLs are not equal!");
        }
    }

    /**
     * Custom ArgumentMatcher to be able take different paths in mock depending on a OAutClientRequests token GrantType
     */
    private class OauthGrantTypeMatcher extends ArgumentMatcher<OAuthClientRequest> {

        private final GrantType wantedGrantType;

        public OauthGrantTypeMatcher(final GrantType wantedGrantType) {
            this.wantedGrantType = wantedGrantType;
        }

        @Override
        public boolean matches(final Object argument) {
            if (argument != null && ((OAuthClientRequest) argument).getBody() != null) {
                String body = ((OAuthClientRequest) argument).getBody();
                for (String s : body.split("&")) {
                    String[] split = s.split("=");
                    if ("grant_type".equals(split[0])) {
                        return GrantType.valueOf(split[1].toUpperCase()).equals(wantedGrantType);
                    }
                }
            }

            return false;
        }
    }
}
