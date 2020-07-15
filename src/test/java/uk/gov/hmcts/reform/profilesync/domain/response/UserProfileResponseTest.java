package uk.gov.hmcts.reform.profilesync.domain.response;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.profilesync.helper.MockDataProvider.getUserProfile;

import org.junit.Test;
import uk.gov.hmcts.reform.profilesync.domain.UserProfile;

public class UserProfileResponseTest {

    private UserProfile user = getUserProfile();
    private UserProfileResponse userProfileResponse = new UserProfileResponse(user);

    @Test
    public void test_GetIdamId() {
        UserProfileResponse userProfRes = new UserProfileResponse();
        assertThat(userProfRes).isNotNull();
        assertThat(userProfileResponse.getIdamId()).isEqualTo(user.getUserIdentifier());
    }

    @Test
    public void test_GetIdamRegistrationResponse() {
        assertThat(userProfileResponse.getIdamRegistrationResponse()).isEqualTo(user.getIdamRegistrationResponse());
    }

}