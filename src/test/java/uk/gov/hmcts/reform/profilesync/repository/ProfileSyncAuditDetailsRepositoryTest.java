package uk.gov.hmcts.reform.profilesync.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.profilesync.domain.ProfileSyncAudit;
import uk.gov.hmcts.reform.profilesync.domain.ProfileSyncAuditDetails;
import uk.gov.hmcts.reform.profilesync.domain.ProfileSyncAuditDetailsId;

@DataJpaTest
@RunWith(SpringRunner.class)
public class ProfileSyncAuditDetailsRepositoryTest {

    @Autowired
    ProfileSyncAuditDetailsRepository profileSyncAuditDetailsRepository;

    @Autowired
    ProfileSyncAuditRepository profileSyncAuditRepository;

    private String status = "success";
    private String userId = "336f930c-8e73-442f-9749-3f24deedb869";

    @Before
    public void setUp() {
        ProfileSyncAudit syncJobAudit = new ProfileSyncAudit(LocalDateTime.now(), status);
        ProfileSyncAuditDetailsId syncAuditDetailsId = new ProfileSyncAuditDetailsId(syncJobAudit, userId);
        ProfileSyncAuditDetails profileSyncAuditDetails = new ProfileSyncAuditDetails(syncAuditDetailsId, 200,
                status, LocalDateTime.now());
        profileSyncAuditRepository.save(syncJobAudit);
        profileSyncAuditDetailsRepository.save(profileSyncAuditDetails);

    }

    @Test
    public void findAllProfileSyncAuditDetails() {
        List<ProfileSyncAuditDetails> profileSyncAuditDetails = profileSyncAuditDetailsRepository.findAll();
        assertThat(profileSyncAuditDetails).isNotNull();
        assertThat(profileSyncAuditDetails.size()).isEqualTo(1);
        profileSyncAuditDetails.forEach(profileSyncAuditDetail -> {
            assertThat(profileSyncAuditDetail.getCreated()).isNotNull();
            assertThat(profileSyncAuditDetail.getErrorDescription()).isNotNull();
            assertThat(profileSyncAuditDetail.getErrorDescription()).isEqualTo("success");
            assertThat(profileSyncAuditDetail.getStatusCode()).isNotZero();
            assertThat(profileSyncAuditDetail.getStatusCode()).isEqualTo(200);

        });
    }
}
