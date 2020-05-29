package uk.gov.hmcts.reform.profilesync.service.impl;

import feign.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.profilesync.advice.UserProfileSyncException;
import uk.gov.hmcts.reform.profilesync.client.IdamClient;
import uk.gov.hmcts.reform.profilesync.client.UserProfileClient;
import uk.gov.hmcts.reform.profilesync.constants.IdamStatus;
import uk.gov.hmcts.reform.profilesync.constants.Source;
import uk.gov.hmcts.reform.profilesync.domain.SyncJobAudit;
import uk.gov.hmcts.reform.profilesync.domain.UserProfile;
import uk.gov.hmcts.reform.profilesync.domain.response.GetUserProfileResponse;
import uk.gov.hmcts.reform.profilesync.repository.SyncJobRepository;
import uk.gov.hmcts.reform.profilesync.service.ProfileUpdateService;
import uk.gov.hmcts.reform.profilesync.service.UserAcquisitionService;

@Slf4j
@AllArgsConstructor
@Service
public class ProfileUpdateServiceImpl implements ProfileUpdateService {

    @Autowired
    protected UserAcquisitionService userAcquisitionService;

    @Autowired
    private final UserProfileClient userProfileClient;

    @Autowired
    private final SyncJobRepository syncJobRepository;

    public void updateUserProfile(String searchQuery, String bearerToken, String s2sToken, List<IdamClient.User> users) throws UserProfileSyncException {
        log.info("Inside updateUserProfile:: ");
        users.forEach(user -> {
            Optional<GetUserProfileResponse> userProfile = userAcquisitionService.findUser(bearerToken, s2sToken, user.getId());

            if (userProfile.isPresent()) {
                Map<String, Boolean> status = new HashMap<String, Boolean>();
                status.put(IdamStatus.ACTIVE.name(), user.isActive());
                status.put(IdamStatus.PENDING.name(), user.isPending());
                UserProfile updatedUserProfile = UserProfile.builder()
                        .email(user.getEmail())
                        .firstName(user.getForename())
                        .lastName(user.getSurname())
                        .idamStatus(idamStatusResolver().get(status) != null ? idamStatusResolver().get(status).name() : IdamStatus.SUSPENDED.name())
                        .build();

                try {

                    syncUser(bearerToken, s2sToken, user.getId(), updatedUserProfile);

                } catch (UserProfileSyncException e) {

                    log.error("User Not updated : - {}",e.getErrorMessage());
                }
                log.info("User Status updated in User Profile");
            }

        });
    }

    private void syncUser(String bearerToken, String s2sToken,
                          String userId, UserProfile updatedUserProfile) throws UserProfileSyncException {

        log.info("Inside  syncUser:: method");
        Response response = userProfileClient.syncUserStatus(bearerToken, s2sToken, userId, updatedUserProfile);

        log.info("Body response::" + response.body().toString());
        if (response.status() > 300) {

            log.error("Exception occurred while updating the user profile: Status - {}" + userId + ":" + updatedUserProfile.getIdamStatus());
            log.error("Exception occurred while updating the user profile: http Status - {}", response.status());
            saveSyncJobAudit(response.status(), "fail");
            throw new UserProfileSyncException(HttpStatus.valueOf(response.status()), "Failed to update");

        }

        log.info("Successfully updated the user profile: Status - {}");
    }

    private void saveSyncJobAudit(Integer idamResponse, String message) {

        SyncJobAudit syncJobAudit = new SyncJobAudit(idamResponse, message, Source.SYNC);
        syncJobRepository.save(syncJobAudit);
    }

    public Map<Map<String, Boolean>, IdamStatus> idamStatusResolver() {

        Map<Map<String, Boolean>, IdamStatus> idamStatusMap = new HashMap<Map<String, Boolean>, IdamStatus>();
        idamStatusMap.put(addRule(false, true), IdamStatus.PENDING);
        idamStatusMap.put(addRule(true, false), IdamStatus.ACTIVE);
        idamStatusMap.put(addRule(false, false), IdamStatus.SUSPENDED);

        return idamStatusMap;
    }

    public Map<String, Boolean> addRule(boolean activeFlag, boolean pendingFlag) {
        Map<String, Boolean> pendingMapWithRules = new HashMap<>();
        pendingMapWithRules.put("ACTIVE", activeFlag);
        pendingMapWithRules.put("PENDING", pendingFlag);
        return pendingMapWithRules;
    }

}
