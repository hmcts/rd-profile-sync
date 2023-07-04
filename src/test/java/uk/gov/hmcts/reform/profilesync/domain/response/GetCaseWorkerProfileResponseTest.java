package uk.gov.hmcts.reform.profilesync.domain.response;


import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.profilesync.domain.CaseWorkerProfile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.profilesync.helper.MockDataProvider.getCaseWorkerUserProfile;

class GetCaseWorkerProfileResponseTest {

    private CaseWorkerProfile caseWorkerProfile;
    private GetCaseWorkerProfileResponse sut;

    @Before
    public void setUp() {
        caseWorkerProfile = getCaseWorkerUserProfile();
        sut = new GetCaseWorkerProfileResponse(caseWorkerProfile);
    }

    @Test
    public void testGetIdamId() {
        assertThat(sut.getUserId()).isEqualTo(caseWorkerProfile.getUserId());
    }

    @Test
    public void testGetIdamStatus() {
        assertTrue(sut.isIdamStatus());
    }

    @Test
    public void testGetEmail() {
        assertThat(sut.getEmail()).isEqualTo(caseWorkerProfile.getEmail());
    }

    @Test
    public void testGetFirstName() {
        assertThat(sut.getFirstName()).isEqualTo(caseWorkerProfile.getFirstName());
    }

    @Test
    public void testGetLastName() {
        assertThat(sut.getLastName()).isEqualTo(caseWorkerProfile.getLastName());
    }
}
