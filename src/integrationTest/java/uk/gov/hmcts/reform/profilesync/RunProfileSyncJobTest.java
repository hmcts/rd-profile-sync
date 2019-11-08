package uk.gov.hmcts.reform.profilesync;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import uk.gov.hmcts.reform.profilesync.domain.Source;
import uk.gov.hmcts.reform.profilesync.domain.SyncJobAudit;
import uk.gov.hmcts.reform.profilesync.domain.SyncJobConfig;

@Slf4j
public class RunProfileSyncJobTest extends AuthorizationEnabledIntegrationTest {



    @SuppressWarnings("unchecked")
    @Test
    public void persists_and_update_user_details_and_status_with_idam_details() {

        profileSyncJobScheduler.updateIdamDataWithUserProfile();
        SyncJobAudit syncJobAudit = syncJobRepository.findFirstByStatusOrderByAuditTsDesc("success");
        assertThat(syncJobRepository.findAll()).isNotEmpty();
        assertThat(syncJobAudit).isNotNull();
        assertThat(syncJobAudit.getStatus()).isEqualTo("success");

    }

    @Test
    public void persists_and_update_user_details_and_status_failed_with_idam_details() {


        SyncJobAudit syncJobAudit2 = new SyncJobAudit(201, "success", Source.SYNC);
        syncJobRepository.save(syncJobAudit2);
        SyncJobAudit syncJobAudit3 = syncJobRepository.findFirstByStatusOrderByAuditTsDesc("success");
        assertThat(syncJobAudit3).isNotNull();
        assertThat(syncJobAudit3.getStatus()).isEqualTo("success");
        profileSyncJobScheduler.updateIdamDataWithUserProfile();
        List<SyncJobAudit>  syncJobAudits = syncJobRepository.findByStatus("success");
        assertThat(syncJobAudits.size()).isEqualTo(2);

    }

    @Test
    public void persists_and_return_config_name_details_and_config_run() {

        SyncJobConfig syncJobConfig = syncConfigRepository.findByConfigName("firstsearchquery");
        assertThat(syncJobConfig).isNotNull();
        assertThat(syncJobConfig.getConfigName()).isEqualTo("firstsearchquery");
        assertThat(syncJobConfig.getConfigRun()).isNotNull();

    }

}
