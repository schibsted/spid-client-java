package no.spid.api.connection.client.apache.helpers;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;

public class HeadersCollector {

    public Header[] getHeaders(OAuthClientRequest request, Map<String, String> headers) {
        List<Header> result = new LinkedList<Header>();
        if (headers != null) {
            for (String key : headers.keySet()) {
                result.add(new BasicHeader(key, headers.get(key)));
            }
        }
        if (request.getHeaders() != null) {
            for (String key : request.getHeaders().keySet()) {
                result.add(new BasicHeader(key, request.getHeaders().get(key)));
            }
        }
        addContentTypeIfMissing(result);
        return result.toArray(new Header[0]);
    }

    private void addContentTypeIfMissing(List<Header> headers) {
        for (Header header : headers) {
            if (header.getName().equals("Content-Type")) {
                return;
            }
        }
        // Set application/x-www-form-urlencoded when no content info.
        headers.add(new BasicHeader("Content-Type", "application/x-www-form-urlencoded"));
    }
}
