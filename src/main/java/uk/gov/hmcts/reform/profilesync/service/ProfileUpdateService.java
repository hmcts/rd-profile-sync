package uk.gov.hmcts.reform.profilesync.service;

import java.util.Set;

import uk.gov.hmcts.reform.profilesync.advice.UserProfileSyncException;
import uk.gov.hmcts.reform.profilesync.client.IdamClient;
import uk.gov.hmcts.reform.profilesync.domain.ProfileSyncAudit;

public interface ProfileUpdateService {

    ProfileSyncAudit updateUserProfile(String searchQuery, String bearerToken, String s2sToken, Set<IdamClient.User>
            users, ProfileSyncAudit syncAudit) throws UserProfileSyncException;

}
