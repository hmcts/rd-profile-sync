package uk.gov.hmcts.reform.profilesync;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.profilesync.config.TokenConfigProperties;
import uk.gov.hmcts.reform.profilesync.domain.ProfileSyncAudit;
import uk.gov.hmcts.reform.profilesync.domain.SyncJobConfig;
import uk.gov.hmcts.reform.profilesync.schedular.UserProfileSyncJobScheduler;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class RunProfileSyncJobIntTest extends AuthorizationEnabledIntTest {

    private final String dummyAuthorization = "c2hyZWVkaGFyLmxvbXRlQGhtY3RzLm5ldDpITUNUUzEyMzQ=";
    private final String dummyClientAuthAuth = "cmQteHl6LWFwaTp4eXo=";

    @Autowired
    protected UserProfileSyncJobScheduler jobScheduler;

    @Autowired
    private TokenConfigProperties tokenConfigProperties;

    @Test
    void whenSearchUserAndUpServiceCallSuccessAndSyncBatchStatusShouldBeSuccess() {

        searchUserProfileSyncWireMock(HttpStatus.OK);
        userProfileCreateUserWireMock(HttpStatus.CREATED);
        jobScheduler.updateIdamDataWithUserProfile();
        List<ProfileSyncAudit> syncAuditList = profileSyncAuditRepository.findAll();
        assertThat(syncAuditList).isNotEmpty();
        syncAuditList.forEach(syncAudit -> {
            assertThat(syncAudit.getSchedulerStatus()).isEqualTo("success");
            assertThat(syncAudit.getSchedulerEndTime()).isNotNull();
            assertThat(syncAudit.getSchedulerStartTime()).isNotNull();
            assertThat(syncAudit.getSchedulerId()).isPositive();
            assertThat(syncAudit.getProfileSyncAuditDetails()).isNotNull();
            syncAudit.getProfileSyncAuditDetails().forEach(profileSyncAuditDetails -> {
                assertThat(profileSyncAuditDetails.getStatusCode()).isEqualTo(201);
                assertThat(profileSyncAuditDetails.getErrorDescription()).isEqualTo("success");
            });
        });

        userProfileCreateUserWireMock(HttpStatus.BAD_REQUEST);
        profileSyncJobScheduler.updateIdamDataWithUserProfile();
        ProfileSyncAudit syncAuditSecondRes = profileSyncAuditRepository
                .findFirstBySchedulerStatusOrderBySchedulerEndTimeDesc("fail");
        assertThat(syncAuditSecondRes).isNotNull();
        assertThat(syncAuditSecondRes.getSchedulerStatus()).isEqualTo("fail");
        assertThat(syncAuditSecondRes.getSchedulerEndTime()).isNotNull();
        assertThat(syncAuditSecondRes.getSchedulerStartTime()).isNotNull();
        assertThat(syncAuditSecondRes.getSchedulerId()).isPositive();
        assertThat(syncAuditSecondRes.getProfileSyncAuditDetails()).isNotNull();
        syncAuditSecondRes.getProfileSyncAuditDetails().forEach(profileSyncAuditDetails -> {
            assertThat(profileSyncAuditDetails.getStatusCode()).isEqualTo(400);
            assertThat(profileSyncAuditDetails.getErrorDescription())
                    .isEqualTo("the user profile failed while updating the status");
        });

        userProfileCreateUserWireMock(HttpStatus.OK);
        profileSyncJobScheduler.updateIdamDataWithUserProfile();
        ProfileSyncAudit syncAuditThirdRes = profileSyncAuditRepository
                .findFirstBySchedulerStatusOrderBySchedulerEndTimeDesc("success");
        assertThat(syncAuditThirdRes).isNotNull();
        assertThat(syncAuditThirdRes.getSchedulerStatus()).isEqualTo("success");
        assertThat(syncAuditThirdRes.getSchedulerEndTime()).isNotNull();
        assertThat(syncAuditThirdRes.getSchedulerStartTime()).isNotNull();
        assertThat(syncAuditThirdRes.getSchedulerId()).isPositive();
        assertThat(syncAuditThirdRes.getProfileSyncAuditDetails()).isNotNull();
        syncAuditThirdRes.getProfileSyncAuditDetails().forEach(profileSyncAuditDetails -> {
            assertThat(profileSyncAuditDetails.getStatusCode()).isEqualTo(201);
            assertThat(profileSyncAuditDetails.getErrorDescription()).isEqualTo("success");
        });

    }

    @Test
    void whenSearchUserSucessAndUpServiceCallsFailScheduledIsCalledAtLeastOneTimes() {

        searchUserProfileSyncWireMock(HttpStatus.OK);
        userProfileCreateUserWireMock(HttpStatus.BAD_REQUEST);
        jobScheduler.updateIdamDataWithUserProfile();
        List<ProfileSyncAudit> syncAuditSecondRes = profileSyncAuditRepository.findAll();
        assertThat(syncAuditSecondRes).isNotEmpty();
        syncAuditSecondRes.forEach(syncAudit -> {
            assertThat(syncAudit.getSchedulerStatus()).isEqualTo("fail");
            assertThat(syncAudit.getSchedulerEndTime()).isNotNull();
            assertThat(syncAudit.getSchedulerStartTime()).isNotNull();
            assertThat(syncAudit.getSchedulerId()).isPositive();
            assertThat(syncAudit.getProfileSyncAuditDetails()).isNotNull();
            syncAudit.getProfileSyncAuditDetails().forEach(profileSyncAuditDetails -> {
                assertThat(profileSyncAuditDetails.getStatusCode()).isEqualTo(400);
                assertThat(profileSyncAuditDetails.getErrorDescription())
                        .isEqualTo("the user profile failed while updating the status");
            });
        });

    }

    @Test
    void whenSearchUserReturns400StatusCodeAndInsertFailStatusForSycnBatch() {

        searchUserProfileSyncWireMock(HttpStatus.BAD_REQUEST);
        profileSyncJobScheduler.updateIdamDataWithUserProfile();
        List<ProfileSyncAudit> syncAuditSecondRes = profileSyncAuditRepository.findAll();
        assertThat(syncAuditSecondRes).isNotEmpty();
        syncAuditSecondRes.forEach(syncAudit -> {
            assertThat(syncAudit.getSchedulerStatus()).isEqualTo("fail");
            assertThat(syncAudit.getSchedulerEndTime()).isNotNull();
            assertThat(syncAudit.getSchedulerStartTime()).isNotNull();
            assertThat(syncAudit.getSchedulerId()).isPositive();
            assertThat(syncAudit.getProfileSyncAuditDetails().size()).isZero();
        });

    }

    @Test
    void persists_and_update_user_details_and_status_success_with_idam_details() {

        tokenConfigProperties.setAuthorization(dummyAuthorization);
        tokenConfigProperties.setClientAuthorization(dummyClientAuthAuth);
        LocalDateTime dateTime = LocalDateTime.now();
        ProfileSyncAudit profileSyncAudit = new ProfileSyncAudit(dateTime, "success");
        profileSyncAuditRepository.save(profileSyncAudit);
        LocalDateTime dateTime1 = LocalDateTime.now();
        ProfileSyncAudit profileSyncAudit1 = new ProfileSyncAudit(dateTime1, "success");
        profileSyncAuditRepository.save(profileSyncAudit1);
        ProfileSyncAudit profileSyncAuditRes = profileSyncAuditRepository
                .findFirstBySchedulerStatusOrderBySchedulerEndTimeDesc("success");
        assertThat(profileSyncAuditRes).isNotNull();
        assertThat(profileSyncAuditRes.getSchedulerStatus()).isEqualTo("success");
        assertThat(profileSyncAuditRes.getSchedulerEndTime()).isNotNull();

        Duration duration = Duration.between(profileSyncAuditRes.getSchedulerEndTime(), dateTime1);
        assertThat(duration.toMinutes()).isZero();

        profileSyncJobScheduler.updateIdamDataWithUserProfile();
        List<ProfileSyncAudit> profileSyncAudits = profileSyncAuditRepository.findAll();
        assertThat(profileSyncAudits.size()).isGreaterThan(1);
    }

    @Test
    void persists_and_update_user_details_and_status_failed_with_idam_details() {

        searchUserProfileSyncWireMock(HttpStatus.OK);
        userProfileCreateUserWireMock(HttpStatus.CREATED);
        tokenConfigProperties.setAuthorization(dummyAuthorization);
        tokenConfigProperties.setClientAuthorization(dummyClientAuthAuth);

        LocalDateTime dateTime = LocalDateTime.now();
        ProfileSyncAudit profileSyncAudit = new ProfileSyncAudit(dateTime, "fail");
        profileSyncAuditRepository.save(profileSyncAudit);

        LocalDateTime dateTime1 = LocalDateTime.now();
        ProfileSyncAudit profileSyncAudit1 = new ProfileSyncAudit(dateTime1, "fail");
        profileSyncAuditRepository.save(profileSyncAudit1);

        ProfileSyncAudit profileSyncAuditRes = profileSyncAuditRepository
                .findFirstBySchedulerStatusOrderBySchedulerEndTimeDesc("fail");
        assertThat(profileSyncAuditRes).isNotNull();
        assertThat(profileSyncAuditRes.getSchedulerStatus()).isEqualTo("fail");
        assertThat(profileSyncAuditRes.getSchedulerEndTime()).isNotNull();
        Duration duration = Duration.between(profileSyncAuditRes.getSchedulerEndTime(), dateTime1);
        assertThat(duration.toMinutes()).isZero();

        jobScheduler.updateIdamDataWithUserProfile();
        List<ProfileSyncAudit> profileSyncAudits = profileSyncAuditRepository.findAll();
        assertThat(profileSyncAudits.size()).isGreaterThan(1);
    }

    @Test
    void persists_and_return_config_name_details_and_config_run() {

        tokenConfigProperties.setAuthorization(dummyAuthorization);
        tokenConfigProperties.setClientAuthorization(dummyClientAuthAuth);

        SyncJobConfig syncJobConfig = profileSyncConfigRepository.findByConfigName("firstsearchquery");

        assertThat(syncJobConfig).isNotNull();
        assertThat(syncJobConfig.getConfigName()).isEqualTo("firstsearchquery");
        assertThat(syncJobConfig.getConfigRun()).isNotNull();

    }

}
