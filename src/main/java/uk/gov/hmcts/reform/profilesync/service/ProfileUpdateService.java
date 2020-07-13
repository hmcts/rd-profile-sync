package uk.gov.hmcts.reform.profilesync.service;

import java.util.List;

import uk.gov.hmcts.reform.profilesync.advice.UserProfileSyncException;
import uk.gov.hmcts.reform.profilesync.client.IdamClient;
import uk.gov.hmcts.reform.profilesync.domain.ProfileSyncAudit;

public interface ProfileUpdateService {

    ProfileSyncAudit updateUserProfile(String searchQuery, String bearerToken, String s2sToken, List<IdamClient.User> users, ProfileSyncAudit syncAudit) throws UserProfileSyncException;

}
