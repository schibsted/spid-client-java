package no.spid.api.security;

import no.spid.api.client.SpidApiResponse;
import no.spid.api.exceptions.SpidApiException;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Map;

/**
 * Helper functions for signed communication.
 */
public class SpidSecurityHelper {
    private String signatureSecret;

    public SpidSecurityHelper(String signatureSecret) {
        this.signatureSecret = signatureSecret;
    }

    /**
     * Validates the signature on a signed request. (Callbacks)
     *
     * @param request the request to validate
     * @return the message if the signature validates
     * @throws SpidApiException
     */
    public String decryptAndValidateSignedRequest(String request) throws SpidApiException {
        if (request == null || !request.contains(".")) {
            throw new SpidApiException("Invalid request.");
        }

        String callbackSignature = request.split("\\.")[0];
        String callbackPayload = request.split("\\.")[1];

        byte[] callbackSignatureBytes = base64UrlDecode(callbackSignature);
        byte[] generatedSignature = null;

        try {
            SecretKeySpec sks = new SecretKeySpec(signatureSecret.getBytes("UTF-8"), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(sks);
            generatedSignature = mac.doFinal(callbackPayload.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException nsae) {
            throw new SpidApiException(nsae);
        } catch (InvalidKeyException ike) {
            throw new SpidApiException(ike);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (!Arrays.equals(generatedSignature, callbackSignatureBytes))
            throw new SpidApiException("Signature is not valid!");

        return new String(base64UrlDecode(callbackPayload));

    }

    /**
     * Validates the signature in the response, if its validates it updates the response with decoded data and removes
     * encryption data.
     *
     * @param spidResponse the signed response
     * @return a validated and decoded response object
     * @throws SpidApiException
     */
    public SpidApiResponse decryptAndValidateSignedResponse(SpidApiResponse spidResponse) throws SpidApiException {
        String b64signature = spidResponse.getResponseSignture();
        String algorithm = spidResponse.getResponseAlgorithm();
        String b64data = spidResponse.getRawData();

        if (algorithm != null && !algorithm.equals("HMAC-SHA256")) {
            throw new SpidApiException("Hash algorithm not supported. Expected HMAC-SHA256");
        }

        byte[] digest;
        Mac mac;
        SecretKeySpec sks;

        try {
            sks = new SecretKeySpec(signatureSecret.getBytes("UTF-8"), "HmacSHA256");
            mac = Mac.getInstance("HmacSHA256");
            mac.init(sks);
            digest = mac.doFinal(b64data.getBytes("UTF-8"));
        }
        catch (NoSuchAlgorithmException nsae) {
            throw new SpidApiException(nsae.getMessage());
        }
        catch (InvalidKeyException ike) {
            throw new SpidApiException(ike.getMessage());
        }
        catch (Exception e) {
            throw new SpidApiException(e.getMessage());
        }

        byte[] signature = base64UrlDecode(b64signature);

        if(!Arrays.equals(digest, signature))
            throw new SpidApiException("Could not verify signature");

        byte[] data = base64UrlDecode(b64data);

        spidResponse.setDecryptedData(new String(data));
        return spidResponse;
    }

    /**
     * Generates a hash and adds it to the parameter map.
     *
     * @param parameters the parameters to generate hash for
     * @return the initial map with the hash added
     * @throws SpidApiException
     */
    public Map<String,String> addHash(Map<String,String> parameters) throws SpidApiException {
        Mac mac;
        SecretKeySpec sks;
        String parameterString = "", hash;

        String[] keys = parameters.keySet().toArray(new String[parameters.keySet().size()]);
        Arrays.sort(keys);

        for (String key : keys) {
            parameterString += parameters.get(key);
        }

        try {
            sks = new SecretKeySpec(signatureSecret.getBytes("UTF-8"), "HmacSHA256");
            mac = Mac.getInstance("HmacSHA256");
            mac.init(sks);
            byte[] digest = mac.doFinal(parameterString.getBytes("UTF-8"));
            hash =  base64UrlEncode(digest);
        }
        catch (NoSuchAlgorithmException nsae) {
            throw new SpidApiException(nsae.getMessage());
        }
        catch (InvalidKeyException ike) {
            throw new SpidApiException(ike.getMessage());
        }
        catch (Exception e) {
            throw new SpidApiException(e.getMessage());
        }

        parameters.put("hash", hash);
        return parameters;
    }

    private byte[] base64UrlDecode(String str) {
        return Base64.decodeBase64(str.replace("-", "+").replace("_", "/").trim());
    }

    private String base64UrlEncode(byte[] bytes) {
        return Base64.encodeBase64URLSafeString(bytes).replace("+","-").replace("/","_");
    }
}
