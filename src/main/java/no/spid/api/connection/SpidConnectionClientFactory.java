package no.spid.api.connection;

import org.apache.oltu.oauth2.client.HttpClient;

/**
 * Used to feed the SpidApiClient with http clients to connect to the API
 */
public interface SpidConnectionClientFactory {
    public HttpClient getClient();
}
