package no.spid.api.client;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SpidApiResponseTest {

    @Test
    public void getData() {
        SpidApiResponse response = new SpidApiResponse(200, null, "{\"name\":\"SPP Container\",\"version\":\"0.2\",\"api\":2,\"object\":\"Payment\",\"type\":\"element\",\"code\":200,\"data\":\"FOO\"}");

        assertEquals("FOO", response.getRawData());
        assertEquals("{\"name\":\"SPP Container\",\"version\":\"0.2\",\"api\":2,\"object\":\"Payment\",\"type\":\"element\",\"code\":200,\"data\":\"FOO\"}", response.getRawBody());
        assertEquals(200, response.getResponseCode());
        assertFalse(response.isEncrypted());
    }

    @Test
    public void readSignedData() {
        String rawBody = "{\"name\":\"SPP Container\",\"version\":\"0.2\",\"api\":2,\"data\":\"eyJwYXltZW50\",\"algorithm\":\"HMAC-SHA256\",\"sig\":\"jnx1U1lGAWJ7Biskboc6PNBtJsL3bpq_prfo9EDXd0Q\"}";
        SpidApiResponse response = new SpidApiResponse(200, null, rawBody);

        assertEquals("eyJwYXltZW50", response.getRawData());
        assertEquals(rawBody, response.getRawBody());
        assertEquals(200, response.getResponseCode());
        assertTrue(response.isEncrypted());

        response.setDecryptedData("FOO");
        assertFalse(response.isEncrypted());
    }

    @Test
    public void getJSONObject() throws Exception {
        SpidApiResponse response = new SpidApiResponse(200, null, "{\"name\":\"SPP Container\",\"version\":\"0.2\",\"api\":2,\"object\":\"Payment\",\"type\":\"element\",\"code\":200,\"data\":{\"name\":\"FOO\"}}");
        JSONObject json = response.getJsonData();

        assertEquals("FOO", json.getString("name"));
    }

    @Test
    public void getJSONArray() throws Exception {
        SpidApiResponse response = new SpidApiResponse(200, null, "{\"name\":\"SPP Container\",\"version\":\"0.2\",\"api\":2,\"object\":\"Payment\",\"type\":\"element\",\"code\":200,\"data\":[{\"name\":\"FOO\"},{\"name\":\"FOOBAR\"}]}");
        JSONArray json = response.getJsonArray();

        assertEquals(2, json.length());

        JSONObject object = json.getJSONObject(1);
        assertEquals(object.getString("name"), "FOOBAR");
    }
}
