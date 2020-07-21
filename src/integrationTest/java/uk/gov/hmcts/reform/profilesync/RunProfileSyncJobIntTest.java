package uk.gov.hmcts.reform.profilesync;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.profilesync.config.TokenConfigProperties;
import uk.gov.hmcts.reform.profilesync.domain.ProfileSyncAudit;
import uk.gov.hmcts.reform.profilesync.domain.SyncJobConfig;
import uk.gov.hmcts.reform.profilesync.repository.ProfileSyncAuditDetailsRepository;
import uk.gov.hmcts.reform.profilesync.repository.ProfileSyncAuditRepository;
import uk.gov.hmcts.reform.profilesync.repository.ProfileSyncConfigRepository;
import uk.gov.hmcts.reform.profilesync.schedular.UserProfileSyncJobScheduler;

@Slf4j
@RunWith(SpringIntegrationSerenityRunner.class)
public class RunProfileSyncJobIntTest extends AuthorizationEnabledIntTest {

    @Autowired
    private UserProfileSyncJobScheduler profileSyncJobScheduler;
    @Autowired
    private TokenConfigProperties tokenConfigProperties;
    @Autowired
    private ProfileSyncAuditRepository profileSyncAuditRepository;
    @Autowired
    private ProfileSyncAuditDetailsRepository profileSyncAuditDetailsRepository;

    @Autowired
    private ProfileSyncConfigRepository profileSyncConfigRepository;

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
        ProfileSyncAudit profileSyncAudit = profileSyncAuditRepository.findFirstBySchedulerStatusOrderBySchedulerEndTimeDesc("success");
        assertThat(profileSyncAuditRepository.findAll()).isNotEmpty();
        assertThat(profileSyncAuditDetailsRepository.findAll()).isNotEmpty();
        assertThat(profileSyncAudit).isNotNull();
        assertThat(profileSyncAudit.getSchedulerStatus()).isEqualTo("success");
    }

    @Test
    public void persists_and_update_user_details_and_status_success_with_idam_details() {

        tokenConfigProperties.setAuthorization(dummyAuthorization);
        tokenConfigProperties.setClientAuthorization(dummyClientAuthAuth);
        tokenConfigProperties.setUrl(dummyUrl);
        LocalDateTime dateTime = LocalDateTime.now();
        ProfileSyncAudit profileSyncAudit = new ProfileSyncAudit(dateTime, "success");
        profileSyncAuditRepository.save(profileSyncAudit);
        LocalDateTime dateTime1 = LocalDateTime.now();
        ProfileSyncAudit profileSyncAudit1 = new ProfileSyncAudit(dateTime1, "success");
        profileSyncAuditRepository.save(profileSyncAudit1);
        ProfileSyncAudit profileSyncAuditRes = profileSyncAuditRepository.findFirstBySchedulerStatusOrderBySchedulerEndTimeDesc("success");
        assertThat(profileSyncAuditRes).isNotNull();
        assertThat(profileSyncAuditRes.getSchedulerStatus()).isEqualTo("success");
        assertThat(profileSyncAuditRes.getSchedulerEndTime()).isNotNull();

        Duration duration = Duration.between(profileSyncAuditRes.getSchedulerEndTime(), dateTime1);
        assertThat(duration.toMinutes()).isEqualTo(0);

        profileSyncJobScheduler.updateIdamDataWithUserProfile();
        List<ProfileSyncAudit>  profileSyncAudits = profileSyncAuditRepository.findAll();
        assertThat(profileSyncAudits.size()).isGreaterThan(1);
    }

    @Test
    public void persists_and_update_user_details_and_status_failed_with_idam_details() {

        tokenConfigProperties.setAuthorization(dummyAuthorization);
        tokenConfigProperties.setClientAuthorization(dummyClientAuthAuth);
        tokenConfigProperties.setUrl(dummyUrl);

        LocalDateTime dateTime = LocalDateTime.now();
        ProfileSyncAudit profileSyncAudit = new ProfileSyncAudit(dateTime, "fail");
        profileSyncAuditRepository.save(profileSyncAudit);

        LocalDateTime dateTime1 = LocalDateTime.now();
        ProfileSyncAudit profileSyncAudit1 = new ProfileSyncAudit(dateTime1, "fail");
        profileSyncAuditRepository.save(profileSyncAudit1);

        ProfileSyncAudit profileSyncAuditRes = profileSyncAuditRepository.findFirstBySchedulerStatusOrderBySchedulerEndTimeDesc("fail");
        assertThat(profileSyncAuditRes).isNotNull();
        assertThat(profileSyncAuditRes.getSchedulerStatus()).isEqualTo("fail");
        assertThat(profileSyncAuditRes.getSchedulerEndTime()).isNotNull();
        Duration duration = Duration.between(profileSyncAuditRes.getSchedulerEndTime(), dateTime1);
        assertThat(duration.toMinutes()).isEqualTo(0);

        profileSyncJobScheduler.updateIdamDataWithUserProfile();
        List<ProfileSyncAudit>  profileSyncAudits = profileSyncAuditRepository.findAll();
        assertThat(profileSyncAudits.size()).isGreaterThan(1);
    }

    @Test
    public void persists_and_return_config_name_details_and_config_run() {

        tokenConfigProperties.setAuthorization(dummyAuthorization);
        tokenConfigProperties.setClientAuthorization(dummyClientAuthAuth);
        tokenConfigProperties.setUrl(dummyUrl);

        SyncJobConfig syncJobConfig = profileSyncConfigRepository.findByConfigName("firstsearchquery");

        assertThat(syncJobConfig).isNotNull();
        assertThat(syncJobConfig.getConfigName()).isEqualTo("firstsearchquery");
        assertThat(syncJobConfig.getConfigRun()).isNotNull();

    }

}
