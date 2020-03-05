package uk.gov.hmcts.reform.profilesync.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.profilesync.helper.MockDataProvider.email;
import static uk.gov.hmcts.reform.profilesync.helper.MockDataProvider.firstName;
import static uk.gov.hmcts.reform.profilesync.helper.MockDataProvider.getUserProfile;
import static uk.gov.hmcts.reform.profilesync.helper.MockDataProvider.idamId;
import static uk.gov.hmcts.reform.profilesync.helper.MockDataProvider.idamRegistrationResponse;
import static uk.gov.hmcts.reform.profilesync.helper.MockDataProvider.lastName;
import static uk.gov.hmcts.reform.profilesync.helper.MockDataProvider.status;

import java.util.UUID;

import org.junit.Test;
import uk.gov.hmcts.reform.profilesync.constants.IdamStatus;

public class UserProfileTest {

    private UserProfile sut = getUserProfile();

    @Test
    public void testGetIdamId() {
        assertThat(sut.getUserIdentifier()).isEqualTo(idamId);
    }

    @Test
    public void testGetEmail() {
        assertThat(sut.getEmail()).isEqualTo(email);
    }

    @Test
    public void testGetFirstName() {
        assertThat(sut.getFirstName()).isEqualTo(firstName);
    }

    @Test
    public void testGetLastName() {
        assertThat(sut.getLastName()).isEqualTo(lastName);
    }

    @Test
    public void testGetStatus() {
        assertThat(sut.getIdamStatus()).isEqualTo(status);
    }

    @Test
    public void testGetIdamRegistrationResponse() {
        assertThat(sut.getIdamRegistrationResponse()).isEqualTo(idamRegistrationResponse);
    }

    @Test
    public void testSetGetValues() {
        UserProfile profile = UserProfile.builder().userIdentifier(UUID.randomUUID().toString())
                .email("email@org.com")
                .firstName("firstName")
                .lastName("lastName")
                .idamStatus(IdamStatus.ACTIVE.name())
                .idamRegistrationResponse(200)
                .build();

        assertThat(profile.getIdamStatus()).isEqualTo(IdamStatus.ACTIVE.name());
    }
}