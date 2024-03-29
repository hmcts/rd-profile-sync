package uk.gov.hmcts.reform.profilesync;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
class SmokeTest {

    private final String targetInstance =
            StringUtils.defaultIfBlank(
                    System.getenv("TEST_URL"),
                    "http://localhost:8093"
            );

    @Test
    void should_prove_app_is_running_and_healthy() {

        /*SerenityRest.proxy("proxyout.reform.hmcts.net", 8080);
        RestAssured.proxy("proxyout.reform.hmcts.net", 8080);*/

        RestAssured.baseURI = targetInstance;
        RestAssured.useRelaxedHTTPSValidation();

        Response response =
                RestAssured
                        .given()
                        .relaxedHTTPSValidation()
                        .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .get("/health")
                        .andReturn();
        log.info("Response::" + response);
        assertThat(response.getStatusCode()).isEqualTo(200);

    }
}
