package no.spid.api.connection;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;

public class ConfigHelper {

    public static final String SPID_PROXY_HOST = "https.proxyHost";
    public static final String SPID_PROXY_PORT = "https.proxyPort";
    public static final String SPID_READ_TIMEOUT = "spid.readTimeout";
    public static final String SPID_CONNECT_TIMEOUT = "spid.connectTimeout";
    public static final String SPID_MAX_CONNECTION = "spid.maxConnection";

    private static InetSocketAddress getProxySocketAddress() {
        String address = System.getProperty(SPID_PROXY_HOST);
        int port = getProxyPort();
        if (address != null) {
            return new InetSocketAddress(address, port);
        } else {
            return null;
        }
    }

    public static int getProxyPort() {
        int port = System.getProperty(SPID_PROXY_PORT) != null ? Integer.parseInt(System.getProperty(SPID_PROXY_PORT)) : 3128;
        return port;
    }

    public static InetAddress getProxyInetAddress() {
        if (getProxySocketAddress() != null) {
            return getProxySocketAddress().getAddress();
        }
        return null;
    }

    public static Proxy getProxy() {
        InetSocketAddress socketAddress = getProxySocketAddress();
        if (socketAddress != null) {
            return new Proxy(Proxy.Type.HTTP, socketAddress);
        } else {
            return null;
        }
    }

    protected static int getPropertyInt(String key, int defaultValue) {
        return System.getProperty(key) != null ? Integer.parseInt(System.getProperty(key)) : defaultValue;
    }

    public static int getMaxConnections() {
        return getPropertyInt(SPID_MAX_CONNECTION, 25);

    }

    public static int getReadTimeout() {
        return getPropertyInt(SPID_READ_TIMEOUT, 6000);
    }

    public static int getConnectTimeout() {
        return getPropertyInt(SPID_CONNECT_TIMEOUT, 6000);
    }
}
