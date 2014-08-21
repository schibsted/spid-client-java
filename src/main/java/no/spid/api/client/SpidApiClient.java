package no.spid.api.client;

import no.spid.api.connection.SpidConnectionClientFactory;
import no.spid.api.connection.SpidUrlConnectionClientFactory;
import no.spid.api.exceptions.SpidApiException;
import no.spid.api.exceptions.SpidOAuthException;
import no.spid.api.oauth.SpidOAuthBearerClientRequest;
import no.spid.api.oauth.SpidOAuthToken;
import no.spid.api.oauth.SpidOAuthTokenType;
import no.spid.api.security.SpidSecurityHelper;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.client.response.OAuthResourceResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;


import java.util.Map;

/**
 * The SpidApiClient can be used to login users and get their user tokens, get server tokens. When a token is aquired
 * the client can be used to consume the services of the SPiD API.
 *
 * http://techdocs.spid.no
 */
public class SpidApiClient {
    private String clientId;
    private String clientSecret;
    private String clientSignatureSecret;
    private String redirectUrl;
    private String spidBaseUrl;
    private String spidAPIBaseUrl;
    private String spidTokenUrl;
    private String spidFlowUrl;

    private SpidSecurityHelper securityHelper;
    private SpidConnectionClientFactory connectionClientFactory;

    private boolean autorefresh;
    private boolean autorenew;
    private boolean autoDecryptSignedResponses;

    /**
     * Constructor is private, create instances using the ClientBuilder.
     *
     * @param builder the builder used to create an instance
     */
    private SpidApiClient(ClientBuilder builder) {
        clientId = builder.clientId;
        clientSecret = builder.clientSecret;
        clientSignatureSecret = builder.clientSignatureSecret;
        redirectUrl = builder.redirectUrl;
        spidBaseUrl = builder.spidBaseUrl;
        autorenew = builder.autorenew;
        autorefresh = builder.autorefresh;
        autoDecryptSignedResponses = builder.autoDecryptSignedResponses;
        spidAPIBaseUrl = spidBaseUrl + "/api/2";
        spidTokenUrl = spidBaseUrl + "/oauth/token";
        spidFlowUrl = spidBaseUrl + "/flow/";

        securityHelper = builder.securityHelper;
        connectionClientFactory = builder.connectionClientFactory;
    }

    /**
     * Get a URL to start a named flow
     *
     * Valid flow names:
     * - login
     * - signup
     * - checkout
     *
     * @param name the name of the flow you want to start
     * @param redirectUrl where to redirect the user after successful flow end
     * @return the constructed URL to start the requested flow
     * @throws SpidOAuthException
     */
    public String getFlowUrl(String name, String redirectUrl) throws SpidOAuthException{
        OAuthClientRequest request;
        String flow = spidFlowUrl + name;

        try {
            request = OAuthClientRequest
                    .authorizationLocation(flow)
                    .setClientId(clientId)
                    .setRedirectURI(redirectUrl)
                    .setResponseType(OAuth.OAUTH_CODE)
                    .buildQueryMessage();
        } catch (OAuthSystemException e) {
            throw new SpidOAuthException(e);
        }
        return request.getLocationUri();
    }

    /**
     * Get the logout url.
     *
     * @param token       access token for the user you wish to logout
     * @param redirectUrl where to redirect the user after successful logout
     * @return the constructed url
     * @throws SpidOAuthException If an OAuth related error occurs
     */
    public String getLogoutURL(SpidOAuthToken token, String redirectUrl) throws SpidOAuthException {
        OAuthClientRequest request;

        try {
            request = new SpidOAuthBearerClientRequest(spidBaseUrl + "/logout")
                    .setAccessToken(getAccessToken(token))
                    .setRedirectUrl(redirectUrl)
                    .buildQueryMessage();
        } catch (OAuthSystemException e) {
            throw new SpidOAuthException(e);
        }

        return request.getLocationUri();
    }

