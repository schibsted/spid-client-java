package no.spid.api.connection.client.apache.helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicHeader;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TestHttpResponseProcessor {

    @Mock
    private CloseableHttpResponse response;

    @InjectMocks
    private HttpResponseProcessor processor;

    @Test(expected = OAuthSystemException.class)
    public void shouldThrowExceptionWhenNoContent() throws Exception {
        // given

        // when
        processor.getBody(response);
        // then
    }

    @Test
    public void shouldGetContentType() throws Exception {
        // given
        HttpEntity entity = mock(HttpEntity.class);
        when(entity.getContentType()).thenReturn(new BasicHeader("Content-Type", "application/json"));
        when(response.getEntity()).thenReturn(entity);
        // when
        String contentType = processor.getContentType(response);
        // then
        assertNotNull(contentType);
        assertEquals("Content-Type: application/json", contentType);
    }
}
