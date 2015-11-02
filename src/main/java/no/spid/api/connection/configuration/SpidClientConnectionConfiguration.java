package no.spid.api.connection.configuration;

public interface SpidClientConnectionConfiguration {

    int getConnectionTimeout();

    int getReadTimeout();

    int getMaxConnections();

    int getMaxConnectionsPerRoute();

    String getProxyHost();

    int getProxyPort();

}