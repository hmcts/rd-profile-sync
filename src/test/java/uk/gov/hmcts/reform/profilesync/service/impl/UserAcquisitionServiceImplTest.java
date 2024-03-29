package uk.gov.hmcts.reform.profilesync.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.profilesync.advice.UserProfileSyncException;
import uk.gov.hmcts.reform.profilesync.client.UserProfileClient;
import uk.gov.hmcts.reform.profilesync.constants.IdamStatus;
import uk.gov.hmcts.reform.profilesync.domain.UserProfile;
import uk.gov.hmcts.reform.profilesync.domain.response.GetUserProfileResponse;
import uk.gov.hmcts.reform.profilesync.service.UserAcquisitionService;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.profilesync.helper.MockDataProvider.IDAM_ID;

class UserAcquisitionServiceImplTest {

    private final UserProfileClient userProfileClientMock = Mockito.mock(UserProfileClient.class);
    //mocked as its an interface
    private final UserAcquisitionService sut = new UserAcquisitionServiceImpl(userProfileClientMock,
            "RD_Profile_Sync");

    private UserProfile profile;
    private GetUserProfileResponse userProfileResponse;
    private ObjectMapper mapper;
    private String bearerToken;
    private String s2sToken;
    private String id;

    @BeforeEach
    public void setUp() {
        profile = UserProfile.builder().userIdentifier(UUID.randomUUID().toString())
                .email("email@org.com")
                .firstName("firstName")
                .lastName("lastName")
                .idamStatus(IdamStatus.ACTIVE.name()).build();

        userProfileResponse = new GetUserProfileResponse(profile);
        mapper = new ObjectMapper();

        bearerToken = "Bearer ey093089r0e90e9f0jj9w00w-f90fsj0sf-fji0fsejs0";
        s2sToken = "ey0f90sjaf90adjf90asjfsdljfklsf0sfj9s0d";
        id = IDAM_ID;
    }

    @Test
    void findUser() throws IOException {
        String body = mapper.writeValueAsString(userProfileResponse);

        when(userProfileClientMock.findUser(any(), any(), any())).thenReturn(Response.builder()
                .request(Request.create(Request.HttpMethod.GET, "", new HashMap<>(), Request.Body.empty(),
                        null)).body(body, Charset.defaultCharset()).status(200).build());
        Optional<GetUserProfileResponse> getUserProfileResponse = sut.findUser(bearerToken, s2sToken, id);

        assertThat(getUserProfileResponse).isNotNull();
        Assertions.assertTrue(getUserProfileResponse.isPresent());
        assertThat(getUserProfileResponse.get().getEmail()).isEqualTo(profile.getEmail());
        assertThat(getUserProfileResponse.get().getFirstName()).isEqualTo(profile.getFirstName());
        assertThat(getUserProfileResponse.get().getLastName()).isEqualTo(profile.getLastName());
        assertThat(getUserProfileResponse.get().getIdamStatus()).isEqualTo(profile.getIdamStatus());
        verify(userProfileClientMock, times(1)).findUser(any(), any(), any());
    }

    @Test
    void shouldReturn404OnFindUser() throws IOException {
        String body = mapper.writeValueAsString(userProfileResponse);

        when(userProfileClientMock.findUser(any(), any(), any())).thenReturn(Response.builder()
            .request(Request.create(Request.HttpMethod.GET, "", new HashMap<>(), Request.Body.empty(),
                null)).body(body, Charset.defaultCharset()).status(404).build());
        Optional<GetUserProfileResponse> getUserProfileResponse = sut.findUser(bearerToken, s2sToken, id);

        assertThat(getUserProfileResponse).isEmpty();

    }

    @Test
    void findUserThrowExceptionWith400() throws IOException {
        String body = mapper.writeValueAsString(userProfileResponse);
        when(userProfileClientMock.findUser(any(), any(), any())).thenReturn(Response.builder()
                .request(Request.create(Request.HttpMethod.GET, "", new HashMap<>(), Request.Body.empty(),
                        null)).body(body, Charset.defaultCharset()).status(400).build());

        assertThrows(UserProfileSyncException.class, () -> sut.findUser(bearerToken, s2sToken, id));
        verify(userProfileClientMock, times(1)).findUser(any(), any(), any());
    }


    @Test
    void findUserThrowExceptionWith401WithNoBody() {
        when(userProfileClientMock.findUser(any(), any(), any())).thenReturn(Response.builder()
                .request(Request.create(Request.HttpMethod.GET, "", new HashMap<>(), Request.Body.empty(),
                        null)).body(null, Charset.defaultCharset()).status(401)
                .reason("Un Authorized").build());
        assertThrows(UserProfileSyncException.class, () -> sut.findUser(bearerToken, s2sToken, id));
        verify(userProfileClientMock, times(1)).findUser(any(), any(), any());
    }

    @Test
    void findUserThrowExceptionWith500() {
        doThrow(UserProfileSyncException.class).when(userProfileClientMock).findUser(any(), any(), any());
        assertThrows(UserProfileSyncException.class, () -> sut.findUser(bearerToken, s2sToken, id));
    }
}
