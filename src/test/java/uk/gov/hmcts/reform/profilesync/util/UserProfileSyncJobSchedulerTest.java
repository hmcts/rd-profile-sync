package uk.gov.hmcts.reform.profilesync.util;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import org.junit.Test;
import uk.gov.hmcts.reform.profilesync.domain.SyncJobAudit;
import uk.gov.hmcts.reform.profilesync.domain.UserProfileSyncException;
import uk.gov.hmcts.reform.profilesync.repository.SyncJobRepository;
import uk.gov.hmcts.reform.profilesync.service.ProfileSyncService;


public class UserProfileSyncJobSchedulerTest {

    private final SyncJobRepository syncJobRepository = mock(SyncJobRepository.class);
    private final ProfileSyncService profileSyncService = mock(ProfileSyncService.class);
    private SyncJobAudit syncJobAuditMock = mock(SyncJobAudit.class);
    private final UserProfileSyncJobScheduler userProfileSyncJobScheduler = new UserProfileSyncJobScheduler(profileSyncService, syncJobRepository);


    @Test
    public void test_updateIdamDataWithUserProfile() {
        when(syncJobRepository.findFirstByStatusOrderByAuditTsDesc("fail")).thenReturn(syncJobAuditMock);
        when(syncJobRepository.findFirstByStatusOrderByAuditTsDesc("success")).thenReturn(syncJobAuditMock);
        when(syncJobAuditMock.getAuditTs()).thenReturn(LocalDateTime.now().minusHours(1));
        doNothing().when(profileSyncService).updateUserProfileFeed(any(String.class));

        userProfileSyncJobScheduler.updateIdamDataWithUserProfile();

        verify(syncJobRepository, times(1)).save(any(SyncJobAudit.class));
    }

    @Test
    public void test_updateIdamDataWithUserProfileThrowsException() {
        when(syncJobRepository.findFirstByStatusOrderByAuditTsDesc("fail")).thenReturn(syncJobAuditMock);
        when(syncJobRepository.findFirstByStatusOrderByAuditTsDesc("success")).thenReturn(syncJobAuditMock);
        when(syncJobAuditMock.getAuditTs()).thenReturn(LocalDateTime.now().minusMinutes(132));
        doThrow(UserProfileSyncException.class).when(profileSyncService).updateUserProfileFeed(any(String.class));

        userProfileSyncJobScheduler.updateIdamDataWithUserProfile();

        verify(syncJobRepository, times(1)).save(any(SyncJobAudit.class));
    }
}
