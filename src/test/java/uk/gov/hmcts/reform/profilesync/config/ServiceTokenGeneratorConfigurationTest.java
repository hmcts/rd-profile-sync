package uk.gov.hmcts.reform.profilesync.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.mockito.Mockito;

import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

public class ServiceTokenGeneratorConfigurationTest {

    private ServiceTokenGeneratorConfiguration sut = new ServiceTokenGeneratorConfiguration();

    @Test
    public void test_ServiceAuthTokenGenerator() {
        final String secret = "A6A6PRLRFWQLKP6";
        final String microService = "rd_professional_api";
        final ServiceAuthorisationApi serviceAuthorisationApiMock = Mockito.mock(ServiceAuthorisationApi.class);

        AuthTokenGenerator authTokenGenerator = sut.serviceAuthTokenGenerator(secret, microService,
                serviceAuthorisationApiMock);

        assertThat(authTokenGenerator).isNotNull();
    }


}
