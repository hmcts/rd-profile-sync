package uk.gov.hmcts.reform.profilesync.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.profilesync.advice.UserProfileSyncException;
import uk.gov.hmcts.reform.profilesync.client.CaseWorkerRefApiClient;
import uk.gov.hmcts.reform.profilesync.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.profilesync.domain.response.GetCaseWorkerProfileResponse;
import uk.gov.hmcts.reform.profilesync.service.CaseWorkerAcquisitionService;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.profilesync.helper.MockDataProvider.IDAM_ID;

class CaseWorkerAcquisitionServiceImplTest {

    private final CaseWorkerRefApiClient caseWorkerRefApiClientMock = Mockito.mock(CaseWorkerRefApiClient.class);

    private final CaseWorkerAcquisitionService sut = new CaseWorkerAcquisitionServiceImpl(caseWorkerRefApiClientMock,
            "RD_Profile_Sync");

    private CaseWorkerProfile profile;
    private GetCaseWorkerProfileResponse caseWorkerProfileResponse;
    private ObjectMapper mapper;
    private String bearerToken;
    private String s2sToken;
    private String id;

    @BeforeEach
    public void setUp() {
        profile = CaseWorkerProfile.builder()
                .userId(UUID.randomUUID().toString())
                .email("email@org.com")
                .firstName("firstName")
                .lastName("lastName")
                .idamStatus(true).build();

        caseWorkerProfileResponse = new GetCaseWorkerProfileResponse(profile);
        mapper = new ObjectMapper();

        bearerToken = "Bearer ey093089r0e90e9f0jj9w00w-f90fsj0sf-fji0fsejs0";
        s2sToken = "ey0f90sjaf90adjf90asjfsdljfklsf0sfj9s0d";
        id = IDAM_ID;
    }

    @Test
    void findCaseWorkerUser() throws IOException {
        String body = mapper.writeValueAsString(caseWorkerProfileResponse);

        when(caseWorkerRefApiClientMock.findCaseWorkerUser(any(), any(), any())).thenReturn(Response.builder()
                .request(Request.create(Request.HttpMethod.GET, "", new HashMap<>(), Request.Body.empty(),
                        null)).body(body, Charset.defaultCharset()).status(200).build());
        Optional<GetCaseWorkerProfileResponse> getCaseWorkerProfileResponse = sut
                                                                        .findCaseWorkerUser(bearerToken, s2sToken, id);

        assertThat(getCaseWorkerProfileResponse).isNotNull();
        assertTrue(getCaseWorkerProfileResponse.isPresent());
        assertThat(getCaseWorkerProfileResponse.get().getEmail()).isEqualTo(profile.getEmail());
        assertThat(getCaseWorkerProfileResponse.get().getFirstName()).isEqualTo(profile.getFirstName());
        assertThat(getCaseWorkerProfileResponse.get().getLastName()).isEqualTo(profile.getLastName());
        assertTrue(getCaseWorkerProfileResponse.get().isIdamStatus());
        verify(caseWorkerRefApiClientMock, times(1)).findCaseWorkerUser(any(),any(),any());
    }

    @Test
    void shouldReturn404OnFindUser() throws IOException {
        String body = mapper.writeValueAsString(caseWorkerProfileResponse);

        when(caseWorkerRefApiClientMock.findCaseWorkerUser(any(), any(), any())).thenReturn(Response.builder()
                .request(Request.create(Request.HttpMethod.GET, "", new HashMap<>(), Request.Body.empty(),
                        null)).body(body, Charset.defaultCharset()).status(404).build());
        Optional<GetCaseWorkerProfileResponse> getCaseWorkerProfileResponse = sut
                                                                    .findCaseWorkerUser(bearerToken, s2sToken, id);

        assertThat(getCaseWorkerProfileResponse).isEmpty();

    }

    @Test
    void findUserThrowExceptionWith400() throws IOException {
        String body = mapper.writeValueAsString(caseWorkerProfileResponse);
        when(caseWorkerRefApiClientMock.findCaseWorkerUser(any(), any(), any())).thenReturn(Response.builder()
                .request(Request.create(Request.HttpMethod.GET, "", new HashMap<>(), Request.Body.empty(),
                        null)).body(body, Charset.defaultCharset()).status(400).build());

        assertThrows(UserProfileSyncException.class, () -> sut.findCaseWorkerUser(bearerToken, s2sToken, id));
        verify(caseWorkerRefApiClientMock, times(1)).findCaseWorkerUser(any(), any(), any());
    }

    @Test
    void findUserThrowExceptionWith401WithNoBody() {
        when(caseWorkerRefApiClientMock.findCaseWorkerUser(any(), any(), any())).thenReturn(Response.builder()
                .request(Request.create(Request.HttpMethod.GET, "", new HashMap<>(), Request.Body.empty(),
                        null)).body(null, Charset.defaultCharset()).status(401)
                .reason("Un Authorized").build());
        assertThrows(UserProfileSyncException.class, () -> sut.findCaseWorkerUser(bearerToken, s2sToken, id));
        verify(caseWorkerRefApiClientMock, times(1)).findCaseWorkerUser(any(), any(), any());
    }

    @Test
    void findUserThrowExceptionWith500() {
        doThrow(UserProfileSyncException.class).when(caseWorkerRefApiClientMock)
                .findCaseWorkerUser(any(), any(), any());
        assertThrows(UserProfileSyncException.class, () -> sut.findCaseWorkerUser(bearerToken, s2sToken, id));
    }
}
