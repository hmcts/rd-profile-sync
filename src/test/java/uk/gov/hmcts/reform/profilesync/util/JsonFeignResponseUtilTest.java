package uk.gov.hmcts.reform.profilesync.util;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.type.TypeReference;
import feign.Response;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
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
import uk.gov.hmcts.reform.profilesync.domain.response.GetUserProfileResponse;
import uk.gov.hmcts.reform.profilesync.helper.MockDataProvider;

public class JsonFeignResponseUtilTest {
    private Response responseMock; //mocked as builder has private access
    private Response.Body bodyMock; //mocked as Body is an interface in Feign.Response
    private Reader readerMock; //mocked as it is an abstract class from Java.io
    final int statusCode = 200;

    @Before
    public void setUp() throws IOException {
        responseMock = mock(Response.class);
        bodyMock = mock(Response.Body.class);
        readerMock = mock(Reader.class);

        when(responseMock.body()).thenReturn(bodyMock);
        when(responseMock.body().asReader(Charset.defaultCharset())).thenReturn(readerMock);
        when(responseMock.status()).thenReturn(statusCode);
    }

    @Test
    public void testDecode() {
        JsonFeignResponseUtil.decode(responseMock, GetUserProfileResponse.class);
        ResponseEntity entity = JsonFeignResponseUtil.toResponseEntity(this.responseMock, GetUserProfileResponse.class);
        assertThat(entity).isNotNull();
    }

    @Test
    public void testToResponseEntity() {
        ResponseEntity actual = JsonFeignResponseUtil.toResponseEntity(this.responseMock, GetUserProfileResponse.class);
        assertThat(actual).isNotNull();
    }

    @Test
    public void testConvertHeaders() {
        Map<String, Collection<String>> data = new HashMap<>();
        Collection<String> list = asList("Authorization", MockDataProvider.AUTHORIZATION);
        data.put("MyHttpData", list);

        MultiValueMap<String, String> actual = JsonFeignResponseUtil.convertHeaders(data);

        assertThat(actual).isNotNull();
        assertThat(actual.get("MyHttpData")).isNotNull();
    }

    @Test
    public void testToResponseEntityThrowError() throws IOException {
        when(bodyMock.asReader(Charset.defaultCharset())).thenThrow(IOException.class);
        ResponseEntity actual = JsonFeignResponseUtil.toResponseEntity(this.responseMock,
                new TypeReference<List<IdamClient.User>>() {});
        assertThat(actual).isNotNull();
    }

    @Test
    public void testToResponseEntityThrowErrorDecode() throws IOException {
        when(bodyMock.asReader(Charset.defaultCharset())).thenThrow(IOException.class);
        Optional actual = JsonFeignResponseUtil.decode(this.responseMock, String.class);

        assertThat(actual.isPresent()).isFalse();
    }

    @Test
    public void privateConstructorTest() throws Exception {
        Constructor<JsonFeignResponseUtil> constructor = JsonFeignResponseUtil.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance((Object[]) null);
    }
}