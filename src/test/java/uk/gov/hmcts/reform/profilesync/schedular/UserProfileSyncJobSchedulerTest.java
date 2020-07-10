package uk.gov.hmcts.reform.profilesync.schedular;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.profilesync.advice.UserProfileSyncException;
import uk.gov.hmcts.reform.profilesync.domain.SyncJobAudit;
import uk.gov.hmcts.reform.profilesync.domain.SyncJobConfig;
import uk.gov.hmcts.reform.profilesync.repository.SyncConfigRepository;
import uk.gov.hmcts.reform.profilesync.repository.SyncJobRepository;
import uk.gov.hmcts.reform.profilesync.service.ProfileSyncService;

public class UserProfileSyncJobSchedulerTest {

    private final SyncJobRepository syncJobRepository = mock(SyncJobRepository.class); //mocked as its an interface
    private final ProfileSyncService profileSyncService = mock(ProfileSyncService.class); //mocked as its an interface
    private final SyncConfigRepository syncConfigRepositoryMock = mock(SyncConfigRepository.class); //mocked as its an interface
    private UserProfileSyncJobScheduler userProfileSyncJobScheduler = new UserProfileSyncJobScheduler(profileSyncService, syncJobRepository, syncConfigRepositoryMock, "1h", "RD_Profile_Sync");

    private SyncJobAudit syncJobAudit = new SyncJobAudit();
    private SyncJobConfig syncJobConfig = new SyncJobConfig();
    private String firstSearchQuery;
    private String success;

    @Before
    public void setUp() {
        syncJobConfig.setConfigRun("2h");
        syncJobAudit.setAuditTs(LocalDateTime.now().minusHours(1));
        firstSearchQuery = "firstsearchquery";
        success = "success";
    }

    @Test
    public void test_updateIdamDataWithUserProfileWithDbValue() {

        when(syncConfigRepositoryMock.findByConfigName(firstSearchQuery)).thenReturn(syncJobConfig);
        doNothing().when(profileSyncService).updateUserProfileFeed(any(String.class));

        userProfileSyncJobScheduler.updateIdamDataWithUserProfile();

        verify(syncJobRepository, times(1)).save(any(SyncJobAudit.class));
        verify(syncConfigRepositoryMock, times(1)).save(any(SyncJobConfig.class));
        verify(syncConfigRepositoryMock, times(1)).findByConfigName(firstSearchQuery);
    }


    @Test
    public void test_updateIdamDataWithUserProfile() {
        when(syncConfigRepositoryMock.findByConfigName(firstSearchQuery)).thenReturn(syncJobConfig);
        doNothing().when(profileSyncService).updateUserProfileFeed(any(String.class));

        userProfileSyncJobScheduler.updateIdamDataWithUserProfile();

        verify(syncJobRepository, times(1)).save(any(SyncJobAudit.class));
        verify(syncConfigRepositoryMock, times(1)).save(any(SyncJobConfig.class));
        verify(syncConfigRepositoryMock, times(1)).findByConfigName(firstSearchQuery);
    }

    @Test
    public void test_updateIdamDataWithUserProfileThrowsException() {
        when(syncConfigRepositoryMock.findByConfigName(firstSearchQuery)).thenReturn(syncJobConfig);
        when(syncJobRepository.findFirstByStatusOrderByAuditTsDesc(success)).thenReturn(syncJobAudit);
        doThrow(UserProfileSyncException.class).when(profileSyncService).updateUserProfileFeed(any(String.class));

        userProfileSyncJobScheduler.updateIdamDataWithUserProfile();

        verify(syncJobRepository, times(1)).save(any(SyncJobAudit.class));
        verify(syncConfigRepositoryMock, times(1)).findByConfigName(firstSearchQuery);
    }

    @Test
    public void test_objectUserProfileSyncSchedular() {
        UserProfileSyncJobScheduler userProfileSyncJobScheduler = new UserProfileSyncJobScheduler();
        assertThat(userProfileSyncJobScheduler).isNotNull();
    }
}
