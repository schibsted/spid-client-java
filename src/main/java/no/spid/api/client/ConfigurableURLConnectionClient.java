package no.spid.api.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import org.apache.oltu.oauth2.client.HttpClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthClientResponse;
import org.apache.oltu.oauth2.client.response.OAuthClientResponseFactory;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.utils.OAuthUtils;

/**
 * Created by marcin.godyn on 2015-08-18.
 * <p>
 * Based on URKConnectionClient from apache oltu library.
 */
public class ConfigurableURLConnectionClient implements HttpClient {

    public static final String SPID_PROXY_HOST = "https.proxyHost";
    public static final String SPID_PROXY_PORT = "https.proxyPort";
    public static final String SPID_READ_TIMEOUT = "spid.readTimeout";
    public static final String SPID_CONNECT_TIMEOUT = "spid.connectTimeout";
    private int readTimeout = getPropertyInt(SPID_READ_TIMEOUT);
    private int connectTimeout = getPropertyInt(SPID_CONNECT_TIMEOUT);
    private Proxy proxy = getProxy();

    public ConfigurableURLConnectionClient() {
    }

    public <T extends OAuthClientResponse> T execute(OAuthClientRequest request, Map<String, String> headers,
            String requestMethod, Class<T> responseClass)
            throws OAuthSystemException, OAuthProblemException {

        String responseBody = null;
        URLConnection c = null;
        int responseCode = 0;
        try {
            URL url = new URL(request.getLocationUri());

            if (proxy == null) {
                c = url.openConnection();
            } else {
                c = url.openConnection(proxy);
            }

            c.setReadTimeout(readTimeout);
            c.setConnectTimeout(connectTimeout);
            responseCode = -1;
            if (c instanceof HttpURLConnection) {
                HttpURLConnection httpURLConnection = (HttpURLConnection) c;

                if (headers != null && !headers.isEmpty()) {
                    for (Map.Entry<String, String> header : headers.entrySet()) {
                        httpURLConnection.addRequestProperty(header.getKey(), header.getValue());
                    }
                }

                if (request.getHeaders() != null) {
                    for (Map.Entry<String, String> header : request.getHeaders().entrySet()) {
                        httpURLConnection.addRequestProperty(header.getKey(), header.getValue());
                    }
                }

                if (!OAuthUtils.isEmpty(requestMethod)) {
                    httpURLConnection.setRequestMethod(requestMethod);
                    if (requestMethod.equals(OAuth.HttpMethod.POST)) {
                        httpURLConnection.setDoOutput(true);
                        OutputStream ost = httpURLConnection.getOutputStream();
                        PrintWriter pw = new PrintWriter(ost);
                        pw.print(request.getBody());
                        pw.flush();
                        pw.close();
                    }
                } else {
                    httpURLConnection.setRequestMethod(OAuth.HttpMethod.GET);
                }

                httpURLConnection.connect();

                InputStream inputStream;
                responseCode = httpURLConnection.getResponseCode();
                if (responseCode == 400 || responseCode == 401) {
                    inputStream = httpURLConnection.getErrorStream();
                } else {
                    inputStream = httpURLConnection.getInputStream();
                }

                responseBody = OAuthUtils.saveStreamAsString(inputStream);
            }
        } catch (IOException e) {
            throw new OAuthSystemException(e);
        }

        return OAuthClientResponseFactory
                .createCustomResponse(responseBody, c.getContentType(), responseCode, responseClass);
    }

    public void shutdown() {
        // Nothing to do here
    }

    protected Proxy getProxy() {
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

}
