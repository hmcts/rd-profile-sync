package uk.gov.hmcts.reform.profilesync.service;

import java.util.List;

import uk.gov.hmcts.reform.profilesync.advice.UserProfileSyncException;
import uk.gov.hmcts.reform.profilesync.client.IdamClient;

public interface ProfileSyncService {

    String getBearerToken();

    String getS2sToken();

    List<IdamClient.User> getSyncFeed(String bearerToken, String searchQuery);

    void updateUserProfileFeed(String searchQuery) throws UserProfileSyncException;

}
