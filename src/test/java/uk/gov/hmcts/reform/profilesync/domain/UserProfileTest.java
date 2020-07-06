package uk.gov.hmcts.reform.profilesync.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.profilesync.helper.MockDataProvider.EMAIL;
import static uk.gov.hmcts.reform.profilesync.helper.MockDataProvider.FIRST_NAME;
import static uk.gov.hmcts.reform.profilesync.helper.MockDataProvider.IDAM_ID;
import static uk.gov.hmcts.reform.profilesync.helper.MockDataProvider.IDAM_REGISTRATION_RESPONSE;
import static uk.gov.hmcts.reform.profilesync.helper.MockDataProvider.LAST_NAME;
import static uk.gov.hmcts.reform.profilesync.helper.MockDataProvider.STATUS;
import static uk.gov.hmcts.reform.profilesync.helper.MockDataProvider.getUserProfile;

import java.util.UUID;

import org.junit.Test;
import uk.gov.hmcts.reform.profilesync.constants.IdamStatus;

public class UserProfileTest {

    private UserProfile sut = getUserProfile();

    @Test
    public void test_GetIdamId() {
        assertThat(sut.getUserIdentifier()).isEqualTo(IDAM_ID);
    }

    @Test
    public void test_GetEmail() {
        assertThat(sut.getEmail()).isEqualTo(EMAIL);
    }

    @Test
    public void test_GetFirstName() {
        assertThat(sut.getFirstName()).isEqualTo(FIRST_NAME);
    }

    @Test
    public void test_GetLastName() {
        assertThat(sut.getLastName()).isEqualTo(LAST_NAME);
    }

    @Test
    public void test_GetStatus() {
        assertThat(sut.getIdamStatus()).isEqualTo(STATUS);
    }

    @Test
    public void test_GetIdamRegistrationResponse() {
        assertThat(sut.getIdamRegistrationResponse()).isEqualTo(IDAM_REGISTRATION_RESPONSE);
    }

    @Test
    public void test_SetGetValues() {
        UserProfile profile = UserProfile.builder().userIdentifier(UUID.randomUUID().toString())
                .email("email@org.com")
                .firstName("firstName")
                .lastName("lastName")
                .idamStatus(IdamStatus.ACTIVE.name())
                .idamRegistrationResponse(200)
                .build();

        assertThat(profile.getIdamStatus()).isEqualTo(IdamStatus.ACTIVE.name());
    }

    @Test
    public void test_BuilderToString() {
        String profile = UserProfile.builder().toString();

        assertThat(profile).isNotEmpty();
    }
}