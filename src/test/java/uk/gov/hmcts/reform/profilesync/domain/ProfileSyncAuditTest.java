package uk.gov.hmcts.reform.profilesync.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProfileSyncAuditTest {

    private final String status = "success";

    @Test
    void shouldPopulateFewFields() {
        ProfileSyncAudit profileSyncAuditsync = new ProfileSyncAudit(LocalDateTime.now(), status);

        assertThat(profileSyncAuditsync.getSchedulerStartTime()).isNotNull();
        assertThat(profileSyncAuditsync.getSchedulerStatus()).isEqualTo(status);
        assertThat(profileSyncAuditsync.getProfileSyncAuditDetails()).isEmpty();
    }

    @Test
    void shouldPopulateAllFields() {

        LocalDateTime localDateTime = LocalDateTime.now();
        ProfileSyncAudit profileSyncAuditsync = new ProfileSyncAudit();
        profileSyncAuditsync.setSchedulerStartTime(localDateTime);
        profileSyncAuditsync.setSchedulerStatus(status);
        profileSyncAuditsync.setSchedulerId(1L);
        profileSyncAuditsync.setSchedulerEndTime(localDateTime);
        ProfileSyncAuditDetailsId syncAuditDetailsId = new ProfileSyncAuditDetailsId(profileSyncAuditsync,
                "336f930c-8e73-442f-9749-3f24deedb869");
        ProfileSyncAuditDetails profileSyncAuditDetail = new ProfileSyncAuditDetails();
        profileSyncAuditDetail.setCreated(LocalDateTime.now());
        profileSyncAuditDetail.setErrorDescription(status);
        profileSyncAuditDetail.setProfileSyncAuditDetailsId(syncAuditDetailsId);
        profileSyncAuditDetail.setStatusCode(200);
        List<ProfileSyncAuditDetails> profileSyncAuditDetails = new ArrayList<ProfileSyncAuditDetails>();
        profileSyncAuditDetails.add(profileSyncAuditDetail);
        profileSyncAuditsync.setProfileSyncAuditDetails(profileSyncAuditDetails);

        assertThat(profileSyncAuditsync.getSchedulerStartTime()).isNotNull();
        assertThat(profileSyncAuditsync.getSchedulerStartTime()).isEqualTo(localDateTime);
        assertThat(profileSyncAuditsync.getSchedulerStatus()).isEqualTo(status);
        assertThat(profileSyncAuditsync.getSchedulerId()).isEqualTo(1);
        assertThat(profileSyncAuditsync.getSchedulerEndTime()).isNotNull();
        assertThat(profileSyncAuditsync.getProfileSyncAuditDetails().size()).isEqualTo(1);
        assertThat(profileSyncAuditsync.getProfileSyncAuditDetails()).isEqualTo(profileSyncAuditDetails);
    }
}
