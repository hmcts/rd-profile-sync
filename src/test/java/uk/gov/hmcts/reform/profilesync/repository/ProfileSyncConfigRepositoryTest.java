package uk.gov.hmcts.reform.profilesync.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.profilesync.domain.SyncJobConfig;

@DataJpaTest
@RunWith(SpringRunner.class)
public class ProfileSyncConfigRepositoryTest {

    @Autowired
    ProfileSyncConfigRepository profileSyncConfigRepository;

    private String configName = "configName";
    private String configRun = "configRun";
    private SyncJobConfig syncJobConfig = new SyncJobConfig(configName, configRun);

    @Before
    public void setUp() {
        profileSyncConfigRepository.save(syncJobConfig);
    }

    @Test
    public void findByConfigName() {
        SyncJobConfig syncJobConfigFromRepository = profileSyncConfigRepository.findByConfigName(configName);
        assertThat(syncJobConfigFromRepository.getConfigName()).isEqualTo(syncJobConfig.getConfigName());
        assertThat(syncJobConfigFromRepository.getConfigRun()).isEqualTo(syncJobConfig.getConfigRun());
        assertThat(syncJobConfigFromRepository.getId()).isEqualTo(syncJobConfig.getId());
    }
}
