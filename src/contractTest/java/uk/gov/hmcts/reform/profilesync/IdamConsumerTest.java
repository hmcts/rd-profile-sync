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
    private static final String ACCESS_TOKEN = "eyJ0eXAiOiJKV1QiLCJ6aXAiOiJOT05FIiwia2lkIjoiRm8rQXAybThDT3ROb290ZjF4TWg0bGc3MFlBPSIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJmcmVnLXRlc3QtdXNlci1ZOHlqVURSeWpyQGZlZW1haWwuY29tIiwiYXV0aF9sZXZlbCI6MCwiYXVkaXRUcmFja2luZ0lkIjoiYTU2MDliYjYtYzEzYi00MjQ0LTg3ODItNDNmZGViMDZlMDBjIiwiaXNzIjoiaHR0cHM6Ly9mb3JnZXJvY2stYW0uc2VydmljZS5jb3JlLWNvbXB1dGUtaWRhbS1hYXQuaW50ZXJuYWw6ODQ0My9vcGVuYW0vb2F1dGgyL2htY3RzIiwidG9rZW5OYW1lIjoiYWNjZXNzX3Rva2VuIiwidG9rZW5fdHlwZSI6IkJlYXJlciIsImF1dGhHcmFudElkIjoiYWNjNmUyYTAtMWExYi00OGM3LWJmZGItNzI1NjllM2E1NjkzIiwiYXVkIjoicmQtcHJvZmVzc2lvbmFsLWFwaSIsIm5iZiI6MTU2OTQ0MTkxMSwiZ3JhbnRfdHlwZSI6ImF1dGhvcml6YXRpb25fY29kZSIsInNjb3BlIjpbIm9wZW5pZCIsInByb2ZpbGUiLCJyb2xlcyIsImNyZWF0ZS11c2VyIiwibWFuYWdlLXVzZXIiXSwiYXV0aF90aW1lIjoxNTY5NDQxOTExMDAwLCJyZWFsbSI6Ii9obWN0cyIsImV4cCI6MTU2OTQ1NjMxMSwiaWF0IjoxNTY5NDQxOTExLCJleHBpcmVzX2luIjoxNDQwMCwianRpIjoiY2Q5MWM0NjQtMzU0Zi00N2I2LTkwYTUtNWY2Y2U3NGUwYTY5In0.aLobAYYCxkmryzKV1stmag63h-ndxrDjO4462YERcLDIXVmvFJNXfdPRg9U8WGv0GkOrSkHVJ7tbdLQySnOVYulXkPl71g5MqU7ZuEQvHaBpfW9exBCfP-pw8kWyMUck-rB00tkEX7ZpS6euQM0WVbdczPnClxR3tWwktPfN-bCo6PPwqiMkC1DgTmjQBMtjgP1nEiJM7Kocqb2X3OCItf4lps1_nSG68jI98fwaLn8WQgk1sw9eebskChXDfpmIyreeGFWpHNpdFqOFfYEC5FnSgXHQw7Eu-hc5RofPZzKFrbwZHC31t5guK9Wq8zn9Xwe6743g4ozm3EHN8fsjVQ"; //todo

    @BeforeEach
    public void setUp() {
        RestAssured.defaultParser = Parser.JSON;
        RestAssured.config().encoderConfig(new EncoderConfig("UTF-8", "UTF-8"));
    }

    @Test
    @Pact(provider = "Idam_api", consumer = "rd_profile_sync__idam_api")
    public RequestResponsePact executeGetIdamAuthCodeAndGet200Response(PactDslWithProvider builder) {

        Map<String, String> headers = Maps.newHashMap();
        headers.put(HttpHeaders.AUTHORIZATION, ACCESS_TOKEN);
        headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);

        return builder
                .given("Idam returns the auth code ")
                .uponReceiving("Provider receives a POST /oauth2/authorize from an RD - PROFILE SYNC API")
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
    @PactTestFor(pactMethod = "executeGetIdamAuthCodeAndGet200Response")
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
        assertThat(response.get("code").toString()).isNotBlank();
    }

    @Test
    @Pact(provider = "Idam_api", consumer = "rd_profile_sync__idam_api")
    public RequestResponsePact executeGetIdamAuthTokenAndGet200(PactDslWithProvider builder) {

        Map<String, String> headers = Maps.newHashMap();
        headers.put(HttpHeaders.AUTHORIZATION, ACCESS_TOKEN);
        headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);

        return builder
                .given("Idam successfully returns tokens")
                .uponReceiving("Provider receives a POST /o/token request from an RD - PROFILE SYNC API")
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

    @Test
    @Pact(provider = "Idam_api", consumer = "rd_profile_sync__idam_api")
    public RequestResponsePact executeGetUserDetailsAndGet200(PactDslWithProvider builder) {

        Map<String, String> headers = Maps.newHashMap();
        headers.put(HttpHeaders.AUTHORIZATION, ACCESS_TOKEN);

        return builder
                .given("Idam successfully returns user details")
                .uponReceiving("Provider receives a GET /details request from an RD - PROFILE SYNC API")
                .path(IDAM_DETAILS_URL)
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
                        .get(mockServer.getUrl() + IDAM_DETAILS_URL)
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
        assertThat(response.getString("forename")).isNotBlank();
        assertThat(response.getString("surname")).isNotBlank();

        JSONArray rolesArr = new JSONArray(response.getString("roles"));

        assertThat(rolesArr).isNotNull();
        assertThat(rolesArr.length()).isNotZero();
        assertThat(rolesArr.get(0).toString()).isNotBlank();

    }

    @Test
    @Pact(provider = "Idam_api", consumer = "rd_profile_sync__idam_api")
    public RequestResponsePact executeGetUserAndGet200(PactDslWithProvider builder) {

        Map<String, String> headers = Maps.newHashMap();
        headers.put(HttpHeaders.AUTHORIZATION, ACCESS_TOKEN);

        return builder
                .given("Idam successfully returns user")
                .uponReceiving("Provider receives a GET /api/v1/users request from an RD - PROFILE SYNC API")
                .path(IDAM_GET_USER_URL)
                .method(HttpMethod.GET.toString())
                .headers(headers)
                .willRespondWith()
                .status(HttpStatus.OK.value())
                .body(createUserDetailsResponse())
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "executeGetUserAndGet200")
    public void should_get_user_from_elastic_search(MockServer mockServer) throws JSONException {

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
        assertThat(response.getString("forename")).isNotBlank();
        assertThat(response.getString("surname")).isNotBlank();

        JSONArray rolesArr = new JSONArray(response.getString("roles"));

        assertThat(rolesArr).isNotNull();
        assertThat(rolesArr.length()).isNotZero();
        assertThat(rolesArr.get(0).toString()).isNotBlank();

    }

    private PactDslJsonBody createUserDetailsResponse() {
        boolean status = true;
        PactDslJsonArray array = new PactDslJsonArray()
                .string("pui-organisation-manager")
                .string("pui-case-manager");

        return new PactDslJsonBody()
                .stringType("id", "a833c2e2-2c73-4900-96ca-74b1efb37928")
                .stringType("forename", "Jack")
                .stringType("surname", "Skellington")
                .stringType("email", "jack@spookmail.com")
                .booleanType("active", true)
                .object("roles", array);
    }
}