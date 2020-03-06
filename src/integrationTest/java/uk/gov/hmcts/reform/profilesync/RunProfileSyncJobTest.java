package uk.gov.hmcts.reform.profilesync;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.profilesync.config.TokenConfigProperties;
import uk.gov.hmcts.reform.profilesync.constants.Source;
import uk.gov.hmcts.reform.profilesync.domain.SyncJobAudit;
import uk.gov.hmcts.reform.profilesync.domain.SyncJobConfig;
import uk.gov.hmcts.reform.profilesync.repository.SyncConfigRepository;
import uk.gov.hmcts.reform.profilesync.repository.SyncJobRepository;
import uk.gov.hmcts.reform.profilesync.schedular.UserProfileSyncJobScheduler;

@Slf4j
public class RunProfileSyncJobTest extends AuthorizationEnabledIntegrationTest {

    @Autowired
    private UserProfileSyncJobScheduler profileSyncJobScheduler;
    @Autowired
    private TokenConfigProperties tokenConfigProperties;
    @Autowired

    private SyncJobRepository syncJobRepository;

    @Autowired
    private SyncConfigRepository syncConfigRepository;

    private final String dummyAuthorization = "c2hyZWVkaGFyLmxvbXRlQGhtY3RzLm5ldDpITUNUUzEyMzQ=";
    private final String dummyClientAuthAuth = "cmQteHl6LWFwaTp4eXo=";
    private final String dummyUrl = "http://127.0.0.1:5000";

    @SuppressWarnings("unchecked")
    @Test
    public void persists_and_update_user_details_and_status_with_idam_details() {
        tokenConfigProperties.setAuthorization(dummyAuthorization);
        tokenConfigProperties.setClientAuthorization(dummyClientAuthAuth);
        tokenConfigProperties.setUrl(dummyUrl);
        profileSyncJobScheduler.updateIdamDataWithUserProfile();
        SyncJobAudit syncJobAudit = syncJobRepository.findFirstByStatusOrderByAuditTsDesc("success");
        assertThat(syncJobRepository.findAll()).isNotEmpty();
        assertThat(syncJobAudit).isNotNull();
        assertThat(syncJobAudit.getStatus()).isEqualTo("success");
    }

    @Test
    public void persists_and_update_user_details_and_status_failed_with_idam_details() {

        tokenConfigProperties.setAuthorization(dummyAuthorization);
        tokenConfigProperties.setClientAuthorization(dummyClientAuthAuth);
        tokenConfigProperties.setUrl(dummyUrl);
        SyncJobAudit syncJobAudit2 = new SyncJobAudit(201, "success", Source.SYNC);
        syncJobRepository.save(syncJobAudit2);
        SyncJobAudit syncJobAudit3 = syncJobRepository.findFirstByStatusOrderByAuditTsDesc("success");
        assertThat(syncJobAudit3).isNotNull();
        assertThat(syncJobAudit3.getStatus()).isEqualTo("success");
        profileSyncJobScheduler.updateIdamDataWithUserProfile();
        List<SyncJobAudit>  syncJobAudits = syncJobRepository.findByStatus("success");
        assertThat(syncJobAudits.size()).isGreaterThan(1);
    }

    @Test
    public void persists_and_return_config_name_details_and_config_run() {

        tokenConfigProperties.setAuthorization(dummyAuthorization);
        tokenConfigProperties.setClientAuthorization(dummyClientAuthAuth);
        tokenConfigProperties.setUrl(dummyUrl);

        SyncJobConfig syncJobConfig = syncConfigRepository.findByConfigName("firstsearchquery");

        assertThat(syncJobConfig).isNotNull();
        assertThat(syncJobConfig.getConfigName()).isEqualTo("firstsearchquery");
        assertThat(syncJobConfig.getConfigRun()).isNotNull();

    }

}
