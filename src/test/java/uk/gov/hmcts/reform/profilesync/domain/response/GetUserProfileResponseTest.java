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
    public void testGetIdamId() {
        assertThat(sut.getUserIdentifier()).isEqualTo(userProfile.getUserIdentifier());
    }

    @Test
    public void testGetIdamStatus() {
        assertThat(sut.getIdamStatus()).isEqualTo(userProfile.getIdamStatus());
    }

    @Test
    public void testGetEmail() {
        assertThat(sut.getEmail()).isEqualTo(userProfile.getEmail());
    }

    @Test
    public void testGetFirstName() {
        assertThat(sut.getFirstName()).isEqualTo(userProfile.getFirstName());
    }

    @Test
    public void testGetLastName() {
        assertThat(sut.getLastName()).isEqualTo(userProfile.getLastName());
    }
}