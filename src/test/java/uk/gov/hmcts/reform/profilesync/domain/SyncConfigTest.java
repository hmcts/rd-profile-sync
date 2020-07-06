package uk.gov.hmcts.reform.profilesync.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class SyncConfigTest {


    @Test
    public void test_populate_few_fields() {

        SyncJobConfig syncJobConfig = new SyncJobConfig("firstsearchquery", "1h");

        assertThat(syncJobConfig.getConfigName()).isEqualTo("firstsearchquery");
        assertThat(syncJobConfig.getConfigRun()).isEqualTo("1h");

    }

    @Test
    public void test_populate_all_fields() {

        SyncJobConfig syncJobConfig = new SyncJobConfig();
        syncJobConfig.setId(1);
        syncJobConfig.setConfigRun("1h");
        syncJobConfig.setConfigName("firstsearchquery");

        assertThat(syncJobConfig.getId()).isEqualTo(1);
        assertThat(syncJobConfig.getConfigRun()).isEqualTo("1h");
        assertThat(syncJobConfig.getConfigName()).isEqualTo("firstsearchquery");
    }
}