    /**
     * Handles the response received from the API. Error messages are thrown as exceptions on the form
     * errorcode : errormessage
     *
     * @param response the response received from the server
     * @return SpidApiResponse
     * @throws SpidApiException
     */
    private SpidApiResponse handleApiResponse(OAuthResourceResponse response) throws SpidApiException {
        SpidApiResponse spidResponse = new SpidApiResponse(response.getResponseCode(), null, response.getBody());

        if (spidResponse.getResponseCode() >= 200 && spidResponse.getResponseCode() < 300) {

            if (autoDecryptSignedResponses && spidResponse.isEncrypted()) {
                return securityHelper.decryptAndValidateSignedResponse(spidResponse);
            }

            return spidResponse;

        } else {
            throw new SpidApiException(response.getResponseCode() + ":" + response.getBody(), spidResponse);
        }
    }

    /**
     * Perform a GET request to the API with the supplied token and parameters.
     *
     * @param token      the token to use for the request
     * @param endpoint   what API endpoint to call
     * @param properties properties to supply to the API
     * @return SpidApiResponse the response received from the server
     * @throws SpidOAuthException If an OAuth related error occurs
     * @throws SpidApiException If an API related error occurs
     */
    public SpidApiResponse GET(SpidOAuthToken token, String endpoint, Map<String, String> properties) throws SpidOAuthException, SpidApiException {
        OAuthResourceResponse response;

        try {
            OAuthClientRequest request = new SpidOAuthBearerClientRequest(spidAPIBaseUrl + endpoint)
                    .setAccessToken(getAccessToken(token))
                    .addParameters(properties)
                    .buildQueryMessage();

            OAuthClient oAuthClient = new OAuthClient(connectionClientFactory.getClient());
            response = oAuthClient.resource(request, OAuth.HttpMethod.GET, OAuthResourceResponse.class);

        } catch (OAuthSystemException e) {
            throw new SpidOAuthException(e);
        } catch (OAuthProblemException e) {
            throw new SpidOAuthException(e);
        }

        return handleApiResponse(response);
    }

    /**
     * Perform a POST request to the API with the supplied token and parameters.
     *
     * @param token      the token to use for the request
     * @param endpoint   what API endpoint to call
     * @param properties properties to supply to the API
     * @return SpidApiResponse the response received from the server
     * @throws SpidOAuthException If an OAuth related error occurs
     * @throws SpidApiException If an API related error occurs
     */
    public SpidApiResponse POST(SpidOAuthToken token, String endpoint, Map<String, String> properties) throws SpidOAuthException, SpidApiException {
        OAuthResourceResponse response;

        try {
            OAuthClientRequest request = new SpidOAuthBearerClientRequest(spidAPIBaseUrl + endpoint)
                    .setAccessToken(getAccessToken(token))
                    .addParameters(properties)
                    .buildBodyMessage();

            OAuthClient oAuthClient = new OAuthClient(connectionClientFactory.getClient());
            response = oAuthClient.resource(request, OAuth.HttpMethod.POST, OAuthResourceResponse.class);

        } catch (OAuthSystemException e) {
            throw new SpidOAuthException(e);
        } catch (OAuthProblemException e) {
            throw new SpidOAuthException(e);
        }

        return handleApiResponse(response);
    }

    /**
     * Perform a DELETE request to the API with the supplied token and parameters.
     *
     * @param token      the token to use for the request
     * @param endpoint   what API endpoint to call
     * @param properties properties to supply to the API
     * @return SpidApiResponse the response received from the server
     * @throws SpidOAuthException If an OAuth related error occurs
     * @throws SpidApiException If an API related error occurs
     */
    public SpidApiResponse DELETE(SpidOAuthToken token, String endpoint, Map<String, String> properties) throws SpidOAuthException, SpidApiException {
        OAuthResourceResponse response;

        try {
            OAuthClientRequest request = new SpidOAuthBearerClientRequest(spidAPIBaseUrl + endpoint)
                    .setAccessToken(getAccessToken(token))
                    .addParameters(properties)
                    .buildQueryMessage();

            OAuthClient oAuthClient = new OAuthClient(connectionClientFactory.getClient());
            response = oAuthClient.resource(request, OAuth.HttpMethod.DELETE, OAuthResourceResponse.class);

        } catch (OAuthSystemException e) {
            throw new SpidOAuthException(e);
        } catch (OAuthProblemException e) {
            throw new SpidOAuthException(e);
        }

        return handleApiResponse(response);
    }

