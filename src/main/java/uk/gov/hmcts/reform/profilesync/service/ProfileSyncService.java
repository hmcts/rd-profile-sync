package uk.gov.hmcts.reform.profilesync.service;

import java.util.List;

import uk.gov.hmcts.reform.profilesync.client.IdamClient;
import uk.gov.hmcts.reform.profilesync.domain.UserProfileSyncException;

public interface ProfileSyncService {

    String getBearerToken();

    String getS2sToken();

    List<IdamClient.User> getSyncFeed(String bearerToken, String searchQuery);

    void updateUserProfileFeed(String searchQuery) throws UserProfileSyncException;

}
