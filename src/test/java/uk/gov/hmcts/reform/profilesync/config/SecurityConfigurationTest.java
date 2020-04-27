package uk.gov.hmcts.reform.profilesync.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.List;

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
        List<String> paths = securityConfiguration.getAnonymousPaths();
        assertThat(paths.size()).isEqualTo(0);
    }

    @Test(expected = Test.None.class)
    public void configureTest() {
        securityConfiguration.configure(mock(WebSecurity.class));
    }
}
