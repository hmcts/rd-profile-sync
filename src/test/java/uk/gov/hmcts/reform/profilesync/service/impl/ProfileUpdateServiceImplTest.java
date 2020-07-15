package uk.gov.hmcts.reform.profilesync.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Request;
import feign.Response;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.profilesync.client.IdamClient;
import uk.gov.hmcts.reform.profilesync.client.UserProfileClient;
import uk.gov.hmcts.reform.profilesync.constants.IdamStatus;
import uk.gov.hmcts.reform.profilesync.domain.UserProfile;
import uk.gov.hmcts.reform.profilesync.domain.response.GetUserProfileResponse;
import uk.gov.hmcts.reform.profilesync.repository.SyncJobRepository;
import uk.gov.hmcts.reform.profilesync.service.UserAcquisitionService;

public class ProfileUpdateServiceImplTest {

    private final UserProfileClient userProfileClientMock =
            Mockito.mock(UserProfileClient.class); //mocked as its an interface
    private final SyncJobRepository syncJobRepositoryMock =
            Mockito.mock(SyncJobRepository.class); //mocked as its an interface
    private final AuthTokenGenerator tokenGeneratorMock =
            Mockito.mock(AuthTokenGenerator.class); //mocked as its an interface
    private final UserAcquisitionService userAcquisitionServiceMock =
            Mockito.mock(UserAcquisitionService.class); //mocked as its an interface
    private final ProfileUpdateServiceImpl sut = new ProfileUpdateServiceImpl(userAcquisitionServiceMock,
            userProfileClientMock, syncJobRepositoryMock);

    private List<IdamClient.User> users;
    private IdamClient.User profile;
    private UserProfile userProfile;
    private GetUserProfileResponse getUserProfileResponse;
    private ObjectMapper mapper;
    private final String searchQuery = "lastModified:>now-24h";
    private final String bearerToken = "foobar";
    private final String s2sToken = "ey0somes2stoken";

    @Before
    public void setUp() {
        userProfile = UserProfile.builder().userIdentifier(UUID.randomUUID().toString())
                .email("email@org.com").firstName("firstName").lastName("lastName")
                .idamStatus(IdamStatus.ACTIVE.name()).build();
        getUserProfileResponse = new GetUserProfileResponse(userProfile);
        mapper = new ObjectMapper();

        profile = new IdamClient.User();
        profile.setActive(true);
        profile.setEmail("some@some.com");
        profile.setForename("some");
        profile.setId(UUID.randomUUID().toString());
        profile.setActive(true);
        profile.setSurname("kotla");

        users = new ArrayList<>();
        users.add(profile);
    }

    @Test
    public void testUpdateUserProfile() throws Exception {
        String body = mapper.writeValueAsString(getUserProfileResponse);

        when(userProfileClientMock.findUser(any(), any(), any())).thenReturn(Response.builder()
                .request(Request.create(Request.HttpMethod.GET, "", new HashMap<>(), Request.Body.empty(),
                        null)).body(body, Charset.defaultCharset()).status(200).build());
        when(tokenGeneratorMock.generate()).thenReturn(s2sToken);

        sut.updateUserProfile(searchQuery, bearerToken, s2sToken, users);

        verify(userAcquisitionServiceMock, times(1)).findUser(any(), any(), any());
    }

    @Test
    public void testUpdateUserProfileForOptional() throws Exception {
        when(userAcquisitionServiceMock.findUser(any(), any(), any())).thenReturn(Optional.of(getUserProfileResponse));
        when(tokenGeneratorMock.generate()).thenReturn(s2sToken);

        String body = mapper.writeValueAsString(userProfile);

        when(userProfileClientMock.syncUserStatus(any(), any(), any(), any())).thenReturn(Response.builder()
                .request(Request.create(Request.HttpMethod.PUT, "", new HashMap<>(), Request.Body.empty(),
                        null)).body(body, Charset.defaultCharset()).status(201).build());

        sut.updateUserProfile(searchQuery, bearerToken, s2sToken, users);

        verify(userAcquisitionServiceMock, times(1)).findUser(bearerToken, s2sToken,
                profile.getId());
    }

