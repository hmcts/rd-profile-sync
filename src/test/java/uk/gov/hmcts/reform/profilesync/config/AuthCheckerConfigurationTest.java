package uk.gov.hmcts.reform.profilesync.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import javax.servlet.http.HttpServletRequest;

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

    @Test
    public void testAuthorizedServicesExtractor() {
        Function<HttpServletRequest, Collection<String>> function = authCheckerConfiguration.authorizedServicesExtractor();

        assertThat(function).isNotNull();
    }

    @Test
    public void testAuthorizedRolesExtractor() {
        Function<HttpServletRequest, Collection<String>> function = authCheckerConfiguration.authorizedRolesExtractor();

        assertThat(function).isNotNull();
    }

    @Test
    public void testUserIdExtractor() {
        Function<HttpServletRequest, Optional<String>> function = authCheckerConfiguration.userIdExtractor();

        assertThat(function).isNotNull();
    }
}
