package no.spid.api.connection.client.apache.helpers;

import java.io.ByteArrayInputStream;
import java.util.Map;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;

public class HttpUriRequestBuilder {

    private HeadersCollector headersRewriter;

    public HttpUriRequestBuilder(HeadersCollector headersRewriter) {
        this.headersRewriter = headersRewriter;
    }

    public HttpUriRequest buildHttpUriRequest(OAuthClientRequest request, String requestMethod, Map<String, String> headers) {
        if ("POST".equals(requestMethod)) {
            HttpPost postRequest = new HttpPost(request.getLocationUri());
            BasicHttpEntity entity = new BasicHttpEntity();
            entity.setContent(new ByteArrayInputStream(request.getBody().getBytes()));
            postRequest.setEntity(entity);
            postRequest.setHeaders(headersRewriter.getHeaders(request, headers));
            return postRequest;
        } else if ("GET".equals(requestMethod)) {
            HttpGet getRequest = new HttpGet(request.getLocationUri());
            getRequest.setHeaders(headersRewriter.getHeaders(request, headers));
            return getRequest;
        } else if ("DELETE".equals(requestMethod)) {
            HttpDelete deleteRequest = new HttpDelete(request.getLocationUri());
            deleteRequest.setHeaders(headersRewriter.getHeaders(request, headers));
            return deleteRequest;
        } else {
            throw new IllegalArgumentException("Only POST, GET and DELETE methods supported.");
        }
    }
}
