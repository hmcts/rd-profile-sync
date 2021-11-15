package uk.gov.hmcts.reform.profilesync;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.profilesync.client.IdamClient;
import uk.gov.hmcts.reform.profilesync.client.UserProfileClient;
import uk.gov.hmcts.reform.profilesync.constants.IdamStatus;
import uk.gov.hmcts.reform.profilesync.repository.ProfileSyncAuditDetailsRepository;
import uk.gov.hmcts.reform.profilesync.repository.ProfileSyncAuditRepository;
import uk.gov.hmcts.reform.profilesync.repository.ProfileSyncConfigRepository;
import uk.gov.hmcts.reform.profilesync.schedular.UserProfileSyncJobScheduler;
import uk.gov.hmcts.reform.profilesync.wiremock.WireMockExtension;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;

@Configuration
public abstract class AuthorizationEnabledIntTest extends SpringBootIntTest {

    @Autowired
    protected UserProfileClient userProfileFeignClient;

    @Autowired
    protected IdamClient idamFeignClient;

    @Autowired
    protected UserProfileSyncJobScheduler profileSyncJobScheduler;

    @Autowired
    protected ProfileSyncConfigRepository profileSyncConfigRepository;

    @Autowired
    protected ProfileSyncAuditRepository profileSyncAuditRepository;

    @Autowired
    protected ProfileSyncAuditDetailsRepository profileSyncAuditDetailsRepository;

    @RegisterExtension
    protected WireMockExtension userProfileService = new WireMockExtension(8091);

    @RegisterExtension
    protected WireMockExtension sidamService = new WireMockExtension(5000);

    @RegisterExtension
    protected WireMockExtension s2sService = new WireMockExtension(8990);

    @BeforeEach
    public void setupS2sAndIdamStubs() throws Exception {

        s2sService.stubFor(get(urlEqualTo("/details"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("rd_profile_sync")));

        s2sService.stubFor(WireMock.post(urlEqualTo("/lease"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJyZF9wcm9mZXNzaW9uYWxfYXBpIiwiZXhwIjoxNTY0NzU2MzY4fQ."
                                + "UnRfwq_yGo6tVWEoBldCkD1zFoiMSqqm1rTHqq4f_PuTEHIJj2IHeARw3wOnJG2c3MpjM71ZTFa"
                                + "0RNE4D2AUgA")));

        sidamService.stubFor(WireMock.post(urlPathMatching("/oauth2/authorize"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{"
                                + " \"code\": \"ef4fac86-d3e8-47b6-88a7-c7477fb69d3f\""
                                + "}")));

        sidamService.stubFor(WireMock.post(urlPathMatching("/o/token"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{"
                                + "  \"access_token\": \"ef4fac86-d3e8-47b6-88a7-c7477fb69d3f\""
                                + "}")));


    }

    public void searchUserProfileSyncWireMock(HttpStatus status) {

        String body = null;
        int returnHttpStaus = status.value();
        if (status.is2xxSuccessful()) {
            body = "[{"
                    + "  \"id\": \"ef4fac86-d3e8-47b6-88a7-c7477fb69d3f\","
                    + "  \"forename\": \"Super\","
                    + "  \"surname\": \"User\","
                    + "  \"email\": \"super.user@hmcts.net\","
                    + "  \"active\": \"true\","
                    + "  \"roles\": ["
                    + "  \"pui-case-manager\""
                    + "  ]"
                    + "}]";
            returnHttpStaus = 200;
        } else if (status.is4xxClientError()) {
            returnHttpStaus = 400;
        }

        sidamService.stubFor(get(urlPathMatching("/api/v1/users"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withHeader("X-Total-Count", "1")
                        .withBody(body)
                        .withStatus(returnHttpStaus)));

    }

    @BeforeEach
    public void userProfileGetUserWireMock() {

        userProfileService.stubFor(WireMock.get(
                        urlEqualTo("/v1/userprofile?userId=ef4fac86-d3e8-47b6-88a7-c7477fb69d3f"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody("{"
                                + "  \"userIdentifier\":\"ef4fac86-d3e8-47b6-88a7-c7477fb69d3f\","
                                + "  \"firstName\": \"prashanth\","
                                + "  \"lastName\": \"rao\","
                                + "  \"email\": \"super.user@hmcts.net\","
                                + "  \"idamStatus\": \"" + IdamStatus.ACTIVE + "\""
                                + "}")));
    }


    @AfterEach
    public void cleanupTestData() {
        profileSyncAuditDetailsRepository.deleteAll();
        profileSyncAuditRepository.deleteAll();
    }

    public void userProfileCreateUserWireMock(HttpStatus status) {
        String body = null;
        int returnHttpStaus = status.value();
        if (status.is2xxSuccessful()) {
            body = "{"
                    + "  \"idamId\":\"ef4fac86-d3e8-47b6-88a7-c7477fb69d3f\","
                    + "  \"idamRegistrationResponse\":\"201\""
                    + "}";
            returnHttpStaus = 201;
        } else if (status.is4xxClientError()) {
            body = "{"
                    + "  \"errorMessage\": \"400\","
                    + "  \"errorDescription\": \"BAD REQUEST\","
                    + "  \"timeStamp\": \"23:10\""
                    + "}";
            returnHttpStaus = 400;
        }

        userProfileService.stubFor(
                WireMock.put(urlPathMatching("/v1/userprofile/ef4fac86-d3e8-47b6-88a7-c7477fb69d3f"))
                        .willReturn(
                                aResponse()
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(body)
                                        .withStatus(returnHttpStaus)
                        )
        );
    }
}

