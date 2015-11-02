package no.spid.api.connection.configuration;

import java.util.Properties;

public class SystemPropertiesSpidClientConnectionConfiguration implements SpidClientConnectionConfiguration {

    public static final int DEFAULT_PROXY_PORT = 3128;
    public static final int DEFAULT_CONNECT_TIMEOUT = 6000;
    public static final int DEFAULT_READ_TIMEOUT = 6000;
    public static final int DEFAULT_MAX_CONNECTIONS_PER_ROUTE = 25;
    public static final int DEFAULT_MAX_CONNECTIONS = 50;

    public static final String SPID_PROXY_HOST = "https.proxyHost";
    public static final String SPID_PROXY_PORT = "https.proxyPort";
    public static final String SPID_READ_TIMEOUT = "spid.readTimeout";
    public static final String SPID_CONNECTION_TIMEOUT = "spid.connectionTimeout";
    public static final String SPID_MAX_CONNECTIONS = "spid.maxConnections";
    public static final String SPID_MAX_CONNECTIONS_PER_ROUTE = "spid.maxConnectionsPerRoute";

    private Properties properties;

    public SystemPropertiesSpidClientConnectionConfiguration() {
        this(System.getProperties());
    }

    // Constructor for testing. Allows to inject properties which simulate system properies.
    SystemPropertiesSpidClientConnectionConfiguration(Properties properties) {
        this.properties = properties;
    }

    public String getProxyHost() {
        return properties.getProperty(SPID_PROXY_HOST);
    }

    public int getProxyPort() {
        int port = getPropertyInt(SPID_PROXY_PORT, DEFAULT_PROXY_PORT);
        return port;
    }

    public int getMaxConnections() {
        return getPropertyInt(SPID_MAX_CONNECTIONS, DEFAULT_MAX_CONNECTIONS);
    }

    public int getMaxConnectionsPerRoute() {
        return getPropertyInt(SPID_MAX_CONNECTIONS_PER_ROUTE, DEFAULT_MAX_CONNECTIONS_PER_ROUTE);
    }

    public int getReadTimeout() {
        return getPropertyInt(SPID_READ_TIMEOUT, DEFAULT_READ_TIMEOUT);
    }

    public int getConnectionTimeout() {
        return getPropertyInt(SPID_CONNECTION_TIMEOUT, DEFAULT_CONNECT_TIMEOUT);
    }

    private int getPropertyInt(String key, int defaultValue) {
        return properties.getProperty(key) != null ? Integer.parseInt(properties.getProperty(key)) : defaultValue;
    }

}
