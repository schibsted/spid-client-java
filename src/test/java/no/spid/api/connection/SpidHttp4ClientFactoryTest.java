package no.spid.api.connection;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.net.InetSocketAddress;
import java.util.Map;

import org.apache.http.conn.ConnectTimeoutException;
import org.apache.oltu.oauth2.client.HttpClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthClientResponse;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class SpidHttp4ClientFactoryTest {

    private static final String URL_WHICH_WILL_TIMEOUT = "https://192.168.254.1/";
    private static final int CONNECT_TIMEOUT = 1000;
    private static final int READ_TIMEOUT = 1;
    private static final int PROXY_PORT = 3128;
    private static final String PROXY_ADDRESS = "w3cache.tpnet.pl";
    private static final int MARGIN = 100;
    private SpidHttp4ClientFactory factory;
    @Mock
    private SpidClientConfig config;

    @Before
    public void setUp() {
        initMocks(this);
        when(config.getProxyPort()).thenReturn(PROXY_PORT);
        when(config.getProxyInetAddress()).thenReturn(new InetSocketAddress(PROXY_ADDRESS, PROXY_PORT).getAddress());
        when(config.getReadTimeout()).thenReturn(READ_TIMEOUT);
        when(config.getMaxConnections()).thenReturn(1);
        when(config.getConnectTimeout()).thenReturn(CONNECT_TIMEOUT);
        factory = new SpidHttp4ClientFactory(config);
    }

    @Test(timeout = 3000)
    public void shouldTimeoutConnectionToHost() throws OAuthSystemException, OAuthProblemException {
        // given
        HttpClient client = factory.getClient();
        OAuthClientRequest request = new OAuthClientRequest.AuthenticationRequestBuilder(URL_WHICH_WILL_TIMEOUT)
                .buildQueryMessage();
        Map<String, String> headers = null;
        String requestMethod = null;
        Class<OAuthClientResponse> responseClass = null;
        // when
        long start = System.currentTimeMillis();
        try {
            client.execute(request, headers, requestMethod, responseClass);
        } catch (OAuthSystemException e) {
            if (!(e.getCause() instanceof ConnectTimeoutException)) {
                throw e;
            }
        }
        long end = System.currentTimeMillis();
        // then
        assertTrue(end - start < CONNECT_TIMEOUT + MARGIN);
        assertTrue(end - start > CONNECT_TIMEOUT - MARGIN);

    }

}
