package uk.gov.hmcts.reform.profilesync.service;

import java.util.List;

import uk.gov.hmcts.reform.profilesync.advice.UserProfileSyncException;
import uk.gov.hmcts.reform.profilesync.client.IdamClient;
import uk.gov.hmcts.reform.profilesync.domain.ProfileSyncAudit;

public interface ProfileSyncService {

    String getBearerToken();

    String getS2sToken();

    List<IdamClient.User> getSyncFeed(String bearerToken, String searchQuery);

    ProfileSyncAudit updateUserProfileFeed(String searchQuery, ProfileSyncAudit syncAudit) throws UserProfileSyncException;

}
