package no.spid.api.connection;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;

public class SystemPropertiesSpidClientConfig implements SpidClientConfig {

    public static final String SPID_PROXY_HOST = "https.proxyHost";
    public static final String SPID_PROXY_PORT = "https.proxyPort";
    public static final String SPID_READ_TIMEOUT = "spid.readTimeout";
    public static final String SPID_CONNECT_TIMEOUT = "spid.connectTimeout";
    public static final String SPID_MAX_CONNECTION = "spid.maxConnection";

    private InetSocketAddress getProxySocketAddress() {
        String address = System.getProperty(SPID_PROXY_HOST);
        int port = getProxyPort();
        if (address != null) {
            return new InetSocketAddress(address, port);
        } else {
            return null;
        }
    }

    public int getProxyPort() {
        int port = System.getProperty(SPID_PROXY_PORT) != null ? Integer.parseInt(System.getProperty(SPID_PROXY_PORT)) : 3128;
        return port;
    }

    public InetAddress getProxyInetAddress() {
        if (getProxySocketAddress() != null) {
            return getProxySocketAddress().getAddress();
        }
        return null;
    }

    public Proxy getProxy() {
        InetSocketAddress socketAddress = getProxySocketAddress();
        if (socketAddress != null) {
            return new Proxy(Proxy.Type.HTTP, socketAddress);
        } else {
            return null;
        }
    }

    protected int getPropertyInt(String key, int defaultValue) {
        return System.getProperty(key) != null ? Integer.parseInt(System.getProperty(key)) : defaultValue;
    }

    public int getMaxConnections() {
        return getPropertyInt(SPID_MAX_CONNECTION, 25);

    }

    public int getReadTimeout() {
        return getPropertyInt(SPID_READ_TIMEOUT, 6000);
    }

    public int getConnectTimeout() {
        return getPropertyInt(SPID_CONNECT_TIMEOUT, 6000);
    }
}
