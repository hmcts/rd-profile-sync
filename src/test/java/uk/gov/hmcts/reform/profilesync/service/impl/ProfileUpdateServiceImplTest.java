package uk.gov.hmcts.reform.profilesync.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Request;
import feign.Response;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.profilesync.client.IdamClient;
import uk.gov.hmcts.reform.profilesync.client.UserProfileClient;
import uk.gov.hmcts.reform.profilesync.constants.IdamStatus;
import uk.gov.hmcts.reform.profilesync.domain.ProfileSyncAudit;
import uk.gov.hmcts.reform.profilesync.domain.UserProfile;
import uk.gov.hmcts.reform.profilesync.domain.response.GetUserProfileResponse;
import uk.gov.hmcts.reform.profilesync.service.UserAcquisitionService;

public class ProfileUpdateServiceImplTest {
    //mocked as its an interfaces
    private final UserProfileClient userProfileClientMock = Mockito.mock(UserProfileClient.class);
    private final AuthTokenGenerator tokenGeneratorMock = Mockito.mock(AuthTokenGenerator.class);
    private final UserAcquisitionService userAcquisitionServiceMock = Mockito.mock(UserAcquisitionService.class);
    private final ProfileUpdateServiceImpl sut = new ProfileUpdateServiceImpl(userAcquisitionServiceMock,
            userProfileClientMock, "RD_Profile_Sync");
    private ProfileSyncAudit profileSyncAuditMock = mock(ProfileSyncAudit.class);

    private Set<IdamClient.User> users;
    private IdamClient.User profile;
    private UserProfile userProfile;
    private GetUserProfileResponse getUserProfileResponse;
    private ObjectMapper mapper;
    private final String searchQuery = "lastModified:>now-24h";
    private final String bearerToken = "foobar";
    private final String s2sToken = "ey0somes2stoken";

    @Before
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

