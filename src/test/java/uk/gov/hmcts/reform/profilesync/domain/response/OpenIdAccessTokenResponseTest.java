package uk.gov.hmcts.reform.profilesync.domain.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class OpenIdAccessTokenResponseTest {

    @Test
    public void test_BearerTokenResponse() {
        OpenIdAccessTokenResponse openIdAccessTokenResponse = new OpenIdAccessTokenResponse("access_token");
        assertThat(openIdAccessTokenResponse).isNotNull();
        assertThat(openIdAccessTokenResponse.getAccessToken()).isEqualTo("access_token");
    }
}
