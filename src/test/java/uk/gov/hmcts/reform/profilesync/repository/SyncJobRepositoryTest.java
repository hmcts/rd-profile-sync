package uk.gov.hmcts.reform.profilesync.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.profilesync.constants.Source;
import uk.gov.hmcts.reform.profilesync.domain.SyncJobAudit;

@DataJpaTest
@RunWith(SpringRunner.class)
public class SyncJobRepositoryTest {

    @Autowired
    SyncJobRepository syncJobRepository;

    private String status = "status";
    private SyncJobAudit syncJobAudit = new SyncJobAudit(1, status, Source.API);

    @Before
    public void setUp() {
        syncJobRepository.save(syncJobAudit);
    }

    @Test
    public void test_findByStatus() {
        List<SyncJobAudit> syncJobAudits = syncJobRepository.findByStatus(status);
        assertThat(syncJobAudits.size()).isEqualTo(1);
        assertThat(syncJobAudits.get(0)).isEqualTo(syncJobAudit);
    }

    @Test
    public void test_findFirstByStatusOrderByAuditTsDesc() {
        SyncJobAudit syncJobAuditFromRepository = syncJobRepository.findFirstByStatusOrderByAuditTsDesc(status);
        assertThat(syncJobAuditFromRepository.getResponse()).isEqualTo(syncJobAudit.getResponse());
        assertThat(syncJobAuditFromRepository.getStatus()).isEqualTo(syncJobAudit.getStatus());
        assertThat(syncJobAuditFromRepository.getSource()).isEqualTo(syncJobAudit.getSource());
    }
}