    /**
     * Retrieves a server token.
     *
     * @return an access token with the type SpidOAuthTokenType.CLIENT
     * @throws SpidOAuthException If an OAuth related error occurs
     */
    public SpidOAuthToken getServerToken() throws SpidOAuthException {
        OAuthJSONAccessTokenResponse oAuthResponse;

        try {
            OAuthClientRequest request = OAuthClientRequest
                    .tokenLocation(spidTokenUrl)
                    .setClientId(clientId)
                    .setClientSecret(clientSecret)
                    .setGrantType(GrantType.CLIENT_CREDENTIALS)
                    .setRedirectURI(redirectUrl)
                    .buildBodyMessage();

            OAuthClient oAuthClient = new OAuthClient(connectionClientFactory.getClient());
            oAuthResponse = oAuthClient.accessToken(request);

        } catch (OAuthSystemException e) {
            throw new SpidOAuthException(e);
        } catch (OAuthProblemException e) {
            throw new SpidOAuthException(e);
        }

        return new SpidOAuthToken(oAuthResponse.getOAuthToken(), SpidOAuthTokenType.CLIENT);
    }

    /**
     * Retrieves a user token by sending the code received from the authorization-redirect.
     *
     * @param code the received code
     * @return an access token with the type SpidOAuthTokenType.USER
     * @throws SpidOAuthException If an OAuth related error occurs
     */
    public SpidOAuthToken getUserToken(String code) throws SpidOAuthException {
        OAuthJSONAccessTokenResponse oAuthResponse;

        try {
            OAuthClientRequest request = OAuthClientRequest
                    .tokenLocation(spidTokenUrl)
                    .setClientId(clientId)
                    .setClientSecret(clientSecret)
                    .setGrantType(GrantType.AUTHORIZATION_CODE)
                    .setRedirectURI(redirectUrl)
                    .setCode(code)
                    .buildBodyMessage();

            OAuthClient oAuthClient = new OAuthClient(connectionClientFactory.getClient());
            oAuthResponse = oAuthClient.accessToken(request);

        } catch (OAuthSystemException e) {
            throw new SpidOAuthException(e);
        } catch (OAuthProblemException e) {
            throw new SpidOAuthException(e);
        }

        return new SpidOAuthToken(oAuthResponse.getOAuthToken(), SpidOAuthTokenType.USER);
    }

    /**
     * Retrieves a user token by authenticating with the user's username and password
     *
     * @param username The user's username
     * @param password The user's password
     * @return an access token with the type SpidOAuthTokenType.USER
     * @throws SpidOAuthException If an OAuth related error occurs
     */
    public SpidOAuthToken getUserToken(String username, String password) throws SpidOAuthException {
        OAuthJSONAccessTokenResponse oAuthResponse;

        try {
            OAuthClientRequest request = OAuthClientRequest
                    .tokenLocation(spidTokenUrl)
                    .setClientId(clientId)
                    .setClientSecret(clientSecret)
                    .setGrantType(GrantType.PASSWORD)
                    .setRedirectURI(redirectUrl)
                    .setUsername(username)
                    .setPassword(password)
                    .buildBodyMessage();

            OAuthClient oAuthClient = new OAuthClient(connectionClientFactory.getClient());
            oAuthResponse = oAuthClient.accessToken(request);

        } catch (OAuthSystemException e) {
            throw new SpidOAuthException(e);
        } catch (OAuthProblemException e) {
            throw new SpidOAuthException(e);
        }

        return new SpidOAuthToken(oAuthResponse.getOAuthToken(), SpidOAuthTokenType.USER);
    }

