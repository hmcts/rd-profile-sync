package uk.gov.hmcts.reform.profilesync.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.Test;
import uk.gov.hmcts.reform.profilesync.advice.UserProfileSyncException;
import uk.gov.hmcts.reform.profilesync.domain.SyncJobAudit;
import uk.gov.hmcts.reform.profilesync.domain.SyncJobConfig;
import uk.gov.hmcts.reform.profilesync.repository.SyncConfigRepository;
import uk.gov.hmcts.reform.profilesync.repository.SyncJobRepository;
import uk.gov.hmcts.reform.profilesync.schedular.UserProfileSyncJobScheduler;
import uk.gov.hmcts.reform.profilesync.service.ProfileSyncService;


public class UserProfileSyncJobSchedulerTest {

    private final SyncJobRepository syncJobRepository = mock(SyncJobRepository.class);
    private final ProfileSyncService profileSyncService = mock(ProfileSyncService.class);
    private final SyncConfigRepository syncConfigRepositoryMock = mock(SyncConfigRepository.class);
    private SyncJobAudit syncJobAuditMock = mock(SyncJobAudit.class);
    private SyncJobConfig syncJobConfigMock = mock(SyncJobConfig.class);

    private UserProfileSyncJobScheduler userProfileSyncJobScheduler = new UserProfileSyncJobScheduler(profileSyncService, syncJobRepository,syncConfigRepositoryMock,"1h");


    @Test
    public void test_updateIdamDataWithUserProfileWithDbValue() {

        when(syncConfigRepositoryMock.findByConfigName("firstsearchquery")).thenReturn(syncJobConfigMock);
        when(syncJobConfigMock.getConfigRun()).thenReturn("2h");
        when(syncJobRepository.findFirstByStatusOrderByAuditTsDesc("success")).thenReturn(syncJobAuditMock);
        when(syncJobAuditMock.getAuditTs()).thenReturn(LocalDateTime.now().minusHours(1));
        doNothing().when(profileSyncService).updateUserProfileFeed(any(String.class));

        userProfileSyncJobScheduler.updateIdamDataWithUserProfile();

        verify(syncJobRepository, times(1)).save(any(SyncJobAudit.class));
        verify(syncConfigRepositoryMock, times(1)).save(any(SyncJobConfig.class));

    }


    @Test
    public void test_updateIdamDataWithUserProfile() {

        when(syncConfigRepositoryMock.findByConfigName("firstsearchquery")).thenReturn(syncJobConfigMock);
        when(syncJobConfigMock.getConfigRun()).thenReturn("2h");
        when(syncJobRepository.findFirstByStatusOrderByAuditTsDesc("success")).thenReturn(syncJobAuditMock);
        when(syncJobAuditMock.getAuditTs()).thenReturn(LocalDateTime.now().minusHours(1));
        doNothing().when(profileSyncService).updateUserProfileFeed(any(String.class));

        userProfileSyncJobScheduler.updateIdamDataWithUserProfile();

        verify(syncJobRepository, times(1)).save(any(SyncJobAudit.class));
        verify(syncConfigRepositoryMock, times(1)).save(any(SyncJobConfig.class));

    }

    @Test
    public void test_updateIdamDataWithUserProfileThrowsException() {

        when(syncConfigRepositoryMock.findByConfigName("firstsearchquery")).thenReturn(syncJobConfigMock);
        when(syncJobConfigMock.getConfigRun()).thenReturn("1h");
        when(syncJobRepository.findFirstByStatusOrderByAuditTsDesc("success")).thenReturn(syncJobAuditMock);
        when(syncJobAuditMock.getAuditTs()).thenReturn(LocalDateTime.now().minusMinutes(132));
        doThrow(UserProfileSyncException.class).when(profileSyncService).updateUserProfileFeed(any(String.class));

        userProfileSyncJobScheduler.updateIdamDataWithUserProfile();

        verify(syncJobRepository, times(1)).save(any(SyncJobAudit.class));

    }

    @Test
    public void test_objectUserProfileSyncSchedular() {

        UserProfileSyncJobScheduler userProfileSyncJobScheduler = new UserProfileSyncJobScheduler();
        assertThat(userProfileSyncJobScheduler).isNotNull();
    }
}
