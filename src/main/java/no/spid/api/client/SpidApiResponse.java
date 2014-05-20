package no.spid.api.client;

import no.spid.api.exceptions.SpidApiException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

/**
 * A helper/wrapper class for API responses.
 */
public class SpidApiResponse {
    private Map<String, String> headers;
    private int responseCode;
    private String rawBody;
    private JSONObject jsonResponse = null;
    private Object data = null;

    /**
     * Construct an API response from a server response.
     *
     * @param responseCode http response code
     * @param headers http headers
     * @param rawBody body of the response
     */
    public SpidApiResponse(int responseCode, Map<String, String> headers, String rawBody) {
        this.responseCode = responseCode;
        this.headers = headers;
        this.rawBody = rawBody;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public int getResponseCode() {
        return responseCode;
    }

    /**
     * Returns the raw body received from the server
     *
     * @return raw body
     */
    public String getRawBody() {
        return rawBody;
    }

    /**
     * Check if the data received is an array
     *
     * @return true if array, false if not
     */
    public boolean isArray() {
        if ( data == null) {
            data = getJsonResponse().get("data");
        }

        return data instanceof JSONArray;
    }

    /**
     * Check if the data received is an array
     *
     * @return true if array, false if not
     */
    public boolean isObject() {
        if ( data == null) {
            data = getJsonResponse().get("data");
        }

        return data instanceof JSONObject;
    }

    /**
     * Use this to get a JSONArray representation of the response.
     * Will throw exception if data is not an array.
     *
     * @return JSONArray
     * @throws SpidApiException
     */
    public JSONArray getJsonArray() throws SpidApiException {
        if ( data == null) {
            data = getJsonResponse().get("data");
        }
        if ( data instanceof JSONObject) {
            throw new SpidApiException("The response was a JSON object.");
        }

        return (JSONArray)data;
    }

    /**
     * Use this to get a JSONObject representation of the response.
     * Will throw exception if data is not an object.
     *
     * @return JSONObject
     * @throws SpidApiException
     */
    public JSONObject getJsonData() throws SpidApiException {
        if ( data == null) {
            data = getJsonResponse().get("data");
        }
        if ( data instanceof JSONArray) {
            throw new SpidApiException("The response was a JSON array.");
        }

        return (JSONObject)data;
    }

    /**
     * Get response data field as string
     *
     * @return data
     */
    public String getRawData() throws SpidApiException {
        return isArray() ? getJsonArray().toString() : isObject() ? getJsonData().toString() : getJsonValue("data");
    }

    /**
     * Get the entire server response as a JSONObject
     *
     * @return JSONObject
     */
    public JSONObject getJsonResponse() {
        if ( jsonResponse == null) {
            jsonResponse = new JSONObject(rawBody);
        }

        return jsonResponse;
    }

    public String getResposeSignature() {
        return getJsonValue("sig");
    }

    private String getJsonValue(String name) {
        if (getJsonResponse() != null && getJsonResponse().has(name)) {
            return getJsonResponse().getString(name);
        } else {
            return null;
        }
    }

    public String getResponseAlgorithm() {
        return getJsonValue("algorithm");
    }

    /**
     * Update response with decrypted data.
     *
     * @param data decrypted data
     */
    public void setDecryptedData(String data) {
        getJsonResponse().remove("sig");
        getJsonResponse().remove("algorithm");
        getJsonResponse().put("data", data);
    }

    public boolean isEncrypted() {
        return getResposeSignature() != null && getResponseAlgorithm() != null;
    }
}
