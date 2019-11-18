package uk.gov.hmcts.reform.profilesync.service;

import java.util.List;

import uk.gov.hmcts.reform.profilesync.client.IdamClient;
import uk.gov.hmcts.reform.profilesync.domain.UserProfileSyncException;

public interface ProfileSyncService {
    public static String BASIC = "Basic ";
    public static String BEARER = "Bearer ";

    String getBearerToken();

    String getS2sToken();

    List<IdamClient.User> getSyncFeed(String bearerToken, String searchQuery);

    void updateUserProfileFeed(String searchQuery) throws UserProfileSyncException;

}
