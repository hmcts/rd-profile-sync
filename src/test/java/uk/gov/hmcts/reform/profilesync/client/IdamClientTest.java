package uk.gov.hmcts.reform.profilesync.client;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class IdamClientTest {

    @Test
    public void testAuthenticateUserResponse() {
        final String code = "my-code";
        IdamClient.AuthenticateUserResponse authenticateUserResponse = new IdamClient.AuthenticateUserResponse();
        authenticateUserResponse.setCode(code);

        assertThat(authenticateUserResponse.getCode()).isEqualTo(code);
    }

    @Test
    public void testBearerTokenResponse() {
        IdamClient.BearerTokenResponse bearerTokenResponse = new IdamClient.BearerTokenResponse("");
        bearerTokenResponse.setAccessToken("access_token");

        assertThat(bearerTokenResponse.getAccessToken()).isEqualTo("access_token");
    }
}