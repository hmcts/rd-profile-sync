package uk.gov.hmcts.reform.profilesync.schedular;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import org.junit.Test;
import uk.gov.hmcts.reform.profilesync.advice.UserProfileSyncException;
import uk.gov.hmcts.reform.profilesync.domain.ProfileSyncAudit;
import uk.gov.hmcts.reform.profilesync.domain.SyncJobConfig;
import uk.gov.hmcts.reform.profilesync.repository.ProfileSyncAuditRepository;
import uk.gov.hmcts.reform.profilesync.repository.ProfileSyncConfigRepository;
import uk.gov.hmcts.reform.profilesync.service.ProfileSyncService;

public class UserProfileSyncJobSchedulerTest {
    //mocked as its an interface
    private final ProfileSyncAuditRepository profileSyncAuditRepMock = mock(ProfileSyncAuditRepository.class);
    private final ProfileSyncService profileSyncService = mock(ProfileSyncService.class);
    private final ProfileSyncConfigRepository profileSyncConfigRepositoryMock = mock(ProfileSyncConfigRepository.class);

    private SyncJobConfig syncJobConfigMock = mock(SyncJobConfig.class);

    private ProfileSyncAudit profileSyncAudit = mock(ProfileSyncAudit.class);
    private UserProfileSyncJobScheduler userProfileSyncJobScheduler = new UserProfileSyncJobScheduler(profileSyncService,
            profileSyncConfigRepositoryMock, profileSyncAuditRepMock, "1h", "RD_Profile_Sync");
    private String firstSearchQuery = "firstsearchquery";
    private final String success = "success";




    @Test
    public void shouldSaveOnlyProfileSyncAuditWithStatus200() {

        when(profileSyncConfigRepositoryMock.findByConfigName(firstSearchQuery)).thenReturn(syncJobConfigMock);
        when(syncJobConfigMock.getConfigRun()).thenReturn("1h");
        when(profileSyncAudit.getSchedulerEndTime()).thenReturn(LocalDateTime.now().minusHours(1));
        when(profileSyncAuditRepMock.findFirstBySchedulerStatusOrderBySchedulerEndTimeDesc("success"))
                .thenReturn(profileSyncAudit);
        when(profileSyncService.updateUserProfileFeed(any(), any())).thenReturn(profileSyncAudit);

        userProfileSyncJobScheduler.updateIdamDataWithUserProfile();

        verify(profileSyncAuditRepMock, times(2))
                .findFirstBySchedulerStatusOrderBySchedulerEndTimeDesc("success");
        verify(profileSyncAuditRepMock, times(1)).save(any(ProfileSyncAudit.class));
        verify(profileSyncConfigRepositoryMock, times(1)).findByConfigName(firstSearchQuery);
        verify(profileSyncService, times(1)).updateUserProfileFeed(any(), any());
        verify(profileSyncAudit, times(1)).setSchedulerStatus(any(String.class));

    }

    @Test
    public void shouldSaveProfileSyncAuditWithStatus200WhenConfigRunMatchesFromExecuteSearchQueryFrom() {
        when(profileSyncConfigRepositoryMock.findByConfigName(firstSearchQuery)).thenReturn(syncJobConfigMock);
        when(syncJobConfigMock.getConfigRun()).thenReturn("2h");
        when(profileSyncAudit.getSchedulerEndTime()).thenReturn(LocalDateTime.now().minusHours(1));
        when(profileSyncService.updateUserProfileFeed(any(), any())).thenReturn(profileSyncAudit);
        when(profileSyncAuditRepMock.findFirstBySchedulerStatusOrderBySchedulerEndTimeDesc("success"))
                .thenReturn(profileSyncAudit);

        userProfileSyncJobScheduler.updateIdamDataWithUserProfile();

        verify(profileSyncAuditRepMock, times(0))
                .findFirstBySchedulerStatusOrderBySchedulerEndTimeDesc("success");
        verify(profileSyncAuditRepMock, times(1)).save(any(ProfileSyncAudit.class));
        verify(profileSyncConfigRepositoryMock, times(1)).save(any(SyncJobConfig.class));
        verify(profileSyncConfigRepositoryMock, times(1)).findByConfigName(firstSearchQuery);
        verify(profileSyncAudit, times(1)).setSchedulerStatus("success");
        verify(syncJobConfigMock, times(1)).setConfigRun(any(String.class));
        verify(profileSyncAudit, times(1)).setSchedulerStartTime(any());
    }


    @Test
    public void shouldSaveProfileSyncAuditWithFailStatusWhenThrowsException() {
        when(profileSyncConfigRepositoryMock.findByConfigName(firstSearchQuery)).thenReturn(syncJobConfigMock);
        when(syncJobConfigMock.getConfigRun()).thenReturn("1h");
        when(profileSyncAudit.getSchedulerEndTime()).thenReturn(LocalDateTime.now().minusMinutes(132));
        when(profileSyncAuditRepMock.findFirstBySchedulerStatusOrderBySchedulerEndTimeDesc("success"))
                .thenReturn(profileSyncAudit);
        when(profileSyncService.updateUserProfileFeed(any(), any())).thenThrow(UserProfileSyncException.class);

        userProfileSyncJobScheduler.updateIdamDataWithUserProfile();

        verify(profileSyncService, times(1)).updateUserProfileFeed(any(),any());
        verify(profileSyncAuditRepMock, times(1)).save(any(ProfileSyncAudit.class));
        verify(profileSyncConfigRepositoryMock, times(1)).findByConfigName(firstSearchQuery);
    }

    @Test
    public void objectUserProfileSyncSchedular() {
        UserProfileSyncJobScheduler userProfileSyncJobScheduler = new UserProfileSyncJobScheduler();
        assertThat(userProfileSyncJobScheduler).isNotNull();
    }

    @Test
    public void test_getLastBatchFailureTimeInHours() {
        String diff = userProfileSyncJobScheduler.getLastSuccessTimeInHours(LocalDateTime.now().minusMinutes(60));
        assertThat(diff).isNotEmpty().isEqualTo("1h");

        String diff1 = userProfileSyncJobScheduler.getLastSuccessTimeInHours(LocalDateTime.now().minusMinutes(15));
        assertThat(diff1).isNotEmpty().isEqualTo("1h");

        String diff2 = userProfileSyncJobScheduler.getLastSuccessTimeInHours(LocalDateTime.now());
        assertThat(diff2).isNotEmpty().isEqualTo("1h");

        String diff3 = userProfileSyncJobScheduler.getLastSuccessTimeInHours(LocalDateTime.now().plusMinutes(240));
        assertThat(diff3).isNotEmpty().isEqualTo("4h");

    }
}
