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

import org.junit.Test;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.profilesync.client.IdamClient;
import uk.gov.hmcts.reform.profilesync.client.UserProfileClient;
import uk.gov.hmcts.reform.profilesync.domain.GetUserProfileResponse;
import uk.gov.hmcts.reform.profilesync.domain.IdamStatus;
import uk.gov.hmcts.reform.profilesync.domain.UserProfile;
import uk.gov.hmcts.reform.profilesync.repository.SyncJobRepository;
import uk.gov.hmcts.reform.profilesync.service.UserAcquisitionService;

public class ProfileUpdateServiceImplTest {

    private final UserProfileClient userProfileClientMock = Mockito.mock(UserProfileClient.class);

    private final UserAcquisitionService userAcquisitionServiceMock = Mockito.mock(UserAcquisitionService.class);

    private final SyncJobRepository syncJobRepositoryMock = Mockito.mock(SyncJobRepository.class);
    private final ProfileUpdateServiceImpl sut = new ProfileUpdateServiceImpl(userAcquisitionServiceMock,userProfileClientMock,syncJobRepositoryMock);

    private final AuthTokenGenerator tokenGeneratorMock = Mockito.mock(AuthTokenGenerator.class);

    @Test
    public void testUpdateUserProfile() throws Exception {
        final String searchQuery = "lastModified:>now-24h";
        final String bearerToken = "foobar";
        final String s2sToken = "ey0somes2stoken";
        IdamClient.User profile = new IdamClient.User();
        profile.setActive(true);
        profile.setEmail("some@some.com");
        profile.setForename("some");
        profile.setId(UUID.randomUUID().toString());
        profile.setActive(true);
        List<IdamClient.User> users = new ArrayList<>();
        users.add(profile);

        UserProfile userProfile = UserProfile.builder().idamId(UUID.randomUUID().toString())
                .email("email@org.com")
                .firstName("firstName")
                .lastName("lastName")
                .idamStatus(IdamStatus.ACTIVE.name()).build();

        GetUserProfileResponse userProfileResponse = new GetUserProfileResponse(userProfile);

        ObjectMapper mapper = new ObjectMapper();

        String body = mapper.writeValueAsString(userProfileResponse);

        when(userProfileClientMock.findUser(any(), any(), any())).thenReturn(Response.builder().request(Request.create(Request.HttpMethod.GET, "", new HashMap<>(), Request.Body.empty())).body(body, Charset.defaultCharset()).status(200).build());

        when(tokenGeneratorMock.generate()).thenReturn(s2sToken);

        sut.updateUserProfile(searchQuery, bearerToken, s2sToken, users);


        verify(userAcquisitionServiceMock, times(1)).findUser(any(), any(), any());


    }

    @Test
    public void testUpdateUserProfileForOptional() throws Exception {
        final String searchQuery = "lastModified:>now-24h";
        final String bearerToken = "foobar";
        final String s2sToken = "ey0somes2stoken";
        final List<IdamClient.User> users = new ArrayList<>();

        IdamClient.User profile = new IdamClient.User();
        profile.setActive(true);
        profile.setEmail("some@some.com");
        profile.setForename("some");
        profile.setId(UUID.randomUUID().toString());
        profile.setActive(true);
        profile.setSurname("kotla");
        users.add(profile);

        UserProfile userProfile = UserProfile.builder().idamId(UUID.randomUUID().toString())
                .email("email@org.com")
                .firstName("firstName")
                .lastName("lastName")
                .idamStatus(IdamStatus.ACTIVE.name()).build();

        GetUserProfileResponse userProfileResponse = new GetUserProfileResponse(userProfile);

        when(userAcquisitionServiceMock.findUser(any(), any(), any())).thenReturn(Optional.of(userProfileResponse));

        when(tokenGeneratorMock.generate()).thenReturn(s2sToken);

        ObjectMapper mapper = new ObjectMapper();

        String body = mapper.writeValueAsString(userProfile);

        when(userProfileClientMock.syncUserStatus(any(), any(), any(), any())).thenReturn(Response.builder().request(Request.create(Request.HttpMethod.PUT, "", new HashMap<>(), Request.Body.empty())).body(body, Charset.defaultCharset()).status(201).build());

        sut.updateUserProfile(searchQuery, bearerToken, s2sToken, users);

        verify(userAcquisitionServiceMock, times(1)).findUser(any(), any(), any());


    }


