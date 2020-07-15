package uk.gov.hmcts.reform.profilesync.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.profilesync.domain.ProfileSyncAudit;

@DataJpaTest
@RunWith(SpringRunner.class)
public class ProfileSyncAuditRepositoryTest {

    @Autowired
    ProfileSyncAuditRepository profileSyncAuditRepository;

    private String status = "status";
    private ProfileSyncAudit syncJobAudit = new ProfileSyncAudit(LocalDateTime.now(), status);

    @Before
    public void setUp() {
        profileSyncAuditRepository.save(syncJobAudit);
    }

    @Test
    public void findByStatus() {
        ProfileSyncAudit profileSyncAudits = profileSyncAuditRepository.findFirstBySchedulerStatusOrderBySchedulerEndTimeDesc(status);
        assertThat(profileSyncAudits).isNotNull();
        assertThat(profileSyncAudits.getSchedulerStatus()).isEqualTo(status);
    }

    @Test
    public void findFirstBySchedulerStatusOrderBySchedulerEndTimeDesc() {
        ProfileSyncAudit profileSyncAudits = profileSyncAuditRepository.findFirstBySchedulerStatusOrderBySchedulerEndTimeDesc(status);
        assertThat(profileSyncAudits.getSchedulerStatus()).isEqualTo(syncJobAudit.getSchedulerStatus());
        assertThat(profileSyncAudits.getSchedulerEndTime()).isNotNull();
        assertThat(profileSyncAudits.getSchedulerStartTime()).isEqualTo(syncJobAudit.getSchedulerStartTime());
        assertThat(profileSyncAudits.getSchedulerId()).isNotNull();
    }
}
