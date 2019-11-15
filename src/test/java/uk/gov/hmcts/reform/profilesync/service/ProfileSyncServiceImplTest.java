package uk.gov.hmcts.reform.profilesync.service;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import uk.gov.hmcts.reform.profilesync.client.IdamClient;
import uk.gov.hmcts.reform.profilesync.client.UserProfileClient;
import uk.gov.hmcts.reform.profilesync.config.TokenConfigProperties;
import uk.gov.hmcts.reform.profilesync.domain.UserProfileSyncException;
import uk.gov.hmcts.reform.profilesync.helper.MockDataProvider;
import uk.gov.hmcts.reform.profilesync.service.impl.ProfileSyncServiceImpl;

public class ProfileSyncServiceImplTest {

    private final IdamClient idamClientMock = mock(IdamClient.class);

    private final AuthTokenGenerator tokenGeneratorMock = mock(AuthTokenGenerator.class);

    private final UserProfileClient userProfileClientMock = mock(UserProfileClient.class);

    private final TokenConfigProperties propsMock = mock(TokenConfigProperties.class);

    private final ProfileUpdateService profileUpdateService = mock(ProfileUpdateService.class);

    private ProfileSyncServiceImpl sut = new ProfileSyncServiceImpl(idamClientMock, tokenGeneratorMock, profileUpdateService, propsMock);

    private final String accessToken = "dd5g2b6-9699-12f9-bf42-526rf8864g64";


    @Rule
    public WireMockRule wireMockRule = new WireMockRule(5000);

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {

        final String clientId = "234342332";
        final String redirectUri = "http://idam-api.aat.platform.hmcts.net";
        final String authorization = "my authorization";
        final String clientAuth = "cmQteHl6LWFwaTp4eXo=";

        when(propsMock.getClientId()).thenReturn(clientId);
        when(propsMock.getRedirectUri()).thenReturn(redirectUri);
        when(propsMock.getAuthorization()).thenReturn(authorization);
        when(propsMock.getClientAuthorization()).thenReturn(clientAuth);
        when(propsMock.getUrl()).thenReturn("http://127.0.0.1:5000");
        when(propsMock.getAuthorization()).thenReturn("c2hyZWVkaGFyLmxvbXRlQGhtY3RzLm5ldDpITUNUUzEyMzQ=");

        Map<String, String> params = new HashMap<>();
        params.put("client_id", propsMock.getClientId());
        params.put("redirect_uri", propsMock.getRedirectUri());
        params.put("response_type", "code");
        params.put("scope", "openid profile roles create-user manage-user search-user");


        Map<String, String> formParams = new HashMap<>();
        formParams.put("client_id", propsMock.getClientId());
        formParams.put("redirect_uri", propsMock.getRedirectUri());
        formParams.put("code", accessToken);
        formParams.put("grant_type", "authorization_code");

        Map<String, String> getTokenParams = new HashMap<>();
        getTokenParams.put("grant_type", "password");
        getTokenParams.put("username", "shreedhar.lomte@hmcts.net");
        getTokenParams.put("password", "HMCTS1234");
        getTokenParams.put("client_id", propsMock.getClientId());
        getTokenParams.put("client_secret", "xyz");
        getTokenParams.put("redirect_uri", propsMock.getRedirectUri());
        getTokenParams.put("scope", "openid profile roles manage-user create-user search-user");

        IdamClient.BearerTokenResponse bearerTokenExchangeResponse = mock(IdamClient.BearerTokenResponse.class);
        when(bearerTokenExchangeResponse.getAccessToken()).thenReturn(MockDataProvider.clientAuthorization);

    }

