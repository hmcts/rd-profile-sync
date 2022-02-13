package uk.gov.hmcts.reform.profilesync.config;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceTokenGeneratorConfigurationTest {

    private final ServiceTokenGeneratorConfiguration sut = new ServiceTokenGeneratorConfiguration();

    @Test
    void testServiceAuthTokenGenerator() {
        final String secret = "A6A6PRLRFWQLKP6";
        final String microService = "rd_profile_sync";
        final ServiceAuthorisationApi serviceAuthorisationApiMock = Mockito.mock(ServiceAuthorisationApi.class);

        AuthTokenGenerator authTokenGenerator = sut.serviceAuthTokenGenerator(secret, microService,
                serviceAuthorisationApiMock);

        assertThat(authTokenGenerator).isNotNull();
    }


}