    @Test
    public void testUpdateUserProfileForOptional_WithStatus300() throws Exception {
        when(userAcquisitionServiceMock.findUser(any(), any(), any())).thenReturn(Optional.of(getUserProfileResponse));
        when(tokenGeneratorMock.generate()).thenReturn(s2sToken);

        String body = mapper.writeValueAsString(userProfile);

        when(userProfileClientMock.syncUserStatus(any(), any(), any(), any())).thenReturn(Response.builder()
                .request(Request.create(Request.HttpMethod.PUT, "", new HashMap<>(), Request.Body.empty(),
                        null)).body(body, Charset.defaultCharset()).status(300).build());

        sut.updateUserProfile(searchQuery, bearerToken, s2sToken, users);


        verify(userAcquisitionServiceMock, times(1)).findUser(bearerToken, s2sToken,
                profile.getId());
    }

    @Test(expected = Test.None.class)
    public void testUpdateUserProfileForOptionalThrowandCatchExp() throws Exception {
        when(userAcquisitionServiceMock.findUser(any(), any(), any())).thenReturn(Optional.of(getUserProfileResponse));
        when(tokenGeneratorMock.generate()).thenReturn(s2sToken);

        String body = mapper.writeValueAsString(userProfile);

        when(userProfileClientMock.syncUserStatus(any(), any(), any(), any())).thenReturn(Response.builder()
                .request(Request.create(Request.HttpMethod.PUT, "", new HashMap<>(), Request.Body.empty(),
                        null)).body(body, Charset.defaultCharset()).status(400).build());

        sut.updateUserProfile(searchQuery, bearerToken, s2sToken, users);

        verify(userAcquisitionServiceMock, times(1)).findUser(any(), any(), any());
        verify(syncJobRepositoryMock, times(1)).save(any());
        verify(userProfileClientMock, times(1)).syncUserStatus(any(), any(), any(), any());
    }

    @Test
    public void should_resolve_and_return_idam_status_by_idam_flags() {
        Map<Map<String, Boolean>, IdamStatus> idamStatusMap = new HashMap<Map<String, Boolean>, IdamStatus>();
        idamStatusMap.put(addRule(false, true), IdamStatus.PENDING);
        idamStatusMap.put(addRule(true, false), IdamStatus.ACTIVE);
        idamStatusMap.put(addRule(false, false), IdamStatus.SUSPENDED);

        Map<Map<String, Boolean>, IdamStatus> idamStatusMapResponse = sut.idamStatusResolver();

        assertThat(idamStatusMapResponse).isEqualTo(idamStatusMap);
        assertThat(idamStatusMap.get(createIdamRoleInfo(false, true))).isEqualTo(IdamStatus.PENDING);
        assertThat(idamStatusMap.get(createIdamRoleInfo(true, false))).isEqualTo(IdamStatus.ACTIVE);
        assertThat(idamStatusMap.get(createIdamRoleInfo(
                false, false))).isEqualTo(IdamStatus.SUSPENDED);
    }

    @Test
    public void should_resolve_and_return_add_rule() {
        Map<String, Boolean> pendingMapWithRules = new HashMap<>();
        pendingMapWithRules.put("ACTIVE", true);
        pendingMapWithRules.put("PENDING", false);

        Map<String, Boolean> pendingMapWithRulesResponse = sut.addRule(true, false);
        assertThat(pendingMapWithRulesResponse).isEqualTo(pendingMapWithRules);
    }

    private Map<String, Boolean> createIdamRoleInfo(boolean isActive, boolean isPending) {
        Map<String, Boolean> status = new HashMap<String, Boolean>();
        status.put(IdamStatus.ACTIVE.name(), isActive);
        status.put(IdamStatus.PENDING.name(), isPending);
        return status;
    }

    public Map<String, Boolean> addRule(boolean activeFlag, boolean pendingFlag) {
        Map<String, Boolean> pendingMapWithRules = new HashMap<>();
        pendingMapWithRules.put("ACTIVE", activeFlag);
        pendingMapWithRules.put("PENDING", pendingFlag);
        return pendingMapWithRules;
    }
}