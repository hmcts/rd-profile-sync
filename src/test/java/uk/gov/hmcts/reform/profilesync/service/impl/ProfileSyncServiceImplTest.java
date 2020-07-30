package uk.gov.hmcts.reform.profilesync.service.impl;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.profilesync.helper.MockDataProvider.CLIENT_AUTHORIZATION;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import feign.Request;
import feign.Response;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.profilesync.advice.UserProfileSyncException;
import uk.gov.hmcts.reform.profilesync.client.IdamClient;
import uk.gov.hmcts.reform.profilesync.client.UserProfileClient;
import uk.gov.hmcts.reform.profilesync.config.TokenConfigProperties;
import uk.gov.hmcts.reform.profilesync.domain.response.OpenIdAccessTokenResponse;
import uk.gov.hmcts.reform.profilesync.service.ProfileUpdateService;

public class ProfileSyncServiceImplTest {

    private final IdamClient idamClientMock = mock(IdamClient.class); //mocked as its an interface
    private final AuthTokenGenerator tokenGeneratorMock = mock(AuthTokenGenerator.class); //mocked as its an interface
    private final UserProfileClient userProfileClientMock = mock(UserProfileClient.class); //mocked as its an interface
    private final ProfileUpdateService profileUpdateServiceMock
            = mock(ProfileUpdateService.class); //mocked as its an interface
    private final TokenConfigProperties tokenConfigProperties = new TokenConfigProperties();
    private final OpenIdAccessTokenResponse openIdTokenResponseMock = mock(OpenIdAccessTokenResponse.class);
    private ProfileSyncServiceImpl sut = new ProfileSyncServiceImpl(idamClientMock, tokenGeneratorMock,
            profileUpdateServiceMock, tokenConfigProperties, "RD_Profile_Sync");

    private final String accessToken = "dd5g2b6-9699-12f9-bf42-526rf8864g64";

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(5000);

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        final String clientId = "234342332";
        final String redirectUri = "http://idam-api.aat.platform.hmcts.net";
        final String authorization = "c2hyZWVkaGFyLmxvbXRlQGhtY3RzLm5ldDpITUNUUzEyMzQ=";
        final String clientAuth = "cmQteHl6LWFwaTp4eXo=";
        final String url = "http://127.0.0.1:5000";

        tokenConfigProperties.setClientId(clientId);
        tokenConfigProperties.setClientAuthorization(clientAuth);
        tokenConfigProperties.setAuthorization(authorization);
        tokenConfigProperties.setRedirectUri(redirectUri);
        tokenConfigProperties.setUrl(url);

        Map<String, String> params = new HashMap<>();
        params.put("client_id", tokenConfigProperties.getClientId());
        params.put("redirect_uri", tokenConfigProperties.getRedirectUri());
        params.put("response_type", "code");
        params.put("scope", "openid profile roles create-user manage-user search-user");

        Map<String, String> formParams = new HashMap<>();
        formParams.put("client_id", tokenConfigProperties.getClientId());
        formParams.put("redirect_uri", tokenConfigProperties.getRedirectUri());
        formParams.put("code", accessToken);
        formParams.put("grant_type", "authorization_code");

