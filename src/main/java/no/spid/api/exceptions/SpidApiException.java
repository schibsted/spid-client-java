package no.spid.api.exceptions;

import no.spid.api.client.SpidApiResponse;

/**
 * Custom exception thrown when the API does not return 2xx.
 * The original response can be read with the getResponseBody, getResponseCode methods.
 */
public class SpidApiException extends Exception {
    private Integer responseCode;
    private String responseBody;

    public SpidApiException(String s) {
        super(s);
    }

    public SpidApiException(Throwable throwable) {
        super(throwable);
    }

    public SpidApiException(String s, SpidApiResponse spidResponse) {
        super(s);
        this.responseCode = spidResponse.getResponseCode();
        this.responseBody = spidResponse.getRawBody();
    }

    public Integer getResponseCode() {
        return responseCode;
    }

    public String getResponseBody() {
        return responseBody;
    }
}