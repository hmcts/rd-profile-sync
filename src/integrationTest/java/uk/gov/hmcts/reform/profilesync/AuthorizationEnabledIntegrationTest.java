package uk.gov.hmcts.reform.profilesync;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;

import uk.gov.hmcts.reform.profilesync.client.IdamClient;
import uk.gov.hmcts.reform.profilesync.client.UserProfileClient;
import uk.gov.hmcts.reform.profilesync.constants.IdamStatus;
import uk.gov.hmcts.reform.profilesync.repository.SyncConfigRepository;
import uk.gov.hmcts.reform.profilesync.repository.SyncJobRepository;
import uk.gov.hmcts.reform.profilesync.schedular.UserProfileSyncJobScheduler;

@Configuration
@TestPropertySource(properties = {"S2S_URL=http://127.0.0.1:8990","IDAM_URL:http://127.0.0.1:5000", "USER_PROFILE_URL:http://127.0.0.1:8091"})
public abstract class AuthorizationEnabledIntegrationTest  extends SpringBootIntegrationTest {

    @Autowired
    protected SyncJobRepository syncJobRepository;

    @Autowired
    protected UserProfileClient userProfileFeignClient;

    @Autowired
    protected IdamClient idamFeignClient;

    @Autowired
    protected UserProfileSyncJobScheduler profileSyncJobScheduler;

    @Autowired
    protected SyncConfigRepository syncConfigRepository;

    @ClassRule
    public static WireMockRule s2sService = new WireMockRule(8990);

    @ClassRule
    public static WireMockRule sidamService = new WireMockRule(5000);

    @ClassRule
    public static WireMockRule userProfileService = new WireMockRule(8091);


    @Before
    public void setupS2sAndIdamStubs() throws Exception {

        s2sService.stubFor(get(urlEqualTo("/details"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("rd_professional_api")));

        s2sService.stubFor(WireMock.post(urlEqualTo("/lease"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("eyJhbGciOiJIUzUxMiJ9."
                                .concat("eyJzdWIiOiJyZF9wcm9mZXNzaW9uYWxfYXBpIiwiZXhwIjoxNTY0NzU2MzY4fQ.")
                                .concat("UnRfwq_yGo6tVWEoBldCkD1zFoiMSqqm1rTHqq4f_")
                                .concat("PuTEHIJj2IHeARw3wOnJG2c3MpjM71ZTFa0RNE4D2AUgA"))));

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

        sidamService.stubFor(get(urlPathMatching("/api/v1/users"))
               .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{"
                                + "  \"id\": \"ef4fac86-d3e8-47b6-88a7-c7477fb69d3f\","
                                + "  \"forename\": \"Super\","
                                + "  \"surname\": \"User\","
                                + "  \"email\": \"super.user@hmcts.net\","
                                + "  \"active\": \"true\","
                                + "  \"roles\": ["
                                + "  \"pui-case-manager\""
                                + "  ]"
                                + "}]")));

    }

    @Before
    public void userProfileGetUserWireMock() {

        userProfileService.stubFor(WireMock
                .get(urlEqualTo("/v1/userprofile?userId=ef4fac86-d3e8-47b6-88a7-c7477fb69d3f"))
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

    @Before
    public void userProfileSyncWireMock() {

        userProfileService.stubFor(WireMock.put(urlPathMatching("/v1/userprofile/ef4fac86-d3e8-47b6-88a7-c7477fb69d3f"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)));

    }

    @After
    public void cleanupTestData() {

        syncJobRepository.deleteAll();
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
        }

        userProfileService.stubFor(
                WireMock.post(urlPathMatching("/v1/userprofile"))
                        .willReturn(
                                aResponse()
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(body)
                                        .withStatus(200)
                        )
        );
    }
}