    /**
     * Wrapper for validating/refreshing/refetching an oauth-token automatically if enabled. When the token is validated
     * its access token part is returned.
     *
     * @param token the token to validate
     * @return access token
     * @throws SpidOAuthException If an OAuth related error occurs
     */
    private String getAccessToken(SpidOAuthToken token) throws SpidOAuthException {
        if (token == null) {
            throw new SpidOAuthException("No token supplied");
        }
        if (!token.isExpired()) {
            return token.getAccessToken();
        }
        // Try to get a new token by using the refresh token
        if (autorefresh && refreshToken(token)) {
            return token.getAccessToken();
        }
        // Refreshing token failed, if this is a server and autorenew is enabled just get a new one
        if (autorenew && token.getType() == SpidOAuthTokenType.CLIENT) {
            token.refresh(getServerToken());
            return token.getAccessToken();
        }

        throw new SpidOAuthException("Failed all attempts to get a valid token.");
    }

    /**
     * Refreshes the supplied token.
     *
     * @param token the token to refresh
     * @return true if the token could be refreshed, false if not.
     * @throws SpidOAuthException If an OAuth related error occurs
     */
    private boolean refreshToken(SpidOAuthToken token) throws SpidOAuthException {
        OAuthJSONAccessTokenResponse oAuthResponse;

        try {
            OAuthClientRequest request = OAuthClientRequest
                    .tokenLocation(spidTokenUrl)
                    .setClientId(clientId)
                    .setClientSecret(clientSecret)
                    .setGrantType(GrantType.REFRESH_TOKEN)
                    .setRedirectURI(redirectUrl)
                    .setRefreshToken(token.getRefreshToken())
                    .buildBodyMessage();

            OAuthClient oAuthClient = new OAuthClient(connectionClientFactory.getClient());
            oAuthResponse = oAuthClient.accessToken(request);

        } catch (OAuthSystemException e) {
            throw new SpidOAuthException(e);
        } catch (OAuthProblemException e) {
            throw new SpidOAuthException(e);
        }

        SpidOAuthToken newToken = new SpidOAuthToken(oAuthResponse.getOAuthToken(), token.getType());
        token.refresh(newToken);

        return true;
    }

    /**
     * Builder class used to build a SpidApiClient
     */
    public static class ClientBuilder {
        private String clientId;
        private String clientSecret;
        private String clientSignatureSecret;
        private String redirectUrl;
        private String spidBaseUrl;

        private SpidSecurityHelper securityHelper;
        private SpidConnectionClientFactory connectionClientFactory;

        private boolean autorefresh = true;
        private boolean autorenew = true;
        private boolean autoDecryptSignedResponses = true;

        public ClientBuilder(String clientId, String clientSecret, String clientSignatureSecret, String redirectUrl, String spidBaseUrl) {
            this.clientId = clientId;
            this.clientSecret = clientSecret;
            this.clientSignatureSecret = clientSignatureSecret;
            this.redirectUrl = redirectUrl;
            this.spidBaseUrl = spidBaseUrl;

            securityHelper = new SpidSecurityHelper(this.clientSignatureSecret);
            connectionClientFactory = new SpidUrlConnectionClientFactory();
        }

        public ClientBuilder autoRenew(boolean autoRenew) {
            this.autorenew = autorenew;
            return this;
        }

        public ClientBuilder autoRefresh(boolean autoRefresh) {
            this.autorefresh = autorefresh;
            return this;
        }

        public ClientBuilder autoDecryptSignedResponses(boolean autoDecryptSignedResponses) {
            this.autoDecryptSignedResponses = autoDecryptSignedResponses;
            return this;
        }

        public ClientBuilder connectionClientFactory(SpidConnectionClientFactory connectionClientFactory) {
            this.connectionClientFactory = connectionClientFactory;
            return this;
        }

        public SpidApiClient build() {
            return new SpidApiClient(this);
        }
    }
}
