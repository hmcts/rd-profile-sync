package uk.gov.hmcts.reform.profilesync.domain.response;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.profilesync.helper.MockDataProvider.getUserProfile;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.profilesync.domain.UserProfile;

public class GetUserProfileResponseTest {

    private UserProfile userProfile;
    private GetUserProfileResponse sut;

    @Before
    public void setUp() {
        userProfile = getUserProfile();
        sut = new GetUserProfileResponse(userProfile);
    }

    @Test
    public void test_GetIdamId() {
        assertThat(sut.getUserIdentifier()).isEqualTo(userProfile.getUserIdentifier());
    }

    @Test
    public void test_GetIdamStatus() {
        assertThat(sut.getIdamStatus()).isEqualTo(userProfile.getIdamStatus());
    }

    @Test
    public void test_GetEmail() {
        assertThat(sut.getEmail()).isEqualTo(userProfile.getEmail());
    }

    @Test
    public void test_GetFirstName() {
        assertThat(sut.getFirstName()).isEqualTo(userProfile.getFirstName());
    }

    @Test
    public void test_GetLastName() {
        assertThat(sut.getLastName()).isEqualTo(userProfile.getLastName());
    }
}