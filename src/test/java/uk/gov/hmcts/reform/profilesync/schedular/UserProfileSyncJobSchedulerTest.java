package uk.gov.hmcts.reform.profilesync.schedular;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.profilesync.advice.UserProfileSyncException;
import uk.gov.hmcts.reform.profilesync.domain.ProfileSyncAudit;
import uk.gov.hmcts.reform.profilesync.domain.SyncJobConfig;
import uk.gov.hmcts.reform.profilesync.repository.ProfileSyncAuditRepository;
import uk.gov.hmcts.reform.profilesync.repository.SyncConfigRepository;
import uk.gov.hmcts.reform.profilesync.service.ProfileSyncService;

public class UserProfileSyncJobSchedulerTest {
    //mocked as its an interface
    private final ProfileSyncAuditRepository profileSyncAuditRepositoryMock = mock(ProfileSyncAuditRepository.class);
    private final ProfileSyncService profileSyncService = mock(ProfileSyncService.class); //mocked as its an interface
    private final SyncConfigRepository syncConfigRepositoryMock = mock(SyncConfigRepository.class); //mocked as its an interface
    private UserProfileSyncJobScheduler userProfileSyncJobScheduler = new UserProfileSyncJobScheduler(profileSyncService,
            syncConfigRepositoryMock, profileSyncAuditRepositoryMock, "1h");
    private SyncJobConfig syncJobConfig = new SyncJobConfig();
    private ProfileSyncAudit profileSyncAudit = new ProfileSyncAudit();
    private String firstSearchQuery;
    private String success;

    @Before
    public void setUp() {
        syncJobConfig.setConfigRun("2h");
        profileSyncAudit.setSchedulerEndTime(LocalDateTime.now().minusHours(1));
        firstSearchQuery = "firstsearchquery";
        success = "success";

    }

    @Test
    public void shouldSaveOnlyProfileSyncAuditWithStatus200() {

        when(syncConfigRepositoryMock.findByConfigName(firstSearchQuery)).thenReturn(syncJobConfig);
        when(profileSyncService.updateUserProfileFeed(any(), any())).thenReturn(profileSyncAudit);

        userProfileSyncJobScheduler.updateIdamDataWithUserProfile();

        verify(profileSyncAuditRepositoryMock, times(1)).save(any(ProfileSyncAudit.class));
        verify(syncConfigRepositoryMock, times(1)).save(any(SyncJobConfig.class));
        verify(syncConfigRepositoryMock, times(1)).findByConfigName(firstSearchQuery);
    }


    @Test
    public void shouldSaveProfileSyncAuditWithStatus200WhenConfigRunMatchesFromExecuteSearchQueryFrom() {
        syncJobConfig.setConfigRun("1h");
        when(syncConfigRepositoryMock.findByConfigName(firstSearchQuery)).thenReturn(syncJobConfig);
        when(profileSyncService.updateUserProfileFeed(any(), any())).thenReturn(profileSyncAudit);
        when(profileSyncAuditRepositoryMock.findFirstBySchedulerStatusOrderBySchedulerEndTimeDesc("success"))
                .thenReturn(profileSyncAudit);
        userProfileSyncJobScheduler.updateIdamDataWithUserProfile();

        verify(profileSyncAuditRepositoryMock, times(2))
                .findFirstBySchedulerStatusOrderBySchedulerEndTimeDesc("success");
        verify(profileSyncAuditRepositoryMock, times(1)).save(any(ProfileSyncAudit.class));
        verify(syncConfigRepositoryMock, times(0)).save(any(SyncJobConfig.class));
        verify(syncConfigRepositoryMock, times(1)).findByConfigName(firstSearchQuery);
    }


    @Test
    public void shouldSaveProfileSyncAuditWithFailStatusWhenThrowsException() {
        when(syncConfigRepositoryMock.findByConfigName(firstSearchQuery)).thenReturn(syncJobConfig);
        doThrow(UserProfileSyncException.class).when(profileSyncService).updateUserProfileFeed(any(String.class), any());

        userProfileSyncJobScheduler.updateIdamDataWithUserProfile();

        verify(profileSyncService, times(1)).updateUserProfileFeed(any(),any());
        verify(profileSyncAuditRepositoryMock, times(1)).save(any(ProfileSyncAudit.class));
        verify(syncConfigRepositoryMock, times(1)).findByConfigName(firstSearchQuery);
    }

    @Test
    public void objectUserProfileSyncSchedular() {
        UserProfileSyncJobScheduler userProfileSyncJobScheduler = new UserProfileSyncJobScheduler();
        assertThat(userProfileSyncJobScheduler).isNotNull();
    }
}
