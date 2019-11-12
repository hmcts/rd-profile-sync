package uk.gov.hmcts.reform.profilesync.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Request;
import feign.Response;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import org.junit.Test;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.profilesync.client.UserProfileClient;
import uk.gov.hmcts.reform.profilesync.domain.GetUserProfileResponse;
import uk.gov.hmcts.reform.profilesync.domain.IdamStatus;
import uk.gov.hmcts.reform.profilesync.domain.UserProfile;
import uk.gov.hmcts.reform.profilesync.domain.UserProfileSyncException;
import uk.gov.hmcts.reform.profilesync.helper.MockDataProvider;
import uk.gov.hmcts.reform.profilesync.service.UserAcquisitionService;

public class UserAcquisitionServiceImplTest {

    private UserProfileClient userProfileClientMock = Mockito.mock(UserProfileClient.class);

    private UserAcquisitionService sut = new UserAcquisitionServiceImpl(userProfileClientMock);

    @Test
    public void testFindUser() throws IOException {
        int statusCode = 200;
        String bearerToken = "Bearer ey093089r0e90e9f0jj9w00w-f90fsj0sf-fji0fsejs0";
        String s2sToken = "ey0f90sjaf90adjf90asjfsdljfklsf0sfj9s0d";
        String id = MockDataProvider.idamId.toString();

        UserProfile profile = UserProfile.builder().idamId(UUID.randomUUID().toString())
                                .email("email@org.com")
                                .firstName("firstName")
                                .lastName("lastName")
                                .idamStatus(IdamStatus.ACTIVE.name()).build();

        GetUserProfileResponse userProfileResponse = new GetUserProfileResponse(profile);

        ObjectMapper mapper = new ObjectMapper();

        String body = mapper.writeValueAsString(userProfileResponse);

        when(userProfileClientMock.findUser(any(), any(), any())).thenReturn(Response.builder().request(Request.create(Request.HttpMethod.GET, "", new HashMap<>(), Request.Body.empty())).body(body, Charset.defaultCharset()).status(200).build());
        Optional<GetUserProfileResponse> getUserProfileResponse = sut.findUser(bearerToken, s2sToken, id);

        assertThat(getUserProfileResponse).isNotNull();

    }


    @Test(expected = UserProfileSyncException.class)
    public void testFindUserThrowException() throws IOException {
        int statusCode = 200;
        String bearerToken = "Bearer ey093089r0e90e9f0jj9w00w-f90fsj0sf-fji0fsejs0";
        String s2sToken = "ey0f90sjaf90adjf90asjfsdljfklsf0sfj9s0d";
        String id = MockDataProvider.idamId.toString();

        UserProfile profile = UserProfile.builder().idamId(UUID.randomUUID().toString())
                .email("email@org.com")
                .firstName("firstName")
                .lastName("lastName")
                .idamStatus(IdamStatus.ACTIVE.name()).build();

        GetUserProfileResponse userProfileResponse = new GetUserProfileResponse(profile);

        ObjectMapper mapper = new ObjectMapper();

        String body = mapper.writeValueAsString(userProfileResponse);

        when(userProfileClientMock.findUser(any(), any(), any())).thenReturn(Response.builder().request(Request.create(Request.HttpMethod.GET, "", new HashMap<>(), Request.Body.empty())).body(body, Charset.defaultCharset()).status(400).build());

        Optional<GetUserProfileResponse> getUserProfileResponse = sut.findUser(bearerToken, s2sToken, id);

        assertThat(getUserProfileResponse).isNull();
        assertThat(getUserProfileResponse.isPresent()).isFalse();

    }
}