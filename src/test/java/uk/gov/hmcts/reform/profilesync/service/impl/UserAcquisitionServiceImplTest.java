package uk.gov.hmcts.reform.profilesync.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.profilesync.helper.MockDataProvider.IDAM_ID;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Request;
import feign.Response;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.profilesync.advice.UserProfileSyncException;
import uk.gov.hmcts.reform.profilesync.client.UserProfileClient;
import uk.gov.hmcts.reform.profilesync.constants.IdamStatus;
import uk.gov.hmcts.reform.profilesync.domain.UserProfile;
import uk.gov.hmcts.reform.profilesync.domain.response.GetUserProfileResponse;
import uk.gov.hmcts.reform.profilesync.service.UserAcquisitionService;

public class UserAcquisitionServiceImplTest {

    private UserProfileClient userProfileClientMock = Mockito.mock(UserProfileClient.class); //mocked as its an interface
    private UserAcquisitionService sut = new UserAcquisitionServiceImpl(userProfileClientMock);

    private UserProfile profile;
    private GetUserProfileResponse userProfileResponse;
    private ObjectMapper mapper;
    private String bearerToken;
    private String s2sToken;
    private String id;

    @Before
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
    public void testFindUser() throws IOException {
        String body = mapper.writeValueAsString(userProfileResponse);

        when(userProfileClientMock.findUser(any(), any(), any())).thenReturn(Response.builder().request(Request.create(Request.HttpMethod.GET, "", new HashMap<>(), Request.Body.empty(), null)).body(body, Charset.defaultCharset()).status(200).build());
        Optional<GetUserProfileResponse> getUserProfileResponse = sut.findUser(bearerToken, s2sToken, id);

        assertThat(getUserProfileResponse).isNotNull();
        assertThat(getUserProfileResponse.get().getEmail()).isEqualTo(profile.getEmail());
        assertThat(getUserProfileResponse.get().getFirstName()).isEqualTo(profile.getFirstName());
        assertThat(getUserProfileResponse.get().getLastName()).isEqualTo(profile.getLastName());
        assertThat(getUserProfileResponse.get().getIdamStatus()).isEqualTo(profile.getIdamStatus());
        verify(userProfileClientMock, times(1)).findUser(any(), any(), any());
    }


    @Test(expected = UserProfileSyncException.class)
    public void testFindUserThrowExceptionWith400() throws IOException {
        String body = mapper.writeValueAsString(userProfileResponse);
        when(userProfileClientMock.findUser(any(), any(), any())).thenReturn(Response.builder().request(Request.create(Request.HttpMethod.GET, "", new HashMap<>(), Request.Body.empty(), null)).body(body, Charset.defaultCharset()).status(400).build());

        Optional<GetUserProfileResponse> getUserProfileResponse = sut.findUser(bearerToken, s2sToken, id);

        assertThat(getUserProfileResponse).isNull();
        assertThat(getUserProfileResponse.isPresent()).isFalse();
        verify(userProfileClientMock, times(1)).findUser(any(), any(), any());
    }

    @Test(expected = UserProfileSyncException.class)
    public void testFindUserThrowExceptionWith500() throws IOException {
        doThrow(UserProfileSyncException.class).when(userProfileClientMock).findUser(any(), any(), any());
        sut.findUser(bearerToken, s2sToken, id);
    }
}