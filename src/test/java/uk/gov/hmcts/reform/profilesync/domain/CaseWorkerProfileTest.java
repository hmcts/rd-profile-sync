package uk.gov.hmcts.reform.profilesync.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.reform.profilesync.helper.MockDataProvider.getCaseWorkerProfile;
import static uk.gov.hmcts.reform.profilesync.helper.MockDataProvider.EMAIL;
import static uk.gov.hmcts.reform.profilesync.helper.MockDataProvider.FIRST_NAME;
import static uk.gov.hmcts.reform.profilesync.helper.MockDataProvider.IDAM_ID;
import static uk.gov.hmcts.reform.profilesync.helper.MockDataProvider.LAST_NAME;

class CaseWorkerProfileTest {

    private final CaseWorkerProfile cwp = getCaseWorkerProfile();

    @Test
    void getIdamId() {
        assertThat(cwp.isIdamStatus()).isTrue();
    }

    @Test
    void getEmail() {
        assertThat(cwp.getEmail()).isEqualTo(EMAIL);
    }

    @Test
    void getFirstName() {
        assertThat(cwp.getFirstName()).isEqualTo(FIRST_NAME);
    }

    @Test
    void getLastName() {
        assertThat(cwp.getLastName()).isEqualTo(LAST_NAME);
    }


    @Test
    void getCaseWorkerId() {
        assertThat(cwp.getUserId()).isEqualTo(IDAM_ID);
    }

    @Test
    void builderToString() {
        String profile = CaseWorkerProfile.builder().toString();

        assertThat(profile).isNotEmpty();
    }

    @Test
    void setGetValues() {
        CaseWorkerProfile profile = CaseWorkerProfile.builder().userId(UUID.randomUUID().toString())
                .email("email@org.com")
                .firstName("firstName")
                .lastName("lastName")
                .idamStatus(true)
                .build();

        assertTrue(profile.isIdamStatus());
    }
}
