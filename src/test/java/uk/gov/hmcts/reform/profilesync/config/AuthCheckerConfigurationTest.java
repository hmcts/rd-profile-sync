package uk.gov.hmcts.reform.profilesync.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.Test;

public class AuthCheckerConfigurationTest {

    AuthCheckerConfiguration authCheckerConfiguration = new AuthCheckerConfiguration();

    @Test
    public void testGetAuthorisedServices() {
        List<String> retrievedServices = authCheckerConfiguration.getAuthorisedServices();

        assertThat(retrievedServices).isNotNull();
    }

    @Test
    public void testGetAuthorisedRoles() {
        List<String> retrievedRoles = authCheckerConfiguration.getAuthorisedRoles();

        assertThat(retrievedRoles).isNotNull();
    }
}
