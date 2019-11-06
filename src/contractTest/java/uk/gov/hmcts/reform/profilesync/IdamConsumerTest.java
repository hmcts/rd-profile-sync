package uk.gov.hmcts.reform.profilesync;

import static org.assertj.core.api.Assertions.assertThat;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.dsl.PactDslJsonArray;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.model.RequestResponsePact;
import com.google.common.collect.Maps;
import groovy.util.logging.Slf4j;
import io.restassured.RestAssured;
import io.restassured.config.EncoderConfig;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import java.util.Map;
import net.serenitybdd.rest.SerenityRest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Slf4j
@ExtendWith(PactConsumerTestExt.class)
@ExtendWith(SpringExtension.class)
public class IdamConsumerTest {

    private static final String IDAM_DETAILS_URL = "/details";
    private static final String IDAM_OAUTH2_AUTHORIZE_URL = "/o/token";
    private static final String IDAM_OAUTH2_TOKEN_URL = "/o/token";

    private static final String CLIENT_REDIRECT_URI = "/oauth2redirect";
    private static final String IDAM_GET_USER_URL = "/api/v1/users";
    private static final String ACCESS_TOKEN = "111";

    @BeforeEach
    public void setUp() {
        RestAssured.defaultParser = Parser.JSON;
        RestAssured.config().encoderConfig(new EncoderConfig("UTF-8", "UTF-8"));
    }

    @Test
    @Pact(provider = "Idam_api", consumer = "rd_user_profile_api__Idam_api")
    public RequestResponsePact executeGetUserAndGet200Response(PactDslWithProvider builder) {

        Map<String, String> headers = Maps.newHashMap();
        headers.put(HttpHeaders.AUTHORIZATION, ACCESS_TOKEN);
        headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        PactDslJsonArray array = new PactDslJsonArray().stringValue("pui-case-manager");

        return builder
                .given("Idam successfully returns user details")
                .uponReceiving("Provider receives a GET /code request from an RD - USER PROF API")
                .path(IDAM_OAUTH2_AUTHORIZE_URL)
                .method(HttpMethod.POST.toString())
                .headers(headers)
                .willRespondWith()
                .status(HttpStatus.OK.value())
                .body(new PactDslJsonBody()
                        .stringType("code", "12345"))
                .toPact();

    }

    @Test
    @Pact(provider = "sidam_user_details", consumer = "rd_professional_api__sidam_user_details")
    public RequestResponsePact executeGetIdamAuthTokenAndGet200(PactDslWithProvider builder) {

        Map<String, String> headers = Maps.newHashMap();
        headers.put(HttpHeaders.AUTHORIZATION, ACCESS_TOKEN);
        headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);

