package no.spid.api.connection;

import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.oltu.oauth2.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.spid.api.connection.client.apache.ApacheHttpClient;
import no.spid.api.connection.client.apache.helpers.HeadersCollector;
import no.spid.api.connection.client.apache.helpers.HttpResponseProcessor;
import no.spid.api.connection.client.apache.helpers.HttpUriRequestBuilder;
import no.spid.api.connection.client.util.OAuthClientResponseFactoryWrapper;
import no.spid.api.connection.configuration.SpidClientConnectionConfiguration;
import no.spid.api.connection.configuration.SystemPropertiesSpidClientConnectionConfiguration;

public class SpidApacheHttpClientFactory implements SpidConnectionClientFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpidApacheHttpClientFactory.class);

    private ApacheHttpClient apacheHttpClient;

    public SpidApacheHttpClientFactory() {
        this(new SystemPropertiesSpidClientConnectionConfiguration());
    }

    public SpidApacheHttpClientFactory(SpidClientConnectionConfiguration configuration) {
        RequestConfig requestConfig = buildRequestConfig(configuration);
        LOGGER.info("Building HttpClient with follwoing settings: {}", requestConfig.toString());

        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(buildConnectionManager(configuration))
                .build();

        this.apacheHttpClient = new ApacheHttpClient(httpClient,
                new OAuthClientResponseFactoryWrapper(),
                new HttpUriRequestBuilder(new HeadersCollector()),
                new HttpResponseProcessor());

        LOGGER.info("Setup of Apache HTTP Client completed.");
    }

    private PoolingHttpClientConnectionManager buildConnectionManager(SpidClientConnectionConfiguration configuration) {
        LOGGER.info("Building PoolingHttpClientConnectionManager with MaxTotal {} and MaxPerRoute {}", configuration.getMaxConnections(),
                configuration.getMaxConnectionsPerRoute());
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(configuration.getMaxConnections());
        connectionManager.setDefaultMaxPerRoute(configuration.getMaxConnectionsPerRoute());
        return connectionManager;
    }

    private RequestConfig buildRequestConfig(SpidClientConnectionConfiguration configuration) {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(configuration.getReadTimeout())
                .setConnectTimeout(configuration.getConnectionTimeout())
                .setSocketTimeout(configuration.getReadTimeout())
                .setProxy(buildProxy(configuration))
                .build();
        return requestConfig;
    }

    private HttpHost buildProxy(SpidClientConnectionConfiguration configuration) {
        if (configuration.getProxyHost() != null) {
            return new HttpHost(configuration.getProxyHost(), configuration.getProxyPort());
        }
        return null;
    }

    public HttpClient getClient() {
        return apacheHttpClient;
    }

}