    @Test(expected = Test.None.class)
    public void testUpdateUserProfileForOptionalThrowandCatchExp() throws Exception {
        final String searchQuery = "lastModified:>now-24h";
        final String bearerToken = "foobar";
        final String s2sToken = "ey0somes2stoken";
        final List<IdamClient.User> users = new ArrayList<>();

        IdamClient.User profile = new IdamClient.User();
        profile.setActive(true);
        profile.setEmail("some@some.com");
        profile.setForename("some");
        profile.setId(UUID.randomUUID().toString());
        profile.setActive(true);
        profile.setSurname("kotla");
        users.add(profile);

        UserProfile userProfile = UserProfile.builder().idamId(UUID.randomUUID().toString())
                .email("email@org.com")
                .firstName("firstName")
                .lastName("lastName")
                .idamStatus(IdamStatus.ACTIVE.name()).build();

        GetUserProfileResponse userProfileResponse = new GetUserProfileResponse(userProfile);

        when(userAcquisitionServiceMock.findUser(any(), any(), any())).thenReturn(Optional.of(userProfileResponse));

        when(tokenGeneratorMock.generate()).thenReturn(s2sToken);

        ObjectMapper mapper = new ObjectMapper();

        String body = mapper.writeValueAsString(userProfile);

        when(userProfileClientMock.syncUserStatus(any(), any(), any(), any()))
                .thenReturn(Response.builder()
                        .request(Request.create(Request.HttpMethod.PUT, "", new HashMap<>(), Request.Body.empty()))
                        .body(body, Charset.defaultCharset())
                        .status(400).build());

        sut.updateUserProfile(searchQuery, bearerToken, s2sToken, users);

        verify(userAcquisitionServiceMock, times(1)).findUser(any(), any(), any());
        verify(syncJobRepositoryMock,times(1)).save(any());

    }

    @Test
    public void should_resolve_and_return_idam_status_by_idam_flags() {

        Map<Map<String, Boolean>, IdamStatus> idamStatusMap = new HashMap<Map<String, Boolean>, IdamStatus>();
        idamStatusMap.put(addRule(false,true, false), IdamStatus.PENDING);
        idamStatusMap.put(addRule(true, false,false), IdamStatus.ACTIVE);
        idamStatusMap.put(addRule(true, false,true), IdamStatus.ACTIVE_AND_LOCKED);
        idamStatusMap.put(addRule(false,false,false), IdamStatus.SUSPENDED);
        idamStatusMap.put(addRule(false,false,true), IdamStatus.SUSPENDED_AND_LOCKED);

        Map<Map<String, Boolean>, IdamStatus> idamStatusMapResponse = sut.idamStatusResolver();

        assertThat(idamStatusMapResponse).isEqualTo(idamStatusMap);
        assertThat(idamStatusMap.get(createIdamRoleInfo(false,true, false))).isEqualTo(IdamStatus.PENDING);
        assertThat(idamStatusMap.get(createIdamRoleInfo(true,false, false))).isEqualTo(IdamStatus.ACTIVE);
        assertThat(idamStatusMap.get(createIdamRoleInfo(true,false, true))).isEqualTo(IdamStatus.ACTIVE_AND_LOCKED);
        assertThat(idamStatusMap.get(createIdamRoleInfo(false,false, false))).isEqualTo(IdamStatus.SUSPENDED);
        assertThat(idamStatusMap.get(createIdamRoleInfo(false,false, true))).isEqualTo(IdamStatus.SUSPENDED_AND_LOCKED);
    }

    @Test
    public void should_resolve_and_return_add_rule() {


        Map<String, Boolean> pendingMapWithRules = new HashMap<>();
        pendingMapWithRules.put("ACTIVE", true);
        pendingMapWithRules.put("PENDING", false);
        pendingMapWithRules.put("LOCKED", false);

        Map<String, Boolean> pendingMapWithRulesResponse = sut.addRule(true,false,false);
        assertThat(pendingMapWithRulesResponse).isEqualTo(pendingMapWithRules);
    }

    private  Map<String, Boolean>  createIdamRoleInfo(boolean isActive, boolean isPending, boolean isLocked) {

        Map<String, Boolean> status = new HashMap<String, Boolean>();
        status.put(IdamStatus.ACTIVE.name(), isActive);
        status.put(IdamStatus.PENDING.name(), isPending);
        status.put(IdamStatus.LOCKED.name(), isLocked);
        return status;
    }


    public Map<String, Boolean> addRule(boolean activeFlag, boolean pendingFlag, boolean lockedFlag) {
        Map<String, Boolean> pendingMapWithRules = new HashMap<>();
        pendingMapWithRules.put("ACTIVE", activeFlag);
        pendingMapWithRules.put("PENDING", pendingFlag);
        pendingMapWithRules.put("LOCKED", lockedFlag);
        return pendingMapWithRules;
    }

}