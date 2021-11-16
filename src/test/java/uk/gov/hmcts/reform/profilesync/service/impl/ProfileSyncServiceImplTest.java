package uk.gov.hmcts.reform.profilesync.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.profilesync.advice.UserProfileSyncException;
import uk.gov.hmcts.reform.profilesync.client.IdamClient;
import uk.gov.hmcts.reform.profilesync.client.UserProfileClient;
import uk.gov.hmcts.reform.profilesync.config.TokenConfigProperties;
import uk.gov.hmcts.reform.profilesync.domain.ProfileSyncAudit;
import uk.gov.hmcts.reform.profilesync.domain.response.OpenIdAccessTokenResponse;
import uk.gov.hmcts.reform.profilesync.service.ProfileUpdateService;
import uk.gov.hmcts.reform.profilesync.service.wiremock.WireMockExtension;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.profilesync.helper.MockDataProvider.CLIENT_AUTHORIZATION;

class ProfileSyncServiceImplTest {

    private final IdamClient idamClientMock = mock(IdamClient.class); //mocked as its an interface
    private final AuthTokenGenerator tokenGeneratorMock = mock(AuthTokenGenerator.class); //mocked as its an interface
    private final UserProfileClient userProfileClientMock = mock(UserProfileClient.class); //mocked as its an interface
    private final ProfileUpdateService profileUpdateServiceMock
            = mock(ProfileUpdateService.class); //mocked as its an interface
    private final TokenConfigProperties tokenConfigProperties = new TokenConfigProperties();
    private final OpenIdAccessTokenResponse openIdTokenResponseMock = mock(OpenIdAccessTokenResponse.class);
    private final ProfileSyncServiceImpl sut = new ProfileSyncServiceImpl(idamClientMock, tokenGeneratorMock,
            profileUpdateServiceMock, tokenConfigProperties, "RD_Profile_Sync", 100);

    @RegisterExtension
    private final WireMockExtension wireMockExtension = new WireMockExtension(5000);

    ProfileSyncAudit profileSyncAudit;

    @BeforeEach
    public void setUp() {
        final String clientId = "234342332";
        final String redirectUri = "http://idam-api.aat.platform.hmcts.net";
        final String authorization = "c2hyZWVkaGFyLmxvbXRlQGhtY3RzLm5ldDpITUNUUzEyMzQ=";
        final String clientAuth = "cmQteHl6LWFwaTp4eXo=";

        profileSyncAudit = new ProfileSyncAudit();

        tokenConfigProperties.setClientId(clientId);
        tokenConfigProperties.setClientAuthorization(clientAuth);
        tokenConfigProperties.setAuthorization(authorization);
        tokenConfigProperties.setRedirectUri(redirectUri);
    }

