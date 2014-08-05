package no.spid.api.security;

import no.spid.api.exceptions.SpidApiException;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashMap;
import static org.junit.Assert.*;

public class SpidSecurityHelperTest {

    @Test
    public void testAddHash() throws Exception {
        SpidSecurityHelper securityHelper = new SpidSecurityHelper("SignatureSecret");
        HashMap<String,String> parameters = new HashMap<String, String>();
        parameters.put("parameter1", "value1");
        parameters.put("parameter2", "value2");
        parameters.put("parameter3", "value3");

        securityHelper.addHash(parameters);
        assertEquals(4, parameters.size());
        assertEquals("uga44w13afsHV2XG31aELoopALuzUF2yyRffxpecLl0", parameters.get("hash"));
    }

    @Test
    public void testCallbackSuccess() throws Exception {
        final String expectedMessageBody = "{\"object\":\"order\",\"entry\":[{\"orderId\":\"3176403\",\"changedFields\":\"status\",\"time\":\"2014-06-12 13:39:34\"}],\"algorithm\":\"HMAC-SHA256\"}";
        String body = "Go3nym6YCKGSkoGAmNaBfnoEpTgLWXQPwm2rwtvMe0A.eyJvYmplY3QiOiJvcmRlciIsImVudHJ5IjpbeyJvcmRlcklkIjoiMzE3NjQwMyIsImNoYW5nZWRGaWVsZHMiOiJzdGF0dXMiLCJ0aW1lIjoiMjAxNC0wNi0xMiAxMzozOTozNCJ9XSwiYWxnb3JpdGhtIjoiSE1BQy1TSEEyNTYifQ";
        SpidSecurityHelper securityHelper = new SpidSecurityHelper("payment");

        String data = securityHelper.decryptAndValidateSignedRequest(body);

        assertEquals(expectedMessageBody, data);
    }

    @Test(expected = SpidApiException.class)
    public void testCallbackFail() throws Exception {
        String body = "Go3nym6YCKGSkoGAmNaBfnoEpTgLWXQPwm2rwtvMe0A.eyJvYmplY3QiOiJvcmRlciIsImVudHJ5IjpbeyJvcmRlcklkIjoiMzE3NjQwMyIsImNoYW5nZWRGaWVsZHMiOiJzdGF0dXMiLCJ0aW1lIjoiMjAxNC0wNi0xMiAxMzozOTozNCJ9XSwiYWxnb3JpdGhtIjoiSE1BQy1TSEEyNTYifQ";
        SpidSecurityHelper securityHelper = new SpidSecurityHelper("WrongKey");

        String data = securityHelper.decryptAndValidateSignedRequest(body);
    }
}