        Map<String, String> getTokenParams = new HashMap<>();
        getTokenParams.put("grant_type", "password");
        getTokenParams.put("username", "shreedhar.lomte@hmcts.net");
        getTokenParams.put("password", "HMCTS1234");
        getTokenParams.put("client_id", tokenConfigProperties.getClientId());
        getTokenParams.put("client_secret", "xyz");
        getTokenParams.put("redirect_uri", tokenConfigProperties.getRedirectUri());
        getTokenParams.put("scope", "openid profile roles manage-user create-user search-user");
    }

    @Test
    public void getBearerToken() {
        final String bearerTokenJson = "{"
                .concat("  \"access_token\": \"eyjfddsfsdfsdfdj03903.dffkljfke932rjf032j02f3--fskfljdskls-fdkldskll\"")
                .concat("}");
        stubFor(post(urlEqualTo("/o/token"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(bearerTokenJson)));
        when(openIdTokenResponseMock.getAccessToken()).thenReturn(CLIENT_AUTHORIZATION);
        when(idamClientMock.getOpenIdToken(any())).thenReturn(openIdTokenResponseMock);

        String actualToken = sut.getBearerToken();
        assertThat(actualToken).isEqualTo(CLIENT_AUTHORIZATION);
        verify(openIdTokenResponseMock, times(1)).getAccessToken();
        verify(idamClientMock, times(1)).getOpenIdToken(any());

    }

    @Test(expected = UserProfileSyncException.class)
    public void test_getBearerToken_WithStatus300() {
        final String bearerTokenJson = null;
        stubFor(post(urlEqualTo("/o/token"))
                .willReturn(aResponse().withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody(bearerTokenJson)));
        when(idamClientMock.getOpenIdToken(any())).thenThrow(UserProfileSyncException.class);
        String actualToken = sut.getBearerToken();
        assertThat(actualToken).isNull();
    }

    @Test
    public void test_GetS2sToken() {
        final String expect = "Bearer xyz";
        when(tokenGeneratorMock.generate()).thenReturn(expect);

        assertThat(sut.getS2sToken()).isEqualTo(expect);
        verify(tokenGeneratorMock, times(1)).generate();
    }


    @Test
    public void testGetSyncFeed() throws JsonProcessingException {
        final String bearerToken = "Bearer eyJ0eXAiOiJKV1QiLCJ6aXAiOiJOT05FIiwia2lkIjoic";//Dummy one
        final String searchQuery = "lastModified:>now-24h";

        Map<String, String> formParams = new HashMap<>();
        formParams.put("query", searchQuery);
        formParams.put("page", String.valueOf(0));

        List<IdamClient.User> users = new ArrayList<>();
        users.add(createUser("some@some.com"));
        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(users);

        Response response = Response.builder().request(Request.create(Request.HttpMethod.GET, "", new HashMap<>(),
                Request.Body.empty(), null)).body(body, Charset.defaultCharset()).status(200).build();
        when(idamClientMock.getUserFeed(bearerToken, formParams)).thenReturn(response);
        when(userProfileClientMock.findUser(any(), any(), any())).thenReturn(Response.builder()
                .request(Request.create(Request.HttpMethod.GET, "", new HashMap<>(), Request.Body.empty(),
                        null)).body(body, Charset.defaultCharset()).status(200).build());
        assertThat(response).isNotNull();

        List<IdamClient.User> useResponse = sut.getSyncFeed(bearerToken, searchQuery);
        assertThat(useResponse).isNotNull();
        assertThat(useResponse.get(0).getEmail()).isEqualTo("some@some.com");

        verify(idamClientMock, times(1)).getUserFeed(bearerToken, formParams);
    }

    @Test
    public void testGetSyncFeed_whenNoRecords() throws JsonProcessingException {
        final String bearerToken = "Bearer eyJ0eXAiOiJKV1QiLCJ6aXAiOiJOT05FIiwia2lkIjoic";//Dummy one
        final String searchQuery = "lastModified:>now-24h";

        Map<String, String> formParams = new HashMap<>();
        formParams.put("query", searchQuery);
        formParams.put("page", String.valueOf(0));

        Map<String, Collection<String>> headers = new HashMap<>();
        List<String> headersList = new ArrayList<>();
        headersList.add(String.valueOf(0));
        headers.put("X-Total-Count", headersList);

        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(new ArrayList<>());

        Response response = Response.builder().request(Request.create(Request.HttpMethod.GET, "", new HashMap<>(),
                Request.Body.empty(), null)).headers(headers).body(body,
                Charset.defaultCharset()).status(200).build();
        when(idamClientMock.getUserFeed(bearerToken, formParams)).thenReturn(response);
        when(userProfileClientMock.findUser(any(), any(), any())).thenReturn(Response.builder()
                .request(Request.create(Request.HttpMethod.GET, "", new HashMap<>(), Request.Body.empty(),
                        null)).body(body, Charset.defaultCharset()).status(200).build());
        assertThat(response).isNotNull();

        List<IdamClient.User> useResponse = sut.getSyncFeed(bearerToken, searchQuery);
        assertThat(useResponse).isEmpty();

        verify(idamClientMock, times(1)).getUserFeed(bearerToken, formParams);
    }

    @Test
    public void test_GetSyncFeed_when_more_than_20_records() throws JsonProcessingException {
        final String bearerToken = "Bearer iJOT05FWIiOiJwcmF2ZWVuLnRob3R0ZW1wdWRpMyEXwm5B";
        final String searchQuery = "lastModified:>now-24h";

        Map<String, String> formParams = new HashMap<>();
        formParams.put("query", searchQuery);
        formParams.put("page", String.valueOf(0));
        Map<String, String> secondPageFormParams = new HashMap<>();
        secondPageFormParams.put("query", searchQuery);
        secondPageFormParams.put("page", String.valueOf(1));
        List<IdamClient.User> users = new ArrayList<>();
        IdamClient.User profile;
        for (int i = 0; i < 20; i++) {
            profile = createUser("someuser" + i + "@test.com");
            users.add(profile);
        }

        Map<String, Collection<String>> headers = new HashMap<>();
        List<String> headersList = new ArrayList<>();
        headersList.add(String.valueOf(22));
        headers.put("X-Total-Count", headersList);

        List<IdamClient.User> secondPageUsers = new ArrayList<>();
        secondPageUsers.add(createUser("someuser" + 21 + "@test.com"));
        secondPageUsers.add(createUser("someuser" + 22 + "@test.com"));
        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(users);
        String secondPageBody = mapper.writeValueAsString(secondPageUsers);

        Response response = Response.builder().request(Request.create(Request.HttpMethod.GET, "", new HashMap<>(),
                Request.Body.empty(), null)).headers(headers).body(body,
                Charset.defaultCharset()).status(200).build();
        Response secondPageResponse = Response.builder().request(Request.create(Request.HttpMethod.GET, "",
                new HashMap<>(), Request.Body.empty(), null)).headers(headers).body(secondPageBody,
                Charset.defaultCharset()).status(200).build();
        assertThat(response).isNotNull();
        assertThat(secondPageResponse).isNotNull();

        when(idamClientMock.getUserFeed(bearerToken, formParams)).thenReturn(response);
        when(idamClientMock.getUserFeed(bearerToken, secondPageFormParams)).thenReturn(secondPageResponse);
        when(userProfileClientMock.findUser(any(), any(), any())).thenReturn(Response.builder().request(
                Request.create(Request.HttpMethod.GET, "", new HashMap<>(), Request.Body.empty(),
                        null)).body(body, Charset.defaultCharset()).status(200).build());

        List<IdamClient.User> useResponse = sut.getSyncFeed(bearerToken, searchQuery);
        assertThat(useResponse).isNotEmpty();
        assertThat(useResponse.size()).isEqualTo(22);
        assertThat(useResponse.containsAll(users)).isTrue();
        assertThat(useResponse.containsAll(secondPageUsers)).isTrue();

        verify(idamClientMock, times(2)).getUserFeed(any(), any());
    }

    @Test(expected = UserProfileSyncException.class)
    public void test_GetSyncFeed_when_400() throws JsonProcessingException {
        final String bearerToken = "Bearer eyJ0eXAiOiJKV1QiLCPSIsImFsZyI6IlJTMjU2In0";
        final String searchQuery = "lastModified:>now-24h";

        Map<String, String> formParams = new HashMap<>();
        formParams.put("query", searchQuery);
        formParams.put("page", String.valueOf(0));

        List<IdamClient.User> users = new ArrayList<>();
        users.add(createUser("some@some.com"));

        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(users);

        Response response = Response.builder().request(Request.create(Request.HttpMethod.GET, "", new HashMap<>(),
                Request.Body.empty(), null)).body(body, Charset.defaultCharset()).status(400).build();
        when(idamClientMock.getUserFeed(bearerToken, formParams)).thenReturn(response);
        assertThat(response).isNotNull();

        List<IdamClient.User> useResponseList = sut.getSyncFeed(bearerToken, searchQuery);
        assertThat(useResponseList).isEmpty();

        verify(idamClientMock, times(1)).getUserFeed(bearerToken, formParams);
    }

    @Test
    public void test_UpdateUserProfileFeed() throws Exception {
        final String bearerToken = "eyJ0eXAiOiJKV1QiLCJ6aXAiOi";
        final String bearerTokenJson = "{" + "  \"access_token\": \"" + bearerToken + "\"" + "}";
        final String searchQuery = "lastModified:>now-24h";

        stubFor(post(urlEqualTo("/o/token"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(bearerTokenJson)));

        Map<String, String> formParams = new HashMap<>();
        formParams.put("query", searchQuery);
        formParams.put("page", String.valueOf(0));
        IdamClient.User profile = new IdamClient.User();
        profile.setActive(true);
        profile.setEmail("some@some.com");
        profile.setForename("some");
        profile.setId(UUID.randomUUID().toString());
        profile.setActive(true);
        List<IdamClient.User> users = new ArrayList<>();
        users.add(profile);
        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(users);

        Response response = Response.builder().request(Request.create(Request.HttpMethod.GET, "", new HashMap<>(),
                Request.Body.empty(), null)).body(body, Charset.defaultCharset()).status(200).build();
        when(openIdTokenResponseMock.getAccessToken()).thenReturn(bearerToken);
        when(idamClientMock.getOpenIdToken(any())).thenReturn(openIdTokenResponseMock);
        when(idamClientMock.getUserFeed(eq("Bearer " + bearerToken), any())).thenReturn(response);
        when(userProfileClientMock.findUser(any(), any(), any())).thenReturn(Response.builder()
                .request(Request.create(Request.HttpMethod.GET, "", new HashMap<>(), Request.Body.empty(),
                        null)).body(body, Charset.defaultCharset()).status(200).build());
        assertThat(response).isNotNull();

        sut.updateUserProfileFeed(searchQuery);

        verify(profileUpdateServiceMock, times(1)).updateUserProfile(eq(searchQuery),
                eq("Bearer " + bearerToken), any(), any());
        verify(idamClientMock, times(1)).getUserFeed(eq("Bearer " + bearerToken), any());
    }

    @Test
    public void test_objectProfileSyncServiceImpl() {
        ProfileSyncServiceImpl profileSyncService = new ProfileSyncServiceImpl();
        assertThat(profileSyncService).isNotNull();
    }


    private IdamClient.User createUser(String email) {
        IdamClient.User profile = new IdamClient.User();
        profile.setActive(true);
        profile.setEmail(email);
        profile.setForename("some");
        profile.setId(UUID.randomUUID().toString());
        profile.setActive(true);
        return profile;
    }
}