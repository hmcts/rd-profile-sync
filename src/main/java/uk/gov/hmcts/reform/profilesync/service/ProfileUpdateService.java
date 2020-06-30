package uk.gov.hmcts.reform.profilesync.service;

import java.util.List;

import uk.gov.hmcts.reform.profilesync.advice.UserProfileSyncException;
import uk.gov.hmcts.reform.profilesync.client.IdamClient;

public interface ProfileUpdateService {

    void updateUserProfile(String searchQuery, String bearerToken, String s2sToken, List<IdamClient.User> users)
            throws UserProfileSyncException;

}