        return builder
                .given("Idam successfully returns user details")
                .uponReceiving("Provider receives a GET /code request from an RD - REF DATA API")
                .path(IDAM_OAUTH2_TOKEN_URL)
                .headers(headers)
                .method(HttpMethod.POST.toString())
                .willRespondWith()
                .status(HttpStatus.OK.value())
                .body(new PactDslJsonBody()
                        .stringType("access_token", "some-long-access-token")
                        .stringType("token_type", "Bearer")
                        .stringType("expires_in", "28800"))
                .toPact();

    }

    @Test
    @Pact(provider = "sidam_user_details", consumer = "rd_professional_api__sidam_user_details")
    public RequestResponsePact executeGetUserDetailsAndGet200(PactDslWithProvider builder) {

        Map<String, String> headers = Maps.newHashMap();
        headers.put(HttpHeaders.AUTHORIZATION, ACCESS_TOKEN);

        return builder
                .given("Idam successfully returns user details")
                .uponReceiving("Provider receives a GET /details request from an RD - REF DATA API")
                .path(IDAM_GET_USER_URL)
                .method(HttpMethod.GET.toString())
                .headers(headers)
                .willRespondWith()
                .status(HttpStatus.OK.value())
                .body(createUserDetailsResponse())
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "executeGetUserDetailsAndGet200")
    public void should_get_user_details_with_access_token(MockServer mockServer) throws JSONException {

        Map<String, String> headers = Maps.newHashMap();
        headers.put(HttpHeaders.AUTHORIZATION, ACCESS_TOKEN);

        String actualResponseBody =
                SerenityRest
                        .given()
                        .headers(headers)
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .when()
                        .get(mockServer.getUrl() + IDAM_GET_USER_URL)
                        .then()
                        .statusCode(200)
                        .and()
                        .extract()
                        .body()
                        .asString();

        JSONObject response = new JSONObject(actualResponseBody);

        assertThat(actualResponseBody).isNotNull();
        assertThat(response).hasNoNullFieldsOrProperties();
        assertThat(response.getString("id")).isNotBlank();
        assertThat(response.getString("forename")).isEqualTo("Scotty");
        assertThat(response.getString("surname")).isEqualTo("2Hotty");

        JSONArray rolesArr = new JSONArray(response.getString("roles"));

        assertThat(rolesArr).isNotNull();
        assertThat(rolesArr.length()).isNotZero();
        assertThat(rolesArr.get(0).toString()).isEqualTo("pui-case-manager");

    }

    @Test
    @PactTestFor(pactMethod = "executeGetUserAndGet200Response")
    public void should_post_to_oauth2_authorize_and_receive_code_with_200_response(MockServer mockServer) throws JSONException {

        Map<String, String> headers = Maps.newHashMap();
        headers.put(HttpHeaders.AUTHORIZATION, ACCESS_TOKEN);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("response_type", "code");
        body.add("client_id", "ia");
        body.add("redirect_uri", CLIENT_REDIRECT_URI);

        String actualResponseBody =
                SerenityRest
                        .given()
                        .headers(headers)
                        .contentType(ContentType.URLENC)
                        .formParams(body)
                        .when()
                        .post(mockServer.getUrl() + IDAM_OAUTH2_AUTHORIZE_URL)
                        .then()
                        .statusCode(200)
                        .and()
                        .extract()
                        .asString();

        assertThat(actualResponseBody).isNotNull();

        JSONObject response = new JSONObject(actualResponseBody);
        assertThat(response.get("code").toString()).isEqualTo("12345");
    }

    @Test
    @PactTestFor(pactMethod = "executeGetIdamAuthTokenAndGet200")
    public void should_post_to_oauth2_token_and_receive_code_with_200_response(MockServer mockServer) throws JSONException {

        Map<String, String> headers = Maps.newHashMap();

        headers.put(HttpHeaders.AUTHORIZATION, ACCESS_TOKEN);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", "some-code");
        body.add("grant_type", "authorization_code");
        body.add("redirect_uri", CLIENT_REDIRECT_URI);
        body.add("client_id", "ia");
        body.add("client_secret", "some-client-secret");

        String actualResponseBody =

                SerenityRest
                        .given()
                        .headers(headers)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .formParams(body)
                        .log().all(true)
                        .when()
                        .post(mockServer.getUrl() + IDAM_OAUTH2_TOKEN_URL)
                        .then()
                        .statusCode(200)
                        .and()
                        .extract().response().body()
                        .asString();

        JSONObject response = new JSONObject(actualResponseBody);

        assertThat(response).isNotNull();
        assertThat(response.getString("access_token")).isNotBlank();
        assertThat(response.getString("token_type")).isEqualTo("Bearer");
        assertThat(response.getString("expires_in")).isNotBlank();

    }


    private PactDslJsonBody createUserDetailsResponse() {
        PactDslJsonArray array = new PactDslJsonArray().stringValue("pui-case-manager");

        return new PactDslJsonBody()
                .stringType("active", "true")
                .stringType("email", "emawbeweil@gmail.com")
                .stringType("forename", "Scotty")
                .stringType("id", "123456A")
                .stringType("lastModified", "now")
                .stringType("locked", "false")
                .stringType("pending", "false")
                .stringType("roles", array.toString())
                .stringType("surname", "2Hotty");
    }
}