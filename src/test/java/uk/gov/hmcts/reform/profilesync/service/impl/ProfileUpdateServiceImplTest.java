package uk.gov.hmcts.reform.profilesync.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.profilesync.client.CaseWorkerRefApiClient;
import uk.gov.hmcts.reform.profilesync.client.IdamClient;
import uk.gov.hmcts.reform.profilesync.client.UserProfileClient;
import uk.gov.hmcts.reform.profilesync.constants.IdamStatus;
import uk.gov.hmcts.reform.profilesync.domain.ProfileSyncAudit;
import uk.gov.hmcts.reform.profilesync.domain.UserProfile;
import uk.gov.hmcts.reform.profilesync.domain.response.GetUserProfileResponse;
import uk.gov.hmcts.reform.profilesync.service.UserAcquisitionService;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProfileUpdateServiceImplTest {
    //mocked as its an interfaces
    private final UserProfileClient userProfileClientMock = Mockito.mock(UserProfileClient.class);
    private final AuthTokenGenerator tokenGeneratorMock = Mockito.mock(AuthTokenGenerator.class);
    private final UserAcquisitionService userAcquisitionServiceMock = Mockito.mock(UserAcquisitionService.class);

    private final CaseWorkerRefApiClient caseWorkerRefApiClientMock = Mockito.mock(CaseWorkerRefApiClient.class);
    private final ProfileUpdateServiceImpl sut = new ProfileUpdateServiceImpl(userAcquisitionServiceMock,
            userProfileClientMock, caseWorkerRefApiClientMock,"RD_Profile_Sync");
    private ProfileSyncAudit profileSyncAuditMock = mock(ProfileSyncAudit.class);

    private Set<IdamClient.User> users;
    private IdamClient.User profile;
    private UserProfile userProfile;
    private GetUserProfileResponse getUserProfileResponse;
    private ObjectMapper mapper;
    private final String searchQuery = "(roles:prd-admin) AND lastModified:>now-24h";
    private final String bearerToken = "foobar";
    private final String s2sToken = "ey0somes2stoken";

    @BeforeEach
    public void setUp() {
        userProfile = UserProfile.builder().userIdentifier(UUID.randomUUID().toString()).email("email@org.com")
                .firstName("firstName").lastName("lastName").idamStatus(IdamStatus.ACTIVE.name()).build();
        getUserProfileResponse = new GetUserProfileResponse(userProfile);
        mapper = new ObjectMapper();
        profile = new IdamClient.User();
        profile.setEmail("some@some.com");
        profile.setForename("some");
        profile.setId(UUID.randomUUID().toString());
        profile.setActive(true);
        profile.setSurname("kotla");

        List<String> roles = new ArrayList<>();
        roles.add("prd-admin");
        roles.add("staff-admin");

        profile.setRoles(roles);

        users = new HashSet<>();
        users.add(profile);
    }

    @Test
    void updateUserProfile() throws Exception {
        String body = mapper.writeValueAsString(getUserProfileResponse);

        when(userProfileClientMock.findUser(any(), any(), any())).thenReturn(Response.builder()
                .request(Request.create(Request.HttpMethod.GET, "", new HashMap<>(), Request.Body.empty(),
                        null)).body(body, Charset.defaultCharset()).status(200).build());
        when(tokenGeneratorMock.generate()).thenReturn(s2sToken);

        when(caseWorkerRefApiClientMock.syncCaseWorkerUserStatus(any(), any(), any())).thenReturn(Response.builder()
                .request(Request.create(Request.HttpMethod.PUT, "", new HashMap<>(), Request.Body.empty(),
                        null)).body(body, Charset.defaultCharset()).status(201).build());

        ProfileSyncAudit profileSyncAudit = sut.updateUserProfile(searchQuery, bearerToken, s2sToken, users,
                profileSyncAuditMock);
        assertThat(profileSyncAudit).isNotNull();
        verify(userAcquisitionServiceMock, times(1)).findUser(any(), any(), any());
        verify(profileSyncAuditMock, times(2)).setProfileSyncAuditDetails(any());
    }

    @Test
    void updateUserProfileForOptional() throws Exception {
        when(userAcquisitionServiceMock.findUser(any(), any(), any())).thenReturn(Optional.of(getUserProfileResponse));
        when(tokenGeneratorMock.generate()).thenReturn(s2sToken);

        String body = mapper.writeValueAsString(userProfile);

        when(userProfileClientMock.syncUserStatus(any(), any(), any(), any())).thenReturn(Response.builder()
                .request(Request.create(Request.HttpMethod.PUT, "", new HashMap<>(), Request.Body.empty(),
                        null)).body(body, Charset.defaultCharset()).status(201).build());

        when(caseWorkerRefApiClientMock.syncCaseWorkerUserStatus(any(), any(), any())).thenReturn(Response.builder()
                .request(Request.create(Request.HttpMethod.PUT, "", new HashMap<>(), Request.Body.empty(),
                        null)).body(body, Charset.defaultCharset()).status(201).build());

        sut.updateUserProfile(searchQuery, bearerToken, s2sToken, users, profileSyncAuditMock);

        verify(userAcquisitionServiceMock, times(1)).findUser(bearerToken, s2sToken,
                profile.getId());
    }

    @Test
    void updateUserProfileForOptionalThrowandCatchExp() throws Exception {
        when(userAcquisitionServiceMock.findUser(any(), any(), any())).thenReturn(Optional.of(getUserProfileResponse));
        when(tokenGeneratorMock.generate()).thenReturn(s2sToken);

        String body = mapper.writeValueAsString(userProfile);

        when(userProfileClientMock.syncUserStatus(any(), any(), any(), any())).thenReturn(Response.builder()
                .request(Request.create(Request.HttpMethod.PUT, "", new HashMap<>(), Request.Body.empty(),
                        null)).body(body, Charset.defaultCharset()).status(400).build());

        when(caseWorkerRefApiClientMock.syncCaseWorkerUserStatus(any(), any(), any())).thenReturn(Response.builder()
                .request(Request.create(Request.HttpMethod.PUT, "", new HashMap<>(), Request.Body.empty(),
                        null)).body(body, Charset.defaultCharset()).status(400).build());

        sut.updateUserProfile(searchQuery, bearerToken, s2sToken, users, profileSyncAuditMock);

        verify(userAcquisitionServiceMock, times(1)).findUser(any(), any(), any());
        verify(userProfileClientMock, times(1)).syncUserStatus(any(), any(), any(), any());
    }

    @Test
    void updateUserProfileForOptionalWithStatus300() throws Exception {
        when(userAcquisitionServiceMock.findUser(any(), any(), any())).thenReturn(Optional.of(getUserProfileResponse));
        when(tokenGeneratorMock.generate()).thenReturn(s2sToken);

        String body = mapper.writeValueAsString(userProfile);

        when(userProfileClientMock.syncUserStatus(any(), any(), any(), any())).thenReturn(Response.builder()
                .request(Request.create(Request.HttpMethod.PUT, "", new HashMap<>(), Request.Body.empty(),
                        null)).body(body, Charset.defaultCharset()).status(300).build());

        when(caseWorkerRefApiClientMock.syncCaseWorkerUserStatus(any(), any(), any())).thenReturn(Response.builder()
                .request(Request.create(Request.HttpMethod.PUT, "", new HashMap<>(), Request.Body.empty(),
                        null)).body(body, Charset.defaultCharset()).status(300).build());

        sut.updateUserProfile(searchQuery, bearerToken, s2sToken, users, profileSyncAuditMock);

        verify(userAcquisitionServiceMock, times(1)).findUser(bearerToken, s2sToken,
                profile.getId());
    }

    @Test
    void updateUserProfileForOptionalWithStatus401() throws Exception {
        when(userAcquisitionServiceMock.findUser(any(), any(), any())).thenReturn(Optional.of(getUserProfileResponse));
        when(tokenGeneratorMock.generate()).thenReturn(s2sToken);

        when(userProfileClientMock.syncUserStatus(any(), any(), any(), any())).thenReturn(Response.builder()
                .request(Request.create(Request.HttpMethod.PUT, "", new HashMap<>(), Request.Body.empty(),
                        null)).body(null, Charset.defaultCharset()).status(401)
                .reason("Un Authorized").build());

        when(caseWorkerRefApiClientMock.syncCaseWorkerUserStatus(any(), any(), any())).thenReturn(Response.builder()
                .request(Request.create(Request.HttpMethod.PUT, "", new HashMap<>(), Request.Body.empty(),
                        null)).body(null, Charset.defaultCharset()).status(401).build());

        profileSyncAuditMock = sut.updateUserProfile(searchQuery, bearerToken, s2sToken, users, profileSyncAuditMock);
        assertThat(profileSyncAuditMock).isNotNull();

        verify(profileSyncAuditMock, times(2)).setSchedulerStatus(any());
        verify(userAcquisitionServiceMock, times(1)).findUser(bearerToken, s2sToken,
                profile.getId());
    }

    @Test
    void shouldResolveAndReturnIdamStatusByIdamFlagsActive() {
        StringBuilder sb = new StringBuilder();
        sb.append("true");
        sb.append("false");
        String status = sut.resolveIdamStatus(sb);
        assertThat(status)
                .isEqualTo(IdamStatus.ACTIVE.name())
                .isNotEqualTo(IdamStatus.PENDING.name())
                .isNotEqualTo(IdamStatus.SUSPENDED.name());
    }

    @Test
    void shouldResolveAndReturnIdamStatusByIdamFlagsPending() {
        StringBuilder sb = new StringBuilder();
        sb.append("false");
        sb.append("true");
        String status = sut.resolveIdamStatus(sb);
        assertThat(status)
                .isNotEqualTo(IdamStatus.ACTIVE.name())
                .isEqualTo(IdamStatus.PENDING.name())
                .isNotEqualTo(IdamStatus.SUSPENDED.name());
    }

    @Test
    void shouldResolveAndReturnIdamStatusByIdamFlagsSuspending() {
        StringBuilder sb = new StringBuilder();
        String status = sut.resolveIdamStatus(sb);
        assertThat(status)
                .isEqualTo(IdamStatus.SUSPENDED.name())
                .isNotEqualTo(IdamStatus.ACTIVE.name())
                .isNotEqualTo(IdamStatus.PENDING.name());
    }
}
