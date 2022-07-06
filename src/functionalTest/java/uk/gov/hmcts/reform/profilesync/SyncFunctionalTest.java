package uk.gov.hmcts.reform.profilesync;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.profilesync.domain.SyncJobConfig;
import uk.gov.hmcts.reform.profilesync.repository.SyncConfigRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class SyncFunctionalTest {
    @Autowired
    SyncConfigRepository syncConfigRepository;

    @Test
    void shouldReturnConfigRun() {
        SyncJobConfig syncJobConfig = syncConfigRepository.findByConfigName("firstsearchquery");
        assertEquals("72h", syncJobConfig.getConfigRun());

    }
}
