package uk.gov.hmcts.reform.profilesync;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.profilesync.config.TokenConfigProperties;
import uk.gov.hmcts.reform.profilesync.domain.Source;
import uk.gov.hmcts.reform.profilesync.domain.SyncJobAudit;
import uk.gov.hmcts.reform.profilesync.repository.SyncJobRepository;
import uk.gov.hmcts.reform.profilesync.util.UserProfileSyncJobScheduler;

@Slf4j
public class RunProfileSyncJobTest extends AuthorizationEnabledIntegrationTest {

    @Autowired
    UserProfileSyncJobScheduler profileSyncJobScheduler;
    @Autowired
    TokenConfigProperties tokenConfigProperties;
    @Autowired
    SyncJobRepository syncJobRepository;
    final String dummyAuthorization = "c2hyZWVkaGFyLmxvbXRlQGhtY3RzLm5ldDpITUNUUzEyMzQ=";
    final String dummyClientAuthAuth = "cmQteHl6LWFwaTp4eXo=";
    final String dummyUrl = "http://127.0.0.1:5000";

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
        SyncJobAudit syncJobAudit = new SyncJobAudit(500, "fail", Source.SYNC);
        syncJobRepository.save(syncJobAudit);
        SyncJobAudit syncJobAudit1 = syncJobRepository.findFirstByStatusOrderByAuditTsDesc("fail");
        assertThat(syncJobAudit).isNotNull();
        assertThat(syncJobAudit.getStatus()).isEqualTo("fail");

        SyncJobAudit syncJobAudit2 = new SyncJobAudit(201, "success", Source.SYNC);
        syncJobRepository.save(syncJobAudit2);
        SyncJobAudit syncJobAudit3 = syncJobRepository.findFirstByStatusOrderByAuditTsDesc("success");
        assertThat(syncJobAudit3).isNotNull();
        assertThat(syncJobAudit3.getStatus()).isEqualTo("success");
        profileSyncJobScheduler.updateIdamDataWithUserProfile();
        List<SyncJobAudit>  syncJobAudits = syncJobRepository.findByStatus("success");
        assertThat(syncJobAudits.size()).isEqualTo(2);
    }

}
