package no.spid.api.client;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.Proxy;


/**
 * Created by marcin.godyn on 2015-08-20.
 */
public class ConfigurableURLConnectionClientTest {

    public static final String PROXY_HOST = "222.39.8.103";
    public static final String PROXY_PORT = "8118";

    @Before
    public void setUp(){
        System.setProperty("https.proxyHost", PROXY_HOST);
        System.setProperty("https.proxyPort", PROXY_PORT);
    }

    @Test
    public void shouldSetupProxy(){
        //given
        ConfigurableURLConnectionClient client = new ConfigurableURLConnectionClient();
        //when
        Proxy proxy = client.getProxy();
        //then
        Assert.assertEquals(proxy.address().toString(), "/" + PROXY_HOST + ":" + PROXY_PORT);

    }
}
