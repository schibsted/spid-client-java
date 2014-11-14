package no.spid.api.integration;

import no.spid.api.client.SpidApiClient;
import no.spid.api.exceptions.SpidApiException;
import no.spid.api.exceptions.SpidOAuthException;
import no.spid.api.oauth.SpidOAuthToken;
import org.junit.Test;

public class SpidApiClientIntegrationTest {

    //@Test
    public void addUser() {
        String redirectUrl = "http://localhost:8080";
        String spidBaseUrl = "http://spp.dev";

        try {
            SpidApiClient client = new SpidApiClient.ClientBuilder(
                    "<Client ID>",
                    "<Secret>",
                    "",
                    redirectUrl,
                    spidBaseUrl
            ).build();

            SpidOAuthToken token = client.getServerToken();
            String responseJSON = client.POST(token, "/user", null).getRawData();
            System.out.println(responseJSON);
        } catch (SpidOAuthException e) {
            e.printStackTrace();
        } catch (SpidApiException e) {
            e.printStackTrace();
        }
    }

    //@Test
    public void deletePaylink() {
        String redirectUrl = "http://localhost:8080";
        String spidBaseUrl = "http://spp.dev";

        try {
            SpidApiClient client = new SpidApiClient.ClientBuilder(
                    "<Client ID>",
                    "<Secret>",
                    "",
                    redirectUrl,
                    spidBaseUrl
            ).build();

            SpidOAuthToken token = client.getServerToken();
            String responseJSON = client.DELETE(token, "/paylink/288", null).getRawData();
            System.out.println(responseJSON);
        } catch (SpidOAuthException e) {
            e.printStackTrace();
        } catch (SpidApiException e) {
            e.printStackTrace();
        }
    }

}
