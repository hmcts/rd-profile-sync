package uk.gov.hmcts.reform.profilesync.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.springframework.security.config.annotation.web.builders.WebSecurity;

public class SecurityConfigurationTest {

    private SecurityConfiguration securityConfiguration;

    @Before
    public void setUp() {
        securityConfiguration = mock(SecurityConfiguration.class);
    }

    @Test
    public void getAnonymousPathsTest() {
        assertThat(securityConfiguration.getAnonymousPaths()).isEmpty();
    }

    @Test(expected = Test.None.class)
    public void configureTest() {
        securityConfiguration.configure(mock(WebSecurity.class));
    }
}
