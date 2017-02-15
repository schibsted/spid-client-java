package no.spid.api.connection.client.apache;

import java.io.IOException;
import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthClientResponse;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.spid.api.connection.client.apache.helpers.HttpResponseProcessor;
import no.spid.api.connection.client.apache.helpers.HttpUriRequestBuilder;
import no.spid.api.connection.client.util.OAuthClientResponseFactoryWrapper;

public class ApacheHttpClient implements org.apache.oltu.oauth2.client.HttpClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApacheHttpClient.class);

    private CloseableHttpClient httpClient;

    private OAuthClientResponseFactoryWrapper oAuthClientResponseFactoryWrapper;

    private HttpUriRequestBuilder httpUriRequestBuilder;

    private HttpResponseProcessor httpResponseProcessor;

    public ApacheHttpClient(CloseableHttpClient httpClient, OAuthClientResponseFactoryWrapper oAuthClientResponseFactoryWrapper, HttpUriRequestBuilder httpUriRequestBuilder,
            HttpResponseProcessor httpResponseProcessor) {
        this.httpClient = httpClient;
        this.oAuthClientResponseFactoryWrapper = oAuthClientResponseFactoryWrapper;
        this.httpUriRequestBuilder = httpUriRequestBuilder;
        this.httpResponseProcessor = httpResponseProcessor;
    }

    public <T extends OAuthClientResponse> T execute(OAuthClientRequest request, Map<String, String> headers, String requestMethod, Class<T> responseClass)
            throws OAuthSystemException, OAuthProblemException {
        HttpUriRequest rq = httpUriRequestBuilder.buildHttpUriRequest(request, requestMethod, headers);
        return internalExecute(rq, responseClass);
    }

    private <T extends OAuthClientResponse> T internalExecute(HttpUriRequest rq, Class<T> responseClass) throws OAuthSystemException, OAuthProblemException {
        CloseableHttpResponse response = null;
        try {
            LOGGER.debug("Calling SPID with request...");
            response = httpClient.execute(rq);
            LOGGER.debug("Received response from SPID: {}", response);
            String body = httpResponseProcessor.getBody(response);
            String contentType = httpResponseProcessor.getContentType(response);
            return oAuthClientResponseFactoryWrapper.createCustomResponse(body, contentType, response.getStatusLine().getStatusCode(), responseClass);
        } catch (IOException e) {
            throw new OAuthSystemException("Unexpected exception while sending OAuth request", e);
        } catch (RuntimeException e) {
            throw new OAuthSystemException("Unexpected exception while sending OAuth request", e);
        } finally {
            closeResponseNoException(response);
        }
    }

    private void closeResponseNoException(CloseableHttpResponse response) {
        if (response != null) {
            try {
                response.close();
            } catch (IOException e) {
                LOGGER.error("Unable to close HttpResponse.", e);
            }
        }
    }

    public void shutdown() {
        try {
            httpClient.close();
        } catch (IOException e) {
            throw new RuntimeException("Error while closing HttpClient.", e);
        }
    }

}
