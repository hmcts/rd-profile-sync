package uk.gov.hmcts.reform.profilesync.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.profilesync.domain.SyncJobConfig;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ExtendWith(SpringExtension.class)
class SyncConfigRepositoryTest {

    @Autowired
    SyncConfigRepository syncConfigRepository;

    private final String configName = "configName";
    private final String configRun = "configRun";
    private final SyncJobConfig syncJobConfig = new SyncJobConfig(configName, configRun);

    @BeforeEach
    public void setUp() {
        syncConfigRepository.save(syncJobConfig);
    }

    @Test
    void test_findByConfigName() {
        SyncJobConfig syncJobConfigFromRepository = syncConfigRepository.findByConfigName(configName);
        assertThat(syncJobConfigFromRepository.getConfigName()).isEqualTo(syncJobConfig.getConfigName());
        assertThat(syncJobConfigFromRepository.getConfigRun()).isEqualTo(syncJobConfig.getConfigRun());
        assertThat(syncJobConfigFromRepository.getId()).isEqualTo(syncJobConfig.getId());
    }
}
