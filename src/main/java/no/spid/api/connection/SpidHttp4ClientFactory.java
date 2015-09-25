package no.spid.api.connection;

import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.oltu.oauth2.client.HttpClient;
import org.apache.oltu.oauth2.httpclient4.HttpClient4;

/**
 * Used to feed the SpidApiClient with URLConnectionClient's to connect to the API. It is using apache client.
 */
public class SpidHttp4ClientFactory implements SpidConnectionClientFactory {
    private final PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
    private SpidClientConfig config;

    public SpidHttp4ClientFactory() {
        this(new SystemPropertiesSpidClientConfig());
    }

    public SpidHttp4ClientFactory(SpidClientConfig config) {
        this.config = config;
        connectionManager.setMaxTotal(config.getMaxConnections());
        connectionManager.setDefaultMaxPerRoute(config.getMaxConnections());
    }

    public HttpClient getClient() {
        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
        requestConfigBuilder.setConnectTimeout(config.getConnectTimeout())
                .setConnectionRequestTimeout(config.getConnectTimeout())
                .setSocketTimeout(config.getReadTimeout());
        if (config.getProxyInetAddress() != null) {
            HttpHost proxy = new HttpHost(config.getProxyInetAddress(), config.getProxyPort());
            requestConfigBuilder.setProxy(proxy);
        }
        RequestConfig requestConfig = requestConfigBuilder.build();
        org.apache.http.client.HttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(connectionManager)
                .build();

        HttpClient httpClient = new HttpClient4(client);
        return httpClient;
    }
}
