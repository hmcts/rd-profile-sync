package uk.gov.hmcts.reform.profilesync.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import feign.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;


@SuppressWarnings("unchecked")
@Slf4j
public class JsonFeignResponseUtil {
    private static final ObjectMapper json = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private JsonFeignResponseUtil() {

    }

    public static Optional decode(Response response, Class clazz) {
        try {
            return Optional.of(json.readValue(response.body().asReader(), clazz));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public static ResponseEntity toResponseEntity(Response response, Class clazz) {
        Optional payload = decode(response, clazz);

        return new ResponseEntity(
                payload.orElse(null),
                convertHeaders(response.headers()),
                HttpStatus.valueOf(response.status()));
    }

    public static ResponseEntity toResponseEntity(Response response, TypeReference reference) {
        Optional payload = Optional.empty();

        try {
            payload = Optional.of(json.readValue(response.body().asReader(), reference));

        } catch (IOException ex) {

            log.error("error while reading the body", ex);
        }

        return new ResponseEntity(
                payload.orElse(null),
                convertHeaders(response.headers()),
                HttpStatus.valueOf(response.status()));
    }

    public static MultiValueMap<String, String> convertHeaders(Map<String, Collection<String>> responseHeaders) {
        MultiValueMap<String, String> responseEntityHeaders = new LinkedMultiValueMap<>();
        responseHeaders.entrySet().stream().forEach(e ->
                responseEntityHeaders.put(e.getKey(), new ArrayList<>(e.getValue())));
        return responseEntityHeaders;
    }
}

