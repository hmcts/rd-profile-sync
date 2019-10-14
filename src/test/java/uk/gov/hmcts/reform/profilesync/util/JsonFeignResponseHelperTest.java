package uk.gov.hmcts.reform.profilesync.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.type.TypeReference;
import feign.Response;

import java.io.IOException;
import java.io.Reader;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import uk.gov.hmcts.reform.profilesync.client.IdamClient;
import uk.gov.hmcts.reform.profilesync.domain.UserProfileResponse;
import uk.gov.hmcts.reform.profilesync.helper.MockDataProvider;

public class JsonFeignResponseHelperTest {
    final int statusCode = 200;
    private Response responseMock;

    @Before
    public void setUp() {
        responseMock = mock(Response.class);
        Reader readerMock = mock(Reader.class);
        Response.Body bodyMock = mock(Response.Body.class);

        try {
            when(responseMock.body()).thenReturn(bodyMock);
            when(responseMock.body().asReader()).thenReturn(readerMock);
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("Exception ex:" + ex);
        }

        when(responseMock.status()).thenReturn(statusCode);
    }

    @Test
    public void testDecode() {
        JsonFeignResponseHelper.decode(responseMock, UserProfileResponse.class);

        ResponseEntity entity = JsonFeignResponseHelper.toResponseEntity(this.responseMock, UserProfileResponse.class);

        assertThat(entity).isNotNull();
    }

    @Test
    public void testToResponseEntity() {
        ResponseEntity actual = JsonFeignResponseHelper.toResponseEntity(this.responseMock, UserProfileResponse.class);

        assertThat(actual).isNotNull();
    }

    @Test
    public void testConvertHeaders() {
        Map<String, Collection<String>> data = new HashMap<>();

        Collection<String> list = new ArrayList<>(Arrays.asList(new String[]{"Authorization",MockDataProvider.authorization}));

        data.put("MyHttpData", list);

        MultiValueMap<String, String> actual = JsonFeignResponseHelper.convertHeaders(data);

        assertThat(actual).isNotNull();
        assertThat(actual.get("MyHttpData")).isNotNull();
    }

    @Test
    public void testToResponseEntityThrowError() throws IOException {
        IOException ioException = mock(IOException.class);
        Response.Body bodyMock = mock(Response.Body.class);
        when(responseMock.body()).thenReturn(bodyMock);
        when(bodyMock.asReader()).thenThrow(IOException.class);
        ResponseEntity actual = JsonFeignResponseHelper.toResponseEntity(this.responseMock, new TypeReference<List<IdamClient.User>>(){});

        assertThat(actual).isNotNull();
    }

    @Test
    public void testToResponseEntityThrowErrorDecode() throws IOException {
        IOException ioException = mock(IOException.class);
        Response.Body bodyMock = mock(Response.Body.class);
        when(responseMock.body()).thenReturn(bodyMock);
        when(bodyMock.asReader()).thenThrow(IOException.class);
        Optional actual = JsonFeignResponseHelper.decode(this.responseMock, String.class);

        assertThat(actual.isPresent()).isFalse();
    }

    @Test
    public void privateConstructorTest() throws Exception {
        Constructor<JsonFeignResponseHelper> constructor = JsonFeignResponseHelper.class.getDeclaredConstructor();
        assertThat(constructor.isAccessible()).isFalse();
        constructor.setAccessible(true);
        constructor.newInstance((Object[]) null);
    }
}