package uk.gov.hmcts.reform.profilesync.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.time.LocalDateTime;
import org.junit.Test;

public class ProfileSyncAuditDetailsIdTest {

    private String status = "success";
    private String userId = "336f930c-8e73-442f-9749-3f24deedb869";

    @Test
    public void shouldPopulateAllFields() {
        ProfileSyncAudit syncJobAudit = new ProfileSyncAudit(LocalDateTime.now(), status);
        ProfileSyncAuditDetailsId syncAuditDetailsIdOne = new ProfileSyncAuditDetailsId(syncJobAudit, userId);
        ProfileSyncAuditDetailsId syncAuditDetailsId = new ProfileSyncAuditDetailsId(syncJobAudit, userId);
        assertThat(syncAuditDetailsId.getProfileSyncAudit()).isNotNull();
        assertThat(syncAuditDetailsId.getProfileSyncAudit()).isEqualTo(syncJobAudit);
        assertThat(syncAuditDetailsId.getUserIdentifier()).isEqualTo(userId);
        assertThat(syncAuditDetailsId).isEqualTo(syncAuditDetailsIdOne);
    }

    @Test
    public void shouldCreateDefaultConstructor() {

        ProfileSyncAuditDetailsId syncAuditDetailsId = new ProfileSyncAuditDetailsId();
        assertThat(syncAuditDetailsId).isNotNull();
    }

    @Test
    public void shouldReturnHashCode() {
        ProfileSyncAudit syncJobAudit = new ProfileSyncAudit(LocalDateTime.now(), status);
        ProfileSyncAuditDetailsId syncAuditDetailsId = new ProfileSyncAuditDetailsId(syncJobAudit, userId);
        int userIdValue = syncAuditDetailsId.hashCode();
        assertThat(userIdValue).isNotEqualTo(0);
    }

    @Test
    public void shouldReturnEqual() {
        ProfileSyncAudit syncJobAudit = new ProfileSyncAudit(LocalDateTime.now(), status);
        ProfileSyncAuditDetailsId syncAuditDetailsId = new ProfileSyncAuditDetailsId(syncJobAudit, userId);
        ProfileSyncAuditDetailsId syncAuditDetailsIdOne = new ProfileSyncAuditDetailsId(syncJobAudit, userId);
        assertEquals(syncAuditDetailsIdOne, syncAuditDetailsId);
        assertThat(syncAuditDetailsId.hashCode()).isEqualTo(syncAuditDetailsIdOne.hashCode());

    }

    @Test
    public void shouldReturnNotEqual() {
        ProfileSyncAudit syncJobAudit = new ProfileSyncAudit(LocalDateTime.now(), status);
        ProfileSyncAuditDetailsId syncAuditDetailsId = new ProfileSyncAuditDetailsId(syncJobAudit, userId);
        ProfileSyncAuditDetailsId syncAuditDetailsIdOne = new ProfileSyncAuditDetailsId(syncJobAudit, "436f930c-8e73-442f-9749-3f24deedb869");
        assertNotEquals(syncAuditDetailsIdOne, syncAuditDetailsId);
        assertThat(syncAuditDetailsId.hashCode()).isNotEqualTo(syncAuditDetailsIdOne.hashCode());

    }
}
