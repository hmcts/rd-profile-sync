package uk.gov.hmcts.reform.profilesync.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.hmcts.reform.profilesync.domain.SyncJobConfig;

public interface ProfileSyncConfigRepository extends JpaRepository<SyncJobConfig, Long> {

    SyncJobConfig findByConfigName(String configName);

}
