package uk.gov.hmcts.reform.profilesync.config;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.profilesync.helper.MockDataProvider.authorization;
import static uk.gov.hmcts.reform.profilesync.helper.MockDataProvider.clientAuthorization;
import static uk.gov.hmcts.reform.profilesync.helper.MockDataProvider.clientId;
import static uk.gov.hmcts.reform.profilesync.helper.MockDataProvider.clientSecret;
import static uk.gov.hmcts.reform.profilesync.helper.MockDataProvider.redirectUri;

import org.junit.Test;

public class TokenConfigPropertiesTest {

    private TokenConfigProperties sut = new TokenConfigProperties();

    @Test
    public void testGetClientId() {
        sut.setClientId(clientId);
        assertThat(sut.getClientId()).isEqualTo(clientId);
    }

    @Test
    public void testGetClientSecret() {
        sut.setClientSecret(clientSecret);
        assertThat(sut.getClientSecret()).isEqualTo(clientSecret);
    }

    @Test
    public void testGetRedirectUri() {
        sut.setRedirectUri(redirectUri);
        assertThat(sut.getRedirectUri()).isEqualTo(redirectUri);
    }

    @Test
    public void testGetAuthorization() {
        sut.setAuthorization(authorization);
        assertThat(sut.getAuthorization()).isEqualTo(authorization);
    }

    @Test
    public void testGetClientAuthorization() {
        sut.setClientAuthorization(clientAuthorization);
        assertThat(sut.getClientAuthorization()).isEqualTo(clientAuthorization);
    }

    @Test
    public void testGetUrl() {
        sut.setUrl("www.url.com");
        assertThat(sut.getUrl()).isEqualTo("www.url.com");
    }
}