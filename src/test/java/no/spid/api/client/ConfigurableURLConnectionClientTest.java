package no.spid.api.client;

import java.net.Proxy;

import no.spid.api.connection.ConfigHelper;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by marcin.godyn on 2015-08-20.
 */
public class ConfigurableURLConnectionClientTest {

    public static final String PROXY_HOST = "222.39.8.103";
    public static final String PROXY_PORT = "8118";

    @Before
    public void setUp() {
        System.setProperty("https.proxyHost", PROXY_HOST);
        System.setProperty("https.proxyPort", PROXY_PORT);
    }

    @Test
    public void shouldSetupProxy() {
        // given
        ConfigurableURLConnectionClient client = new ConfigurableURLConnectionClient();
        // when
        Proxy proxy = ConfigHelper.getProxy();
        // then
        Assert.assertEquals(proxy.address().toString(), "/" + PROXY_HOST + ":" + PROXY_PORT);

    }
}
