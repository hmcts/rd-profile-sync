package uk.gov.hmcts.reform.profilesync.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SyncJobConfigTest {

    private final String firstSearchQuery = "firstsearchquery";
    private final String configRun = "1h";

    @Test
    void test_populate_few_fields() {
        SyncJobConfig syncJobConfig = new SyncJobConfig(firstSearchQuery, configRun);

        assertThat(syncJobConfig.getConfigName()).isEqualTo(firstSearchQuery);
        assertThat(syncJobConfig.getConfigRun()).isEqualTo(configRun);
    }

    @Test
    void test_populate_all_fields() {
        SyncJobConfig syncJobConfig = new SyncJobConfig();
        syncJobConfig.setId(1);
        syncJobConfig.setConfigRun(configRun);
        syncJobConfig.setConfigName(firstSearchQuery);

        assertThat(syncJobConfig.getId()).isEqualTo(1);
        assertThat(syncJobConfig.getConfigRun()).isEqualTo(configRun);
        assertThat(syncJobConfig.getConfigName()).isEqualTo(firstSearchQuery);
    }
}
