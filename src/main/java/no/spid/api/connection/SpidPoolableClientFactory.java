package no.spid.api.connection;

import org.apache.oltu.oauth2.client.HttpClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;

/**
 * Used to feed the SpidApiClient with URLConnectionClient's to connect to the API
 */
public class SpidPoolableClientFactory implements SpidConnectionClientFactory{
    public HttpClient getClient() {
        return new URLConnectionClient();
    }
}
