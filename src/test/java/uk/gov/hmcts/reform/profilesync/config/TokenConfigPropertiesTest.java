package uk.gov.hmcts.reform.profilesync.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import uk.gov.hmcts.reform.profilesync.helper.MockDataProvider;

public class TokenConfigPropertiesTest {

    private TokenConfigProperties sut = new TokenConfigProperties();

    @Test
    public void testGetClientId() {
        sut.setClientId(MockDataProvider.clientId);
        assertThat(sut.getClientId()).isEqualTo(MockDataProvider.clientId);
    }

    @Test
    public void testGetClientSecret() {
        sut.setClientSecret(MockDataProvider.clientSecret);
        assertThat(sut.getClientSecret()).isEqualTo(MockDataProvider.clientSecret);
    }

    @Test
    public void testGetRedirectUri() {
        sut.setRedirectUri(MockDataProvider.redirectUri);
        assertThat(sut.getRedirectUri()).isEqualTo(MockDataProvider.redirectUri);
    }

    @Test
    public void testGetAuthorization() {
        sut.setAuthorization(MockDataProvider.authorization);
        assertThat(sut.getAuthorization()).isEqualTo(MockDataProvider.authorization);
    }

    @Test
    public void testGetClientAuthorization() {
        sut.setClientAuthorization(MockDataProvider.clientAuthorization);
        assertThat(sut.getClientAuthorization()).isEqualTo(MockDataProvider.clientAuthorization);
    }

    @Test
    public void testGetUrl() {
        sut.setUrl("www.url.com");
        assertThat(sut.getUrl()).isEqualTo("www.url.com");
    }
}