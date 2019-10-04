package uk.gov.hmcts.reform.profilesync.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.Test;
import uk.gov.hmcts.reform.profilesync.helper.MockDataProvider;

public class UserProfileTest {

    private UserProfile sut = MockDataProvider.getUserProfile();

    @Test
    public void testGetIdamId() {
        assertThat(sut.getIdamId()).isEqualTo(MockDataProvider.idamId);
    }

    @Test
    public void testGetEmail() {
        assertThat(sut.getEmail()).isEqualTo(MockDataProvider.email);
    }

    @Test
    public void testGetFirstName() {
        assertThat(sut.getFirstName()).isEqualTo(MockDataProvider.firstName);
    }

    @Test
    public void testGetLastName() {
        assertThat(sut.getLastName()).isEqualTo(MockDataProvider.lastName);
    }

    @Test
    public void testGetStatus() {
        assertThat(sut.getIdamStatus()).isEqualTo(MockDataProvider.status);
    }

    @Test
    public void testGetIdamRegistrationResponse() {
        assertThat(sut.getIdamRegistrationResponse()).isEqualTo(MockDataProvider.idamRegistrationResponse);
    }

    @Test
    public void testSetGetValues() {

        UserProfile profile = UserProfile.builder().idamId(UUID.randomUUID().toString())
                .email("email@org.com")
                .firstName("firstName")
                .lastName("lastName")
                .idamStatus(IdamStatus.ACTIVE.name()).build();
        assertThat(profile.getIdamStatus()).isEqualTo(IdamStatus.ACTIVE.name());
    }


}