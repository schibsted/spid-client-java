package no.spid.api.connection;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.oltu.oauth2.client.HttpClient;
import org.junit.Before;
import org.junit.Test;

import no.spid.api.connection.client.apache.ApacheHttpClient;
import no.spid.api.connection.configuration.SystemPropertiesSpidClientConnectionConfiguration;

public class TestSpidApacheHttpClientFactory {

    @Before
    public void setup() throws Exception {
        System.setProperty(SystemPropertiesSpidClientConnectionConfiguration.SPID_PROXY_HOST, "google.com");
    }

    @Test
    public void shouldConstructHttpClient() throws Exception {
        // given
        SpidApacheHttpClientFactory factory = new SpidApacheHttpClientFactory(new SystemPropertiesSpidClientConnectionConfiguration());
        // when
        HttpClient client = factory.getClient();
        // then
        assertNotNull(client);
        assertTrue(client instanceof ApacheHttpClient);
    }
}
