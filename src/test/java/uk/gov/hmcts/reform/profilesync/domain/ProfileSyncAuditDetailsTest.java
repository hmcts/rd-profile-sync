package uk.gov.hmcts.reform.profilesync.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.Test;

public class ProfileSyncAuditDetailsTest {

    private final String status = "success";
    private final int statusCode = 200;
    private final String userId = "336f930c-8e73-442f-9749-3f24deedb869";
    ProfileSyncAudit syncJobAudit = new ProfileSyncAudit(LocalDateTime.now(), status);
    ProfileSyncAuditDetailsId syncAuditDetailsId = new ProfileSyncAuditDetailsId(syncJobAudit, userId);

    @Test
    public void shouldPopulateAllFields() {

        ProfileSyncAuditDetails profileSyncAuditDetails = new ProfileSyncAuditDetails(syncAuditDetailsId,statusCode,
                status,LocalDateTime.now());

        assertThat(profileSyncAuditDetails.getCreated()).isNotNull();
        assertThat(profileSyncAuditDetails.getErrorDescription()).isEqualTo(status);
        assertThat(profileSyncAuditDetails.getStatusCode()).isEqualTo(statusCode);
        assertThat(profileSyncAuditDetails.getProfileSyncAuditDetailsId()).isEqualTo(syncAuditDetailsId);
    }

    @Test
    public void shouldPopulateAllFieldsWithSetter() {

        ProfileSyncAuditDetails profileSyncAuditDetails = new ProfileSyncAuditDetails();
        profileSyncAuditDetails.setCreated(LocalDateTime.now());
        profileSyncAuditDetails.setErrorDescription(status);
        profileSyncAuditDetails.setProfileSyncAuditDetailsId(syncAuditDetailsId);
        profileSyncAuditDetails.setStatusCode(statusCode);

        assertThat(profileSyncAuditDetails).isNotNull();
        assertThat(profileSyncAuditDetails.getCreated()).isNotNull();
        assertThat(profileSyncAuditDetails.getErrorDescription()).isEqualTo(status);
        assertThat(profileSyncAuditDetails.getStatusCode()).isEqualTo(statusCode);
        assertThat(profileSyncAuditDetails.getProfileSyncAuditDetailsId()).isEqualTo(syncAuditDetailsId);
    }

}
