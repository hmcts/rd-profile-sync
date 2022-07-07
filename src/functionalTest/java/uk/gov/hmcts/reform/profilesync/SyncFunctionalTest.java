package uk.gov.hmcts.reform.profilesync;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

class SyncFunctionalTest {

    private final String targetInstance =
            StringUtils.defaultIfBlank(
                    System.getenv("TEST_URL"),
                    "http://localhost:8093"
            );


    @Test
    void testAppRunningHealthy() {
        RestAssured.baseURI = targetInstance;
        RestAssured.useRelaxedHTTPSValidation();

        Response response =
                RestAssured
                        .given()
                        .relaxedHTTPSValidation()
                        .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .get("/health")
                        .andReturn();
        assertThat(response.getStatusCode()).isEqualTo(200);
    }
}
