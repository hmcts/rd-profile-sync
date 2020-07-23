package uk.gov.hmcts.reform.profilesync.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.hmcts.reform.profilesync.domain.ProfileSyncAuditDetails;
import uk.gov.hmcts.reform.profilesync.domain.ProfileSyncAuditDetailsId;

public interface ProfileSyncAuditDetailsRepository extends JpaRepository<ProfileSyncAuditDetails,
        ProfileSyncAuditDetailsId> {
}
