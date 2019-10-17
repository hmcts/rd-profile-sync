package uk.gov.hmcts.reform.profilesync.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.Test;

public class SyncJobTest {


    @Test
    public void should_populate_few_fields() {

        SyncJobAudit syncJobAudit = new SyncJobAudit(200, "success", Source.SYNC);

        assertThat(syncJobAudit.getSource().name()).isEqualTo(Source.SYNC.name());
        assertThat(syncJobAudit.getStatus()).isEqualTo("success");
        assertThat(syncJobAudit.getResponse()).isEqualTo(200);
    }

    @Test
    public void should_populate_all_fields() {

        SyncJobAudit syncJobAudit = new SyncJobAudit();
        syncJobAudit.setId(1L);
        syncJobAudit.setAuditTs(LocalDateTime.now());
        syncJobAudit.setRecordsUpdated(2);
        syncJobAudit.setErrorMessage("errorMsg");
        assertThat(syncJobAudit.getId()).isEqualTo(1);
        assertThat(syncJobAudit.getAuditTs()).isNotNull();
        assertThat(syncJobAudit.getErrorMessage()).isEqualTo("errorMsg");
        assertThat(syncJobAudit.getRecordsUpdated()).isEqualTo(2);
    }
}