    @Test
    public void getBearerToken() {
        final String bearerTokenJson = "{" + "  \"access_token\": \"eyjfddsfsdfsdfdj03903.dffkljfke932rjf032j02f3--fskfljdskls-fdkldskll\"" + "}";
        stubFor(post(urlEqualTo("/o/token"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(bearerTokenJson)));

        String actualToken = sut.getBearerToken();
        assertThat(actualToken).isEqualTo(MockDataProvider.clientAuthorization);
    }

    @Test
    public void testGetS2sToken() {
        final String expect = "Bearer xyz";
        when(tokenGeneratorMock.generate()).thenReturn(expect);

        assertThat(sut.getS2sToken()).isEqualTo(expect);
    }


    @Test
    public void testGetSyncFeed() throws JsonProcessingException {
        final String bearerToken = "Bearer eyJ0eXAiOiJKV1QiLCJ6aXAiOiJOT05FIiwia2lkIjoicDBZL0lpN0txdS9uZndIK0RvdmZVMEszbHRJPSIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJwcmF2ZWVuLnRob3R0ZW1wdWRpQGhtY3RzLm5ldCIsImF1dGhfbGV2ZWwiOjAsImF1ZGl0VHJhY2tpbmdJZCI6IjIxOWNiMjdkLTkwZmEtNGE3Yi05Yzk1LWRhNWRhMzI0MjJlNyIsImlzcyI6Imh0dHBzOi8vZm9yZ2Vyb2NrLWFtLnNlcnZpY2UuY29yZS1jb21wdXRlLWlkYW0tcHJldmlldy5pbnRlcm5hbDo4NDQzL29wZW5hbS9vYXV0aDIvaG1jdHMiLCJ0b2tlbk5hbWUiOiJhY2Nlc3NfdG9rZW4iLCJ0b2tlbl90eXBlIjoiQmVhcmVyIiwiYXV0aEdyYW50SWQiOiJkYzFkM2NjNS05OWZhLTRhNDAtYWIxMC0zMzcxZjcwNjQ0YWMiLCJhdWQiOiJyZC1wcm9mZXNzaW9uYWwtYXBpIiwibmJmIjoxNTYzNTMyMDY4LCJncmFudF90eXBlIjoiYXV0aG9yaXphdGlvbl9jb2RlIiwic2NvcGUiOlsib3BlbmlkIiwicHJvZmlsZSIsInJvbGVzIiwiY3JlYXRlLXVzZXIiLCJtYW5hZ2UtdXNlciIsInNlYXJjaC11c2VyIl0sImF1dGhfdGltZSI6MTU2MzUzMjA0NzAwMCwicmVhbG0iOiIvaG1jdHMiLCJleHAiOjE1NjM1NjA4NjgsImlhdCI6MTU2MzUzMjA2OCwiZXhwaXJlc19pbiI6Mjg4MDAsImp0aSI6ImU3NjMyNzgwLWM1N2QtNDhjMC1iNmQ3LTZmM2M2MzIwMDExMiJ9.HmhKLluiDncAcHrZSvrgFxnagIFPVRXa9aSl8uymK3l91Ss94csZpDyUh5UQH0bzZtJjeNPAci5dEYgLuqeO9-ydhRA_tBhfbjS7kBlUmo6RHK492O5WE7goMhpsx-j6KoJ94-cNDgnbk0YyGYzPODS7FLx1HZgLH5_E7sIzT8-GXTMQOm7rwxFkcA0BZwgp45YYV1wvxRP6vCOpbY0Zrp8hx6fQdbpAste0BFOruOSXR9eLuqCCxX_wud7dv9FHF6wCBf_nZ6XkgVTZAHav0tWsLnfGMyEXwm5BJWKy3ctl8H8RQNPArA1PlhdkQBNYWk2BX_1zC7zFI_terWkqjQ";
        final String searchQuery = "lastModified:>now-24h";

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

        Response response = Response.builder().request(Request.create(Request.HttpMethod.GET, "", new HashMap<>(), Request.Body.empty())).body(body, Charset.defaultCharset()).status(200).build();
        when(idamClientMock.getUserFeed(bearerToken, formParams)).thenReturn(response);
        when(userProfileClientMock.findUser(any(), any(), any())).thenReturn(Response.builder().request(Request.create(Request.HttpMethod.GET, "", new HashMap<>(), Request.Body.empty())).body(body, Charset.defaultCharset()).status(200).build());
        List<IdamClient.User> useResponse = sut.getSyncFeed(bearerToken, searchQuery);
        assertThat(response).isNotNull();
    }

    @Test
    public void testGetSyncFeed_whenNoRecords() throws JsonProcessingException {
        final String bearerToken = "Bearer eyJ0eXAiOiJKV1QiLCJ6aXAiOiJOT05FIiwia2lkIjoicDBZL0lpN0txdS9uZndIK0RvdmZVMEszbHRJPSIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJwcmF2ZWVuLnRob3R0ZW1wdWRpQGhtY3RzLm5ldCIsImF1dGhfbGV2ZWwiOjAsImF1ZGl0VHJhY2tpbmdJZCI6IjIxOWNiMjdkLTkwZmEtNGE3Yi05Yzk1LWRhNWRhMzI0MjJlNyIsImlzcyI6Imh0dHBzOi8vZm9yZ2Vyb2NrLWFtLnNlcnZpY2UuY29yZS1jb21wdXRlLWlkYW0tcHJldmlldy5pbnRlcm5hbDo4NDQzL29wZW5hbS9vYXV0aDIvaG1jdHMiLCJ0b2tlbk5hbWUiOiJhY2Nlc3NfdG9rZW4iLCJ0b2tlbl90eXBlIjoiQmVhcmVyIiwiYXV0aEdyYW50SWQiOiJkYzFkM2NjNS05OWZhLTRhNDAtYWIxMC0zMzcxZjcwNjQ0YWMiLCJhdWQiOiJyZC1wcm9mZXNzaW9uYWwtYXBpIiwibmJmIjoxNTYzNTMyMDY4LCJncmFudF90eXBlIjoiYXV0aG9yaXphdGlvbl9jb2RlIiwic2NvcGUiOlsib3BlbmlkIiwicHJvZmlsZSIsInJvbGVzIiwiY3JlYXRlLXVzZXIiLCJtYW5hZ2UtdXNlciIsInNlYXJjaC11c2VyIl0sImF1dGhfdGltZSI6MTU2MzUzMjA0NzAwMCwicmVhbG0iOiIvaG1jdHMiLCJleHAiOjE1NjM1NjA4NjgsImlhdCI6MTU2MzUzMjA2OCwiZXhwaXJlc19pbiI6Mjg4MDAsImp0aSI6ImU3NjMyNzgwLWM1N2QtNDhjMC1iNmQ3LTZmM2M2MzIwMDExMiJ9.HmhKLluiDncAcHrZSvrgFxnagIFPVRXa9aSl8uymK3l91Ss94csZpDyUh5UQH0bzZtJjeNPAci5dEYgLuqeO9-ydhRA_tBhfbjS7kBlUmo6RHK492O5WE7goMhpsx-j6KoJ94-cNDgnbk0YyGYzPODS7FLx1HZgLH5_E7sIzT8-GXTMQOm7rwxFkcA0BZwgp45YYV1wvxRP6vCOpbY0Zrp8hx6fQdbpAste0BFOruOSXR9eLuqCCxX_wud7dv9FHF6wCBf_nZ6XkgVTZAHav0tWsLnfGMyEXwm5BJWKy3ctl8H8RQNPArA1PlhdkQBNYWk2BX_1zC7zFI_terWkqjQ";
        final String searchQuery = "lastModified:>now-24h";

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
        //users.add(profile);
        Map<String, Collection<String>> headers = new HashMap<>();
        List<String> headersList = new ArrayList<>();
        headersList.add(String.valueOf(0));
        headers.put("X-Total-Count", headersList);

        ObjectMapper mapper = new ObjectMapper();

        String body = mapper.writeValueAsString(users);

        Response response = Response.builder().request(Request.create(Request.HttpMethod.GET, "", new HashMap<>(), Request.Body.empty())).headers(headers).body(body, Charset.defaultCharset()).status(200).build();
        when(idamClientMock.getUserFeed(bearerToken, formParams)).thenReturn(response);
        when(userProfileClientMock.findUser(any(), any(), any())).thenReturn(Response.builder().request(Request.create(Request.HttpMethod.GET, "", new HashMap<>(), Request.Body.empty())).body(body, Charset.defaultCharset()).status(200).build());
        List<IdamClient.User> useResponse = sut.getSyncFeed(bearerToken, searchQuery);
        assertThat(response).isNotNull();
    }

    @Test
    public void testGetSyncFeed_when_more_than_20_records() throws JsonProcessingException {
        final String bearerToken = "Bearer iJOT05FWIiOiJwcmF2ZWVuLnRob3R0ZW1wdWRpMyEXwm5B";
        final String searchQuery = "lastModified:>now-24h";

        Map<String, String> formParams = new HashMap<>();
        formParams.put("query", searchQuery);
        formParams.put("page", String.valueOf(0));
        Map<String, String> secondPageFormParams = new HashMap<>();
        secondPageFormParams.put("query", searchQuery);
        secondPageFormParams.put("page", String.valueOf(1));
        List<IdamClient.User> users = new ArrayList<>();
        IdamClient.User profile = null;
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

        Response response = Response.builder().request(Request.create(Request.HttpMethod.GET, "", new HashMap<>(), Request.Body.empty())).headers(headers).body(body, Charset.defaultCharset()).status(200).build();
        Response secondPageResponse = Response.builder().request(Request.create(Request.HttpMethod.GET, "", new HashMap<>(), Request.Body.empty())).headers(headers).body(secondPageBody, Charset.defaultCharset()).status(200).build();

        when(idamClientMock.getUserFeed(bearerToken, formParams)).thenReturn(response);
        when(idamClientMock.getUserFeed(bearerToken, secondPageFormParams)).thenReturn(secondPageResponse);
        when(userProfileClientMock.findUser(any(), any(), any())).thenReturn(Response.builder().request(Request.create(Request.HttpMethod.GET, "", new HashMap<>(), Request.Body.empty())).body(body, Charset.defaultCharset()).status(200).build());
        List<IdamClient.User> useResponse = sut.getSyncFeed(bearerToken, searchQuery);
        assertThat(response).isNotNull();
        assertThat(useResponse).isNotEmpty();
    }

    private IdamClient.User createUser(String email) {
        IdamClient.User profile = new IdamClient.User();
        profile.setActive(true);
        profile.setEmail(email);
        profile.setForename("some");
        profile.setId(UUID.randomUUID().toString());
        profile.setActive(true);
        List<IdamClient.User> users = new ArrayList<>();
        return profile;
    }

    @Test(expected = UserProfileSyncException.class)
    public void testGetSyncFeed_when_400() throws JsonProcessingException {
        final String bearerToken = "Bearer eyJ0eXAiOiJKV1QiLCPSIsImFsZyI6IlJTMjU2In0";
        final String searchQuery = "lastModified:>now-24h";

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

        Response response = Response.builder().request(Request.create(Request.HttpMethod.GET, "", new HashMap<>(), Request.Body.empty())).body(body, Charset.defaultCharset()).status(400).build();
        when(idamClientMock.getUserFeed(bearerToken, formParams)).thenReturn(response);
        List<IdamClient.User> useResponseList = sut.getSyncFeed(bearerToken, searchQuery);
        assertThat(response).isNotNull();
        assertThat(useResponseList).isEmpty();
    }

    @Test
    public void testUpdateUserProfileFeed() throws Exception {
        final String bearerToken = "eyJ0eXAiOiJKV1QiLCJ6aXAiOi";
        final String bearerTokenJson = "{" + "  \"access_token\": \"" + bearerToken + "\"" + "}";


        stubFor(post(urlEqualTo("/o/token"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(bearerTokenJson)));


        final String searchQuery = "lastModified:>now-24h";
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

        Response response = Response.builder().request(Request.create(Request.HttpMethod.GET, "", new HashMap<>(), Request.Body.empty())).body(body, Charset.defaultCharset()).status(200).build();
        when(idamClientMock.getUserFeed(eq("Bearer " + bearerToken), any())).thenReturn(response);
        when(userProfileClientMock.findUser(any(), any(), any())).thenReturn(Response.builder().request(Request.create(Request.HttpMethod.GET, "", new HashMap<>(), Request.Body.empty())).body(body, Charset.defaultCharset()).status(200).build());
        assertThat(response).isNotNull();

        sut.updateUserProfileFeed(searchQuery);
        verify(profileUpdateService, times(1)).updateUserProfile(eq(searchQuery), eq("Bearer " + bearerToken), any(), any());
    }

}