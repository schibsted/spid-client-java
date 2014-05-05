package no.spid.api.security;

import org.junit.Test;

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
    public void testValidateSignedRequest() {
        //TODO
    }
}
