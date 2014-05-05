package no.spid.api.oauth;


import org.apache.oltu.oauth2.client.request.OAuthBearerClientRequest;
import org.apache.oltu.oauth2.common.OAuth;

import java.util.Map;

/**
 * Overloaded this to support the cases where SPiD does not conform to the final OAuth2 spec.
 * Namely the name of the bearer token(oauth_token instead of access_token).
 */
public class SpidOAuthBearerClientRequest extends OAuthBearerClientRequest {

    public SpidOAuthBearerClientRequest(String url) {
        super(url);
    }

    public OAuthBearerClientRequest addParameters( Map<String, String> parameters) {
        if ( parameters != null) {
            this.parameters.putAll(parameters);
        }
        return this;
    }

    public SpidOAuthBearerClientRequest setRedirectUrl(String redirectUri) {
        this.parameters.put(OAuth.OAUTH_REDIRECT_URI, redirectUri);
        return this;
    }

    @Override
    public SpidOAuthBearerClientRequest setAccessToken(String accessToken) {
        this.parameters.put(OAuth.OAUTH_TOKEN, accessToken);
        return this;
    }
}
