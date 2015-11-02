package no.spid.api.connection.client.apache.helpers;

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpResponseProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpResponseProcessor.class);

    public String getContentType(CloseableHttpResponse response) {
        if (response.getEntity().getContentType() != null) {
            return response.getEntity().getContentType().toString();
        } else {
            LOGGER.warn("No content type information found in response. Assuming application/json");
            return "application/json";
        }
    }

    public String getBody(CloseableHttpResponse response) throws IOException, OAuthSystemException {
        if (response.getEntity() == null) {
            LOGGER.warn("No entity returned for request.");
            throw new OAuthSystemException("No data received.");
        }
        return EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8"));
    }

}
