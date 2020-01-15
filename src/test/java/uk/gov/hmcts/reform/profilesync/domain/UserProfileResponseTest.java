package uk.gov.hmcts.reform.profilesync.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import uk.gov.hmcts.reform.profilesync.helper.MockDataProvider;

public class UserProfileResponseTest {

    private UserProfile user = MockDataProvider.getUserProfile();
    private UserProfileResponse userProfileResponse = new UserProfileResponse(user);

    @Test
    public void testGetIdamId() {
        UserProfileResponse userProfRes = new UserProfileResponse();
        assertThat(userProfRes).isNotNull();
        assertThat(userProfileResponse.getIdamId()).isEqualTo(user.getUserIdentifier());
    }

    @Test
    public void testGetIdamRegistrationResponse() {
        assertThat(userProfileResponse.getIdamRegistrationResponse()).isEqualTo(user.getIdamRegistrationResponse());
    }

}