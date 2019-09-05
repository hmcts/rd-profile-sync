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
import uk.gov.hmcts.reform.profilesync.client.IdamClient;
import uk.gov.hmcts.reform.profilesync.client.UserProfileClient;
import uk.gov.hmcts.reform.profilesync.domain.GetUserProfileResponse;
import uk.gov.hmcts.reform.profilesync.domain.IdamStatus;
import uk.gov.hmcts.reform.profilesync.domain.Source;
import uk.gov.hmcts.reform.profilesync.domain.SyncJobAudit;
import uk.gov.hmcts.reform.profilesync.domain.UserProfile;
import uk.gov.hmcts.reform.profilesync.domain.UserProfileSyncException;
import uk.gov.hmcts.reform.profilesync.repository.SyncJobRepository;
import uk.gov.hmcts.reform.profilesync.service.ProfileUpdateService;
import uk.gov.hmcts.reform.profilesync.service.UserAcquisitionService;

@Slf4j
@AllArgsConstructor
@Service
public class ProfileUpdateServiceImpl implements ProfileUpdateService {

    @Autowired
    protected   UserAcquisitionService userAcquisitionService;

    @Autowired
    private final UserProfileClient userProfileClient;

    @Autowired
    private final SyncJobRepository syncJobRepository;

    public void updateUserProfile(String searchQuery, String bearerToken, String s2sToken, List<IdamClient.User> users) throws UserProfileSyncException {
        log.info("In side updateUserProfile:: ");
        users.forEach(user -> {
            Optional<GetUserProfileResponse> userProfile = userAcquisitionService.findUser(bearerToken, s2sToken, user.getId());

            if (userProfile.isPresent()) {
                Map<String, Boolean> status = new HashMap<String, Boolean>();
                status.put(IdamStatus.ACTIVE.name(), user.isActive());
                status.put(IdamStatus.PENDING.name(), user.isPending());
                status.put(IdamStatus.LOCKED.name(), user.isLocked());
                UserProfile updatedUserProfile = UserProfile.builder()
                        .email(user.getEmail())
                        .firstName(user.getForename())
                        .lastName(user.getSurname())
                        .idamStatus(idamStatusResolver().get(status) != null ? idamStatusResolver().get(status).name() : "PENDING")
                        .build();

                try {

                    syncUser(bearerToken,s2sToken,user.getId().toString(),updatedUserProfile);

                } catch (UserProfileSyncException e) {

                    log.error("User Not updated : Id - {}", user.getId());
                }
                log.info("User updated : Id - {}", user.getId());
            }
            log.info("User Not find in UP: Id - {}", user.getId());
        });
    }

    private void syncUser(String bearerToken, String s2sToken,
                          String userId, UserProfile updatedUserProfile)throws UserProfileSyncException {

        log.info("Inside  syncUser:: method");
        Response response = userProfileClient.syncUserStatus(bearerToken, s2sToken, userId, updatedUserProfile);

        if (response.status() > 300) {

            log.error("Exception occurred while updating the user profile: Status - {}", response.status());
            saveSyncJobAudit(response.status(),"fail");
            throw new UserProfileSyncException(HttpStatus.valueOf(response.status()),"Failed to update");

        }

        log.error("Successfully updated the user profile: Status - {}" + userId);
    }

    private void saveSyncJobAudit(Integer idamResponse,String message) {

        SyncJobAudit syncJobAudit = new SyncJobAudit(idamResponse, message, Source.SYNC);
        syncJobRepository.save(syncJobAudit);
    }

    public Map<Map<String, Boolean>, IdamStatus> idamStatusResolver() {

        Map<Map<String, Boolean>, IdamStatus> idamStatusMap = new HashMap<Map<String, Boolean>, IdamStatus>();
        idamStatusMap.put(addRule(false,true, false), IdamStatus.PENDING);
        idamStatusMap.put(addRule(true, false,false), IdamStatus.ACTIVE);
        idamStatusMap.put(addRule(true, false,true), IdamStatus.ACTIVE_AND_LOCKED);
        idamStatusMap.put(addRule(false,false,false), IdamStatus.SUSPENDED);
        idamStatusMap.put(addRule(false,false,true), IdamStatus.SUSPENDED_AND_LOCKED);
        idamStatusMap.put(addRule(false,false,true), IdamStatus.SUSPENDED_AND_LOCKED);

        return idamStatusMap;
    }

    public  Map<String, Boolean> addRule(boolean activeFlag, boolean pendingFlag, boolean lockedFlag) {
        Map<String, Boolean> pendingMapWithRules = new HashMap<>();
        pendingMapWithRules.put("ACTIVE", activeFlag);
        pendingMapWithRules.put("PENDING", pendingFlag);
        pendingMapWithRules.put("LOCKED", lockedFlag);
        return pendingMapWithRules;
    }

}
