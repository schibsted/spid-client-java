package no.spid.api.connection;

import java.net.InetSocketAddress;
import java.net.Proxy;

public class ConfigHelper {

    public static final String SPID_PROXY_HOST = "https.proxyHost";
    public static final String SPID_PROXY_PORT = "https.proxyPort";
    public static final String SPID_READ_TIMEOUT = "spid.readTimeout";
    public static final String SPID_CONNECT_TIMEOUT = "spid.connectTimeout";

    public static Proxy getProxy() {
        String address = System.getProperty(SPID_PROXY_HOST);
        int port = System.getProperty(SPID_PROXY_PORT) != null ? Integer.parseInt(System.getProperty(SPID_PROXY_PORT)) : 3128;
        if (address != null) {
            return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(address, port));
        } else {
            return null;
        }
    }

    protected static int getPropertyInt(String key) {
        return System.getProperty(key) != null ? Integer.parseInt(System.getProperty(key)) : 6000;
    }

    public static int getReadTimeout() {
        return getPropertyInt(SPID_READ_TIMEOUT);
    }

    public static int getConnectTimeout() {
        return getPropertyInt(SPID_CONNECT_TIMEOUT);
    }
}
