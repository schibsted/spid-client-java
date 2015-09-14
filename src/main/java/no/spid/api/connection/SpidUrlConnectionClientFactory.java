package no.spid.api.connection;

import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.oltu.oauth2.client.HttpClient;
import org.apache.oltu.oauth2.httpclient4.HttpClient4;

/**
 * Used to feed the SpidApiClient with URLConnectionClient's to connect to the API. It is using apache client.
 */
public class SpidUrlConnectionClientFactory implements SpidConnectionClientFactory {
    private static PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();

    public HttpClient getClient() {
        org.apache.http.client.HttpClient client = HttpClients.custom().setConnectionManager(connectionManager).build();
        HttpClient httpClient = new HttpClient4(client);
        return httpClient;
    }
}
