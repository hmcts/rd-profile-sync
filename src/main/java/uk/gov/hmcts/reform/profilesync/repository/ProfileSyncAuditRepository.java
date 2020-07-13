package uk.gov.hmcts.reform.profilesync.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.hmcts.reform.profilesync.domain.ProfileSyncAudit;

public interface ProfileSyncAuditRepository extends JpaRepository<ProfileSyncAudit, Long> {
    ProfileSyncAudit findFirstBySchedulerStatusOrderBySchedulerEndTimeDesc(String status);
}

