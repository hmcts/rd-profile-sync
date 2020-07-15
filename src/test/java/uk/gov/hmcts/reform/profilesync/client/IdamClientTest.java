package uk.gov.hmcts.reform.profilesync.client;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class IdamClientTest {

    @Test
    public void test_BearerTokenResponse() {
        IdamClient.BearerTokenResponse bearerTokenResponse = new IdamClient.BearerTokenResponse("");
        bearerTokenResponse.setAccessToken("access_token");

        assertThat(bearerTokenResponse.getAccessToken()).isEqualTo("access_token");
    }
}