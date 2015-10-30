package no.spid.api.connection.client.util;

import org.apache.oltu.oauth2.client.response.OAuthClientResponse;
import org.apache.oltu.oauth2.client.response.OAuthClientResponseFactory;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;

/**
 * Responsibility - wraps static invocations of {@link OAuthClientResponseFactory}
 * 
 * @author jan.gurda
 *
 */
public class OAuthClientResponseFactoryWrapper {
    public <T extends OAuthClientResponse> T createCustomResponse(String body, String contentType, int responseCode, Class<T> clazz)
            throws OAuthSystemException, OAuthProblemException {
        return OAuthClientResponseFactory.createCustomResponse(body, contentType, responseCode, clazz);
    }
}
