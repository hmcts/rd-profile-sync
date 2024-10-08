package uk.gov.hmcts.reform.profilesync.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.hmcts.reform.profilesync.domain.ProfileSyncAudit;
import uk.gov.hmcts.reform.profilesync.domain.ProfileSyncAuditDetails;
import uk.gov.hmcts.reform.profilesync.domain.ProfileSyncAuditDetailsId;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@TestPropertySource(properties = {"spring.config.location=classpath:application.yaml"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProfileSyncAuditDetailsRepositoryTest {

    @Autowired
    ProfileSyncAuditDetailsRepository profileSyncAuditDetailsRepository;

    @Autowired
    ProfileSyncAuditRepository profileSyncAuditRepository;

    @BeforeEach
    void setUp() {
        String status = "success";
        ProfileSyncAudit syncJobAudit = new ProfileSyncAudit(LocalDateTime.now(), status);
        String userId = "336f930c-8e73-442f-9749-3f24deedb869";
        ProfileSyncAuditDetailsId syncAuditDetailsId = new ProfileSyncAuditDetailsId(syncJobAudit, userId);
        ProfileSyncAuditDetails profileSyncAuditDetails = new ProfileSyncAuditDetails(syncAuditDetailsId, 200,
                status, LocalDateTime.now());
        profileSyncAuditRepository.save(syncJobAudit);
        profileSyncAuditDetailsRepository.save(profileSyncAuditDetails);

    }

    @Test
    void findAllProfileSyncAuditDetails() {
        List<ProfileSyncAuditDetails> profileSyncAuditDetails = profileSyncAuditDetailsRepository.findAll();
        assertThat(profileSyncAuditDetails).isNotNull()
                                            .hasSize(1);
        profileSyncAuditDetails.forEach(profileSyncAuditDetail -> {
            assertThat(profileSyncAuditDetail.getCreated()).isNotNull();
            assertThat(profileSyncAuditDetail.getErrorDescription()).isNotNull();
            assertThat(profileSyncAuditDetail.getErrorDescription()).isEqualTo("success");
            assertThat(profileSyncAuditDetail.getStatusCode()).isNotZero();
            assertThat(profileSyncAuditDetail.getStatusCode()).isEqualTo(200);

        });
    }
}
