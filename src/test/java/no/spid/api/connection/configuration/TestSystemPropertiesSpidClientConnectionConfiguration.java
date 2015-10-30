package no.spid.api.connection.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Properties;

import org.junit.Test;

public class TestSystemPropertiesSpidClientConnectionConfiguration {

    private Properties props = new Properties();

    @Test
    public void shouldReturnDefaultConnectionTimeout() throws Exception {
        // given

        // when
        int connectionTimeout = new SystemPropertiesSpidClientConnectionConfiguration(props).getConnectionTimeout();
        // then
        assertEquals(SystemPropertiesSpidClientConnectionConfiguration.DEFAULT_CONNECT_TIMEOUT, connectionTimeout);
    }

    @Test
    public void shouldReturnCustomizedConnectionTimeout() throws Exception {
        // given
        props.setProperty(SystemPropertiesSpidClientConnectionConfiguration.SPID_CONNECTION_TIMEOUT, "20");
        // when
        int connectionTimeout = new SystemPropertiesSpidClientConnectionConfiguration(props).getConnectionTimeout();
        // then
        assertEquals(20, connectionTimeout);
    }

    @Test
    public void shouldReturnDefaultReadTimeout() throws Exception {
        // given

        // when
        int readTimeout = new SystemPropertiesSpidClientConnectionConfiguration(props).getReadTimeout();
        // then
        assertEquals(SystemPropertiesSpidClientConnectionConfiguration.DEFAULT_READ_TIMEOUT, readTimeout);
    }

    @Test
    public void shouldReturnCustomizedReadTimeout() throws Exception {
        // given
        props.setProperty(SystemPropertiesSpidClientConnectionConfiguration.SPID_READ_TIMEOUT, "21");
        // when
        int readTimeout = new SystemPropertiesSpidClientConnectionConfiguration(props).getReadTimeout();
        // then
        assertEquals(21, readTimeout);
    }

    @Test
    public void shouldReturnDefaultMaxConnections() throws Exception {
        // given

        // when
        int maxConnections = new SystemPropertiesSpidClientConnectionConfiguration(props).getMaxConnections();
        // then
        assertEquals(SystemPropertiesSpidClientConnectionConfiguration.DEFAULT_MAX_CONNECTIONS, maxConnections);
    }

    @Test
    public void shouldReturnCustomizedMaxConnections() throws Exception {
        // given
        props.setProperty(SystemPropertiesSpidClientConnectionConfiguration.SPID_MAX_CONNECTIONS, "22");
        // when
        int maxConnections = new SystemPropertiesSpidClientConnectionConfiguration(props).getMaxConnections();
        // then
        assertEquals(22, maxConnections);
    }

    @Test
    public void shouldReturnDefaultMaxConnectionsPerRoute() throws Exception {
        // given

        // when
        int maxConnectionsPerRoute = new SystemPropertiesSpidClientConnectionConfiguration(props).getMaxConnectionsPerRoute();
        // then
        assertEquals(SystemPropertiesSpidClientConnectionConfiguration.DEFAULT_MAX_CONNECTIONS_PER_ROUTE, maxConnectionsPerRoute);
    }

    @Test
    public void shouldReturnCustomizedMaxConnectionsPerRoute() throws Exception {
        // given
        props.setProperty(SystemPropertiesSpidClientConnectionConfiguration.SPID_MAX_CONNECTIONS_PER_ROUTE, "23");
        // when
        int maxConnectionsPerRoute = new SystemPropertiesSpidClientConnectionConfiguration(props).getMaxConnectionsPerRoute();
        // then
        assertEquals(23, maxConnectionsPerRoute);
    }

    @Test
    public void shouldReturnDefaultProxyPort() throws Exception {
        // given

        // when
        int proxyPort = new SystemPropertiesSpidClientConnectionConfiguration(props).getProxyPort();
        // then
        assertEquals(SystemPropertiesSpidClientConnectionConfiguration.DEFAULT_PROXY_PORT, proxyPort);
    }

    @Test
    public void shouldReturnCustomizedProxyPort() throws Exception {
        // given
        props.setProperty(SystemPropertiesSpidClientConnectionConfiguration.SPID_PROXY_PORT, "24");
        // when
        int proxyPort = new SystemPropertiesSpidClientConnectionConfiguration(props).getProxyPort();
        // then
        assertEquals(24, proxyPort);
    }

    @Test
    public void shouldReturnNullWhenNoProxyDefined() throws Exception {
        // given

        // when
        String proxyHost = new SystemPropertiesSpidClientConnectionConfiguration(props).getProxyHost();
        // then
        assertNull(proxyHost);
    }

    @Test
    public void shouldReturnCustomizedProxyHost() throws Exception {
        // given
        props.setProperty(SystemPropertiesSpidClientConnectionConfiguration.SPID_PROXY_HOST, "name");
        // when
        String proxyHost = new SystemPropertiesSpidClientConnectionConfiguration(props).getProxyHost();
        // then
        assertEquals("name", proxyHost);
    }
}