    @Test
    void getBearerToken() {
        final String bearerTokenJson = "{"
                .concat("  \"access_token\": \"eyjfddsfsdfsdfdj03903.dffkljfke932rjf032j02f3--fskfljdskls-fdkldskll\"")
                .concat("}");
        wireMockExtension.stubFor(post(urlEqualTo("/o/token"))
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

    @Test
    void test_getBearerToken_WithStatus300() {
        final String bearerTokenJson = null;

        wireMockExtension.stubFor(WireMock.post(urlPathMatching("/o/token"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody(bearerTokenJson)));

        when(idamClientMock.getOpenIdToken(any())).thenThrow(UserProfileSyncException.class);
        assertThrows(UserProfileSyncException.class, sut::getBearerToken);
        verify(idamClientMock, times(1)).getOpenIdToken(any());
    }

    @Test
    void getS2sToken() {
        final String expect = "Bearer xyz";
        when(tokenGeneratorMock.generate()).thenReturn(expect);

        assertThat(sut.getS2sToken()).isEqualTo(expect);
        verify(tokenGeneratorMock, times(1)).generate();
    }


    @Test
    void testGetSyncFeed() throws JsonProcessingException {
        final String bearerToken = "Bearer eyJ0eXAiOiJKV1QiLCJ6aXAiOiJOT05FIiwia2lkIjoic";//Dummy one
        final String searchQuery = "lastModified:>now-24h";

        Map<String, String> formParams = new HashMap<>();
        formParams.put("query", searchQuery);
        formParams.put("page", String.valueOf(0));
        formParams.put("size", String.valueOf(100));

        List<IdamClient.User> users = new ArrayList<>();
        users.add(createUser("some@some.com"));
        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(users);

        Response response = Response.builder().request(Request.create(Request.HttpMethod.GET, "", new HashMap<>(),
                Request.Body.empty(), null)).body(body, Charset.defaultCharset()).status(200).build();
        when(idamClientMock.getUserFeed(bearerToken, formParams)).thenReturn(response);
        when(userProfileClientMock.findUser(any(), any(), any())).thenReturn(Response.builder().request(
                Request.create(Request.HttpMethod.GET, "", new HashMap<>(), Request.Body.empty(),
                        null)).body(body, Charset.defaultCharset()).status(200).build());
        assertThat(response).isNotNull();

        Set<IdamClient.User> useResponses = sut.getSyncFeed(bearerToken, searchQuery);
        assertThat(useResponses).isNotNull();

        useResponses.forEach(useResponse -> assertThat(useResponse.getEmail()).isEqualTo("some@some.com"));
        verify(idamClientMock, times(1)).getUserFeed(bearerToken, formParams);
    }

    @Test
    void testGetSyncFeed_whenNoRecords() throws JsonProcessingException {
        final String bearerToken = "Bearer eyJ0eXAiOiJKV1QiLCJ6aXAiOiJOT05FIiwia2lkIjoic";//Dummy one
        final String searchQuery = "lastModified:>now-24h";

        Map<String, String> formParams = new HashMap<>();
        formParams.put("query", searchQuery);
        formParams.put("page", String.valueOf(0));
        formParams.put("size", String.valueOf(100));


        Map<String, Collection<String>> headers = new HashMap<>();
        List<String> headersList = new ArrayList<>();
        headersList.add(String.valueOf(0));
        headers.put("X-Total-Count", headersList);

        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(new ArrayList<>());

        Response response = Response.builder().request(Request.create(Request.HttpMethod.GET, "",
                new HashMap<>(), Request.Body.empty(), null)).headers(headers).body(body,
                Charset.defaultCharset()).status(200).build();
        when(idamClientMock.getUserFeed(bearerToken, formParams)).thenReturn(response);
        when(userProfileClientMock.findUser(any(), any(), any())).thenReturn(Response.builder()
                .request(Request.create(Request.HttpMethod.GET, "", new HashMap<>(), Request.Body.empty(),
                        null)).body(body, Charset.defaultCharset()).status(200).build());
        assertThat(response).isNotNull();

        Set<IdamClient.User> useResponse = sut.getSyncFeed(bearerToken, searchQuery);
        assertThat(useResponse).isEmpty();

        verify(idamClientMock, times(1)).getUserFeed(bearerToken, formParams);
    }

    @Test
    void getSyncFeedWhenMoreThan20Records() throws JsonProcessingException {
        final String bearerToken = "Bearer iJOT05FWIiOiJwcmF2ZWVuLnRob3R0ZW1wdWRpMyEXwm5B";
        final String searchQuery = "lastModified:>now-24h";

        Map<String, String> formParams = new HashMap<>();
        formParams.put("query", searchQuery);
        formParams.put("page", String.valueOf(0));
        formParams.put("size", String.valueOf(100));

        Map<String, String> secondPageFormParams = new HashMap<>();
        secondPageFormParams.put("query", searchQuery);
        secondPageFormParams.put("page", String.valueOf(1));
        secondPageFormParams.put("size", String.valueOf(100));

        Map<String, String> thirdPageFormParams = new HashMap<>();
        thirdPageFormParams.put("query", searchQuery);
        thirdPageFormParams.put("page", String.valueOf(2));
        thirdPageFormParams.put("size", String.valueOf(100));

        Map<String, String> fourthPageFormParams = new HashMap<>();
        fourthPageFormParams.put("query", searchQuery);
        fourthPageFormParams.put("page", String.valueOf(3));
        fourthPageFormParams.put("size", String.valueOf(100));

        Map<String, String> fifthPageFormParams = new HashMap<>();
        fifthPageFormParams.put("query", searchQuery);
        fifthPageFormParams.put("page", String.valueOf(4));
        fifthPageFormParams.put("size", String.valueOf(100));

        Map<String, String> sixthPageFormParams = new HashMap<>();
        sixthPageFormParams.put("query", searchQuery);
        sixthPageFormParams.put("page", String.valueOf(5));
        sixthPageFormParams.put("size", String.valueOf(100));

        Set<IdamClient.User> users = new HashSet<>();
        IdamClient.User profile;
        for (int i = 0; i < 500; i++) {
            profile = createUser("someuser" + i + "@test.com");
            users.add(profile);
        }

        Map<String, Collection<String>> headers = new HashMap<>();
        List<String> headersList = new ArrayList<>();
        headersList.add(String.valueOf(502));
        headers.put("X-Total-Count", headersList);

        List<IdamClient.User> secondPageUsers = new ArrayList<>();
        secondPageUsers.add(createUser("someuser" + 501 + "@test.com"));
        secondPageUsers.add(createUser("someuser" + 502 + "@test.com"));
        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(users);
        String secondPageBody = mapper.writeValueAsString(secondPageUsers);

        Response response = Response.builder().request(Request.create(Request.HttpMethod.GET, "", new HashMap<>(),
                Request.Body.empty(), null)).headers(headers)
                .body(body, Charset.defaultCharset()).status(200).build();
        Response secondPageResponse = Response.builder().request(Request.create(Request.HttpMethod.GET,
                "", new HashMap<>(), Request.Body.empty(), null)).headers(headers)
                .body(secondPageBody, Charset.defaultCharset()).status(200).build();
        assertThat(response).isNotNull();
        assertThat(secondPageResponse).isNotNull();

        when(idamClientMock.getUserFeed(bearerToken, formParams)).thenReturn(response);
        when(idamClientMock.getUserFeed(bearerToken, secondPageFormParams)).thenReturn(secondPageResponse);
        when(idamClientMock.getUserFeed(bearerToken, thirdPageFormParams)).thenReturn(secondPageResponse);
        when(idamClientMock.getUserFeed(bearerToken, fourthPageFormParams)).thenReturn(secondPageResponse);
        when(idamClientMock.getUserFeed(bearerToken, fifthPageFormParams)).thenReturn(secondPageResponse);
        when(idamClientMock.getUserFeed(bearerToken, sixthPageFormParams)).thenReturn(secondPageResponse);
        when(userProfileClientMock.findUser(any(), any(), any())).thenReturn(Response.builder()
                .request(Request.create(Request.HttpMethod.GET, "", new HashMap<>(), Request.Body.empty(),
                        null)).body(body, Charset.defaultCharset()).status(200).build());

        Set<IdamClient.User> useResponse = sut.getSyncFeed(bearerToken, searchQuery);
        assertThat(useResponse).isNotEmpty();
        assertThat(useResponse.size()).isEqualTo(502);
        assertThat(useResponse.containsAll(users)).isTrue();
        assertThat(useResponse.containsAll(secondPageUsers)).isTrue();

        verify(idamClientMock, times(6)).getUserFeed(any(), any());
    }

    @Test
    void getSyncFeedWhen400() throws JsonProcessingException {
        final String bearerToken = "Bearer eyJ0eXAiOiJKV1QiLCPSIsImFsZyI6IlJTMjU2In0";
        final String searchQuery = "lastModified:>now-24h";

        Map<String, String> formParams = new HashMap<>();
        formParams.put("query", searchQuery);
        formParams.put("page", String.valueOf(0));
        formParams.put("size", String.valueOf(100));

        Set<IdamClient.User> users = new HashSet<>();
        users.add(createUser("some@some.com"));

        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(users);

        Response response = Response.builder().request(Request.create(Request.HttpMethod.GET, "",
                new HashMap<>(), Request.Body.empty(), null)).body(body, Charset.defaultCharset())
                .status(400).build();
        when(idamClientMock.getUserFeed(bearerToken, formParams)).thenReturn(response);
        assertThat(response).isNotNull();

        assertThrows(UserProfileSyncException.class, () -> sut.getSyncFeed(bearerToken, searchQuery));

        verify(idamClientMock, times(1)).getUserFeed(bearerToken, formParams);
    }

    @Test
    void updateUserProfileFeed() throws Exception {
        final String bearerToken = "eyJ0eXAiOiJKV1QiLCJ6aXAiOi";
        final String bearerTokenJson = "{" + "  \"access_token\": \"" + bearerToken + "\"" + "}";
        final String searchQuery = "lastModified:>now-24h";

        wireMockExtension.stubFor(post(urlEqualTo("/o/token"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(bearerTokenJson)));

        Map<String, String> formParams = new HashMap<>();
        formParams.put("query", searchQuery);
        formParams.put("page", String.valueOf(0));
        formParams.put("size", String.valueOf(100));

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
        when(userProfileClientMock.findUser(any(), any(), any())).thenReturn(Response.builder().request(
                Request.create(Request.HttpMethod.GET, "", new HashMap<>(), Request.Body.empty(),
                        null)).body(body, Charset.defaultCharset()).status(200).build());
        assertThat(response).isNotNull();

        sut.updateUserProfileFeed(searchQuery, profileSyncAudit);

        verify(profileUpdateServiceMock, times(1)).updateUserProfile(eq(searchQuery),
                eq("Bearer " + bearerToken), any(), any(), any());
        verify(idamClientMock, times(1)).getUserFeed(eq("Bearer " + bearerToken), any());
    }

    @Test
    void objectProfileSyncServiceImpl() {
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
