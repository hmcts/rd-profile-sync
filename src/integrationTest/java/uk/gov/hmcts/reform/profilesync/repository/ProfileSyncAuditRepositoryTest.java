package uk.gov.hmcts.reform.profilesync.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.hmcts.reform.profilesync.domain.ProfileSyncAudit;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@TestPropertySource(properties = {"spring.config.location=classpath:application.yaml"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProfileSyncAuditRepositoryTest {

    @Autowired
    ProfileSyncAuditRepository profileSyncAuditRepository;

    private final String status = "status";
    private final ProfileSyncAudit syncJobAudit = new ProfileSyncAudit(LocalDateTime.now(), status);

    @BeforeEach
    public void setUp() {
        profileSyncAuditRepository.save(syncJobAudit);
    }

    @Test
    void findByStatus() {
        ProfileSyncAudit profileSyncAudits = profileSyncAuditRepository
                .findFirstBySchedulerStatusOrderBySchedulerEndTimeDesc(status);
        assertThat(profileSyncAudits).isNotNull();
        assertThat(profileSyncAudits.getSchedulerStatus()).isEqualTo(status);
    }

    @Test
    void findFirstBySchedulerStatusOrderBySchedulerEndTimeDesc() {
        ProfileSyncAudit profileSyncAudits = profileSyncAuditRepository
                .findFirstBySchedulerStatusOrderBySchedulerEndTimeDesc(status);
        assertThat(profileSyncAudits.getSchedulerStatus()).isEqualTo(syncJobAudit.getSchedulerStatus());
        assertThat(profileSyncAudits.getSchedulerEndTime()).isNotNull();
        assertThat(profileSyncAudits.getSchedulerStartTime()).isEqualTo(syncJobAudit.getSchedulerStartTime());
        assertThat(profileSyncAudits.getSchedulerId()).isNotNull();
    }
}
