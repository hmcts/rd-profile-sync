package uk.gov.hmcts.reform.profilesync.schedular;

import java.time.Duration;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.profilesync.advice.UserProfileSyncException;
import uk.gov.hmcts.reform.profilesync.domain.ProfileSyncAudit;
import uk.gov.hmcts.reform.profilesync.domain.SyncJobConfig;
import uk.gov.hmcts.reform.profilesync.repository.ProfileSyncAuditRepository;
import uk.gov.hmcts.reform.profilesync.repository.ProfileSyncConfigRepository;
import uk.gov.hmcts.reform.profilesync.service.ProfileSyncService;

@Component
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileSyncJobScheduler {

    @Autowired
    protected ProfileSyncService profileSyncService;

    @Autowired
    protected ProfileSyncConfigRepository profileSyncConfigRepository;

    @Autowired
    protected ProfileSyncAuditRepository profileSyncAuditRepository;

    @Value("${scheduler.hours:}")
    protected String executeSearchQueryFrom;

    private static final String SUCCESS = "success";

    @Value("${loggingComponentName}")
    protected String loggingComponentName;


    @Scheduled(cron = "${scheduler.config}")
    public void updateIdamDataWithUserProfile() {

        String searchQuery = "(roles:pui-case-manager OR roles:pui-user-manager OR roles:pui-organisation-manager OR roles:pui-finance-manager) AND lastModified:>now-";
        LocalDateTime startTime = LocalDateTime.now();
        SyncJobConfig syncJobConfig =  profileSyncConfigRepository.findByConfigName("firstsearchquery");

        String configRun =  syncJobConfig.getConfigRun().trim();
        ProfileSyncAudit  syncAudit = new ProfileSyncAudit();
        log.info("{}:: Job needs to be run From Last::hours::{}" + configRun, loggingComponentName);

        if (!executeSearchQueryFrom.equals(configRun)) {

            searchQuery = searchQuery + configRun;

            log.info("{}:: searchQuery:: will execute from::DB job run value::{}" + searchQuery, loggingComponentName);

        } else if (null != profileSyncAuditRepository.findFirstBySchedulerStatusOrderBySchedulerEndTimeDesc(SUCCESS)) {

            ProfileSyncAudit syncAuditDtl  = profileSyncAuditRepository.findFirstBySchedulerStatusOrderBySchedulerEndTimeDesc(SUCCESS);
            searchQuery = searchQuery + getLastSuccessTimeInHours(syncAuditDtl.getSchedulerEndTime());

            log.info("{}::  SearchQuery::executing from last success ::{}" + searchQuery, loggingComponentName);
        }

        try {
            syncAudit = profileSyncService.updateUserProfileFeed(searchQuery, syncAudit);
            if (StringUtils.isEmpty(syncAudit.getSchedulerStatus())) {
                syncAudit.setSchedulerStatus(SUCCESS);
            }
            syncAudit.setSchedulerStartTime(startTime);
            //updating same sync object with status and start time and if user profiles associated
            // then it will save along with profileSyncAudit details.
            profileSyncAuditRepository.save(syncAudit);

            // setting the value to run next job for from
            if (!executeSearchQueryFrom.equals(configRun)) {
                syncJobConfig.setConfigRun(executeSearchQueryFrom);
                profileSyncConfigRepository.save(syncJobConfig);
            }
            log.info("{}::Sync batch job executed successfully::{}", loggingComponentName);

        } catch (UserProfileSyncException e) {
            log.error("{}::Sync Batch Job Failed::{}", loggingComponentName, e.getErrorMessage());
            syncAudit.setSchedulerStatus("fail");
            syncAudit.setSchedulerStartTime(startTime);
            profileSyncAuditRepository.save(syncAudit);

        }
    }

    public String getLastSuccessTimeInHours(LocalDateTime lastSuccessBatch) {

        long hoursDiff = 1;
        Duration duration = Duration.between(LocalDateTime.now(), lastSuccessBatch);
        long minutesDiff = Math.abs(duration.toMinutes());
        if (minutesDiff > 60) {
            hoursDiff = minutesDiff / 60;
            if (minutesDiff % 60 > 0) {

                hoursDiff = hoursDiff + 1;
            }

            log.info("{}:: Diff of Hours::{}" + hoursDiff, loggingComponentName);
        }
        log.info("{}::Since Last  success in sync job in hours::{}" + hoursDiff, loggingComponentName);
        return Long.toString(hoursDiff) + 'h';
    }

}