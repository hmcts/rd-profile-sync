package uk.gov.hmcts.reform.profilesync.util;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.profilesync.domain.Source;
import uk.gov.hmcts.reform.profilesync.domain.SyncJobAudit;
import uk.gov.hmcts.reform.profilesync.domain.UserProfileSyncException;
import uk.gov.hmcts.reform.profilesync.repository.SyncJobRepository;
import uk.gov.hmcts.reform.profilesync.service.ProfileSyncService;

@Component
@Slf4j
public class UserProfileSyncJobScheduler {

    @Autowired
    protected ProfileSyncService profileSyncService;

    @Autowired
    protected SyncJobRepository syncJobRepository;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd hh:mm:ss");


    @Scheduled(cron = "${scheduler.config}")
    public void updateIdamDataWithUserProfile() {

        log.info("The time is now {}", dateFormat.format(new Date()));

        String searchQuery = "(roles:pui-case-manager OR roles:pui-user-manager OR roles:pui-organisation-manager OR roles:pui-finance-manager) AND lastModified:>now-1h";
        List<SyncJobAudit>  syncJobAudits = syncJobRepository.findAll();

        if (null != syncJobRepository.findFirstByStatusOrderByAuditTsDesc("fail")) {

            log.info("The last batch failed time{}", dateFormat.format(new Date()));
            SyncJobAudit auditjob = syncJobRepository.findFirstByStatusOrderByAuditTsDesc("success");
            searchQuery =  searchQuery.replace("1",getLastBatchFailureTimeInHours(auditjob.getAuditTs()));
            log.info("searchQuery::",searchQuery);
        }
        try {

            profileSyncService.updateUserProfileFeed(searchQuery);
            SyncJobAudit syncJobAudit = new SyncJobAudit(201, "success", Source.SYNC);
            syncJobRepository.save(syncJobAudit);

        } catch (UserProfileSyncException e) {
            log.error("Sync Batch Job Failed::",e);
            SyncJobAudit syncJobAudit = new SyncJobAudit(500, "fail", Source.SYNC);
            syncJobRepository.save(syncJobAudit);

        }
    }

    private String getLastBatchFailureTimeInHours(LocalDateTime lastSuccessBatch) {

        long hoursDiff = 1;
        Duration duration = Duration.between(LocalDateTime.now(), lastSuccessBatch);
        long minutesDiff = Math.abs(duration.toMinutes());
        if (minutesDiff > 60) {
            hoursDiff = minutesDiff / 60;
            if (minutesDiff % 60 > 0) {

                hoursDiff = hoursDiff + 1;
            }

            log.info("Diff of Hours::" + hoursDiff);
        }
        log.info("Since Last Batch failure in sync job in hours:: " + hoursDiff);
        return Long.valueOf(hoursDiff).toString();
    }

}





