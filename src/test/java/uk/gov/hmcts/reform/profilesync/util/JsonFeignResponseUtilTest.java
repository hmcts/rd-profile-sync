package uk.gov.hmcts.reform.profilesync.util;

import com.fasterxml.jackson.core.type.TypeReference;
import feign.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import uk.gov.hmcts.reform.profilesync.client.IdamClient;
import uk.gov.hmcts.reform.profilesync.domain.response.GetUserProfileResponse;
import uk.gov.hmcts.reform.profilesync.helper.MockDataProvider;

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

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JsonFeignResponseUtilTest {
    private Response responseMock; //mocked as builder has private access
    private Response.Body bodyMock; //mocked as Body is an interface in Feign.Response
    final int statusCode = 200;

    @BeforeEach
    public void setUp() throws IOException {
        responseMock = mock(Response.class);
        bodyMock = mock(Response.Body.class);
        //mocked as it is an abstract class from Java.io
        Reader readerMock = mock(Reader.class);

        when(responseMock.body()).thenReturn(bodyMock);
        when(responseMock.body().asReader(Charset.defaultCharset())).thenReturn(readerMock);
        when(responseMock.status()).thenReturn(statusCode);
    }

    @Test
    void testDecode() {
        JsonFeignResponseUtil.decode(responseMock, GetUserProfileResponse.class);
        ResponseEntity<Object> entity =
                JsonFeignResponseUtil.toResponseEntity(this.responseMock, GetUserProfileResponse.class);
        assertThat(entity).isNotNull();
    }

    @Test
    void testToResponseEntity() {
        ResponseEntity<Object> actual =
                JsonFeignResponseUtil.toResponseEntity(this.responseMock, GetUserProfileResponse.class);
        assertThat(actual).isNotNull();
    }

    @Test
    void testConvertHeaders() {
        Map<String, Collection<String>> data = new HashMap<>();
        Collection<String> list = asList("Authorization", MockDataProvider.AUTHORIZATION);
        data.put("MyHttpData", list);

        MultiValueMap<String, String> actual = JsonFeignResponseUtil.convertHeaders(data);

        assertThat(actual).isNotNull();
        assertThat(actual.get("MyHttpData")).isNotNull();
    }

    @Test
    void testToResponseEntityThrowError() throws IOException {
        when(bodyMock.asReader(Charset.defaultCharset())).thenThrow(IOException.class);
        ResponseEntity<Object> actual = JsonFeignResponseUtil.toResponseEntity(this.responseMock,
                new TypeReference<List<IdamClient.User>>() {
                });
        assertThat(actual).isNotNull();
    }

    @Test
    void testToResponseEntityThrowErrorDecode() throws IOException {
        when(bodyMock.asReader(Charset.defaultCharset())).thenThrow(IOException.class);
        Optional<Object> actual = JsonFeignResponseUtil.decode(this.responseMock, String.class);

        assertThat(actual).isNotPresent();
    }

    @Test
    void privateConstructorTest() throws Exception {
        Constructor<JsonFeignResponseUtil> constructor = JsonFeignResponseUtil.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance((Object[]) null);
    }
}
