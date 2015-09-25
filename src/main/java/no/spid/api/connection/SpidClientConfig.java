package no.spid.api.connection;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;

public interface SpidClientConfig {

    int getConnectTimeout();

    int getReadTimeout();

    int getMaxConnections();

    Proxy getProxy();

    InetAddress getProxyInetAddress();

    int getProxyPort();

}