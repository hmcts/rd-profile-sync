package uk.gov.hmcts.reform.profilesync.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;

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
}