        users = new HashSet<>();
        users.add(profile);
    }

    @Test
    public void updateUserProfile() throws Exception {
        String body = mapper.writeValueAsString(getUserProfileResponse);

        when(userProfileClientMock.findUser(any(), any(), any())).thenReturn(Response.builder()
                .request(Request.create(Request.HttpMethod.GET, "", new HashMap<>(), Request.Body.empty(),
                        null)).body(body, Charset.defaultCharset()).status(200).build());
        when(tokenGeneratorMock.generate()).thenReturn(s2sToken);

        ProfileSyncAudit profileSyncAudit =  sut.updateUserProfile(searchQuery, bearerToken, s2sToken, users,
                profileSyncAuditMock);
        assertThat(profileSyncAudit).isNotNull();
        verify(userAcquisitionServiceMock, times(1)).findUser(any(), any(), any());
        verify(profileSyncAuditMock,times(1)).setProfileSyncAuditDetails(any());
    }

    @Test
    public void updateUserProfileForOptional() throws Exception {
        when(userAcquisitionServiceMock.findUser(any(), any(), any())).thenReturn(Optional.of(getUserProfileResponse));
        when(tokenGeneratorMock.generate()).thenReturn(s2sToken);

        String body = mapper.writeValueAsString(userProfile);

        when(userProfileClientMock.syncUserStatus(any(), any(), any(), any())).thenReturn(Response.builder()
                .request(Request.create(Request.HttpMethod.PUT, "", new HashMap<>(), Request.Body.empty(),
                        null)).body(body, Charset.defaultCharset()).status(201).build());

        sut.updateUserProfile(searchQuery, bearerToken, s2sToken, users, profileSyncAuditMock);

        verify(userAcquisitionServiceMock, times(1)).findUser(bearerToken, s2sToken,
                profile.getId());
    }

    @Test(expected = Test.None.class)
    public void updateUserProfileForOptionalThrowandCatchExp() throws Exception {
        when(userAcquisitionServiceMock.findUser(any(), any(), any())).thenReturn(Optional.of(getUserProfileResponse));
        when(tokenGeneratorMock.generate()).thenReturn(s2sToken);

        String body = mapper.writeValueAsString(userProfile);

        when(userProfileClientMock.syncUserStatus(any(), any(), any(), any())).thenReturn(Response.builder()
                .request(Request.create(Request.HttpMethod.PUT, "", new HashMap<>(), Request.Body.empty(),
                        null)).body(body, Charset.defaultCharset()).status(400).build());

        sut.updateUserProfile(searchQuery, bearerToken, s2sToken, users, profileSyncAuditMock);

        verify(userAcquisitionServiceMock, times(1)).findUser(any(), any(), any());
        verify(userProfileClientMock, times(1)).syncUserStatus(any(), any(), any(), any());
    }

    @Test
    public void updateUserProfileForOptionalWithStatus300() throws Exception {
        when(userAcquisitionServiceMock.findUser(any(), any(), any())).thenReturn(Optional.of(getUserProfileResponse));
        when(tokenGeneratorMock.generate()).thenReturn(s2sToken);

        String body = mapper.writeValueAsString(userProfile);

        when(userProfileClientMock.syncUserStatus(any(), any(), any(), any())).thenReturn(Response.builder()
                .request(Request.create(Request.HttpMethod.PUT, "", new HashMap<>(), Request.Body.empty(),
                        null)).body(body, Charset.defaultCharset()).status(300).build());

        sut.updateUserProfile(searchQuery, bearerToken, s2sToken, users, profileSyncAuditMock);

        verify(userAcquisitionServiceMock, times(1)).findUser(bearerToken, s2sToken,
                profile.getId());
    }

    @Test
    public void updateUserProfileForOptionalWithStatus401() throws Exception {
        when(userAcquisitionServiceMock.findUser(any(), any(), any())).thenReturn(Optional.of(getUserProfileResponse));
        when(tokenGeneratorMock.generate()).thenReturn(s2sToken);

        when(userProfileClientMock.syncUserStatus(any(), any(), any(), any())).thenReturn(Response.builder()
                .request(Request.create(Request.HttpMethod.PUT, "", new HashMap<>(), Request.Body.empty(),
                        null)).body(null, Charset.defaultCharset()).status(401)
                .reason("Un Authorized").build());

        profileSyncAuditMock = sut.updateUserProfile(searchQuery, bearerToken, s2sToken, users, profileSyncAuditMock);
        assertThat(profileSyncAuditMock).isNotNull();

        verify(profileSyncAuditMock, times(1)).setSchedulerStatus(any());
        verify(userAcquisitionServiceMock, times(1)).findUser(bearerToken, s2sToken,
                profile.getId());
    }

    @Test
    public void shouldResolveAndReturnIdamStatusByIdamFlagsActive() {
        StringBuilder sb = new StringBuilder();
        sb.append("true");
        sb.append("false");
        String status = sut.resolveIdamStatus(sb);
        assertThat(status).isEqualTo(IdamStatus.ACTIVE.name());
        assertThat(status).isNotEqualTo(IdamStatus.PENDING.name());
        assertThat(status).isNotEqualTo(IdamStatus.SUSPENDED.name());
    }

    @Test
    public void shouldResolveAndReturnIdamStatusByIdamFlagsPending() {
        StringBuilder sb = new StringBuilder();
        sb.append("false");
        sb.append("true");
        String status = sut.resolveIdamStatus(sb);
        assertThat(status).isNotEqualTo(IdamStatus.ACTIVE.name());
        assertThat(status).isEqualTo(IdamStatus.PENDING.name());
        assertThat(status).isNotEqualTo(IdamStatus.SUSPENDED.name());
    }

    @Test
    public void shouldResolveAndReturnIdamStatusByIdamFlagsSuspending() {
        StringBuilder sb = new StringBuilder();
        String status = sut.resolveIdamStatus(sb);
        assertThat(status).isEqualTo(IdamStatus.SUSPENDED.name());
        assertThat(status).isNotEqualTo(IdamStatus.ACTIVE.name());
        assertThat(status).isNotEqualTo(IdamStatus.PENDING.name());
    }
}