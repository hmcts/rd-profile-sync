package uk.gov.hmcts.reform.profilesync.config;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.profilesync.helper.MockDataProvider.AUTHORIZATION;
import static uk.gov.hmcts.reform.profilesync.helper.MockDataProvider.CLIENT_AUTHORIZATION;
import static uk.gov.hmcts.reform.profilesync.helper.MockDataProvider.CLIENT_ID;
import static uk.gov.hmcts.reform.profilesync.helper.MockDataProvider.CLIENT_SECRET;
import static uk.gov.hmcts.reform.profilesync.helper.MockDataProvider.REDIRECT_URI;

import org.junit.Test;

public class TokenConfigPropertiesTest {

    private TokenConfigProperties sut = new TokenConfigProperties();

    @Test
    public void test_GetClientId() {
        sut.setClientId(CLIENT_ID);
        assertThat(sut.getClientId()).isEqualTo(CLIENT_ID);
    }

    @Test
    public void test_GetClientSecret() {
        sut.setClientSecret(CLIENT_SECRET);
        assertThat(sut.getClientSecret()).isEqualTo(CLIENT_SECRET);
    }

    @Test
    public void test_GetRedirectUri() {
        sut.setRedirectUri(REDIRECT_URI);
        assertThat(sut.getRedirectUri()).isEqualTo(REDIRECT_URI);
    }

    @Test
    public void test_GetAuthorization() {
        sut.setAuthorization(AUTHORIZATION);
        assertThat(sut.getAuthorization()).isEqualTo(AUTHORIZATION);
    }

    @Test
    public void test_GetClientAuthorization() {
        sut.setClientAuthorization(CLIENT_AUTHORIZATION);
        assertThat(sut.getClientAuthorization()).isEqualTo(CLIENT_AUTHORIZATION);
    }

    @Test
    public void test_GetUrl() {
        sut.setUrl("www.url.com");
        assertThat(sut.getUrl()).isEqualTo("www.url.com");
    }

}