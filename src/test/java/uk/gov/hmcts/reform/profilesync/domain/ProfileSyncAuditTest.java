package uk.gov.hmcts.reform.profilesync.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.Test;

public class ProfileSyncAuditTest {

    private String status = "success";

    @Test
    public void shouldPopulateFewFields() {
        ProfileSyncAudit profileSyncAuditsync = new ProfileSyncAudit(LocalDateTime.now(), status);

        assertThat(profileSyncAuditsync.getSchedulerStartTime()).isNotNull();
        assertThat(profileSyncAuditsync.getSchedulerStatus()).isEqualTo(status);
    }

    @Test
    public void shouldPopulateAllFields() {
        LocalDateTime localDateTime = LocalDateTime.now();
        ProfileSyncAudit profileSyncAuditsync = new ProfileSyncAudit();
        profileSyncAuditsync.setSchedulerStartTime(localDateTime);
        profileSyncAuditsync.setSchedulerStatus(status);
        profileSyncAuditsync.setSchedulerId(1L);
        profileSyncAuditsync.setSchedulerEndTime(localDateTime);
        assertThat(profileSyncAuditsync.getSchedulerStartTime()).isNotNull();
        assertThat(profileSyncAuditsync.getSchedulerStartTime()).isEqualTo(localDateTime);
        assertThat(profileSyncAuditsync.getSchedulerStatus()).isEqualTo(status);
        assertThat(profileSyncAuditsync.getSchedulerId()).isEqualTo(1);
        assertThat(profileSyncAuditsync.getSchedulerEndTime()).isNotNull();
        assertThat(profileSyncAuditsync.getProfileSyncAuditDetails()).isEmpty();
    }
}
