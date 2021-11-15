package uk.gov.hmcts.reform.profilesync.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ProfileSyncAuditDetailsIdTest {

    private final String status = "success";
    private final String userId = "336f930c-8e73-442f-9749-3f24deedb869";

    @Test
    void shouldPopulateAllFields() {
        ProfileSyncAudit syncJobAudit = new ProfileSyncAudit(LocalDateTime.now(), status);
        ProfileSyncAuditDetailsId syncAuditDetailsIdOne = new ProfileSyncAuditDetailsId(syncJobAudit, userId);
        ProfileSyncAuditDetailsId syncAuditDetailsId = new ProfileSyncAuditDetailsId(syncJobAudit, userId);
        assertThat(syncAuditDetailsId.getProfileSyncAudit()).isNotNull();
        assertThat(syncAuditDetailsId.getProfileSyncAudit()).isEqualTo(syncJobAudit);
        assertThat(syncAuditDetailsId.getUserIdentifier()).isEqualTo(userId);
        assertThat(syncAuditDetailsId).isEqualTo(syncAuditDetailsIdOne);
    }

    @Test
    void shouldCreateDefaultConstructor() {

        ProfileSyncAuditDetailsId syncAuditDetailsId = new ProfileSyncAuditDetailsId();
        assertThat(syncAuditDetailsId).isNotNull();
    }

    @Test
    void shouldReturnHashCode() {
        ProfileSyncAudit syncJobAudit = new ProfileSyncAudit(LocalDateTime.now(), status);
        ProfileSyncAuditDetailsId syncAuditDetailsId = new ProfileSyncAuditDetailsId(syncJobAudit, userId);
        int userIdValue = syncAuditDetailsId.hashCode();
        assertThat(userIdValue).isNotZero();
    }

    @Test
    void shouldReturnEqual() {
        ProfileSyncAudit syncJobAudit = new ProfileSyncAudit(LocalDateTime.now(), status);
        ProfileSyncAuditDetailsId syncAuditDetailsId = new ProfileSyncAuditDetailsId(syncJobAudit, userId);
        ProfileSyncAuditDetailsId syncAuditDetailsIdOne = new ProfileSyncAuditDetailsId(syncJobAudit, userId);
        assertEquals(syncAuditDetailsIdOne, syncAuditDetailsId);
        assertThat(syncAuditDetailsId.hashCode()).hasSameHashCodeAs(syncAuditDetailsIdOne.hashCode());

    }

    @Test
    void shouldReturnNotEqual() {
        ProfileSyncAudit syncJobAudit = new ProfileSyncAudit(LocalDateTime.now(), status);
        ProfileSyncAuditDetailsId syncAuditDetailsId = new ProfileSyncAuditDetailsId(syncJobAudit, userId);
        ProfileSyncAuditDetailsId syncAuditDetailsIdOne = new ProfileSyncAuditDetailsId(syncJobAudit,
                "436f930c-8e73-442f-9749-3f24deedb869");
        assertNotEquals(syncAuditDetailsIdOne, syncAuditDetailsId);
        assertThat(syncAuditDetailsId.hashCode()).isNotEqualTo(syncAuditDetailsIdOne.hashCode());

    }
}
