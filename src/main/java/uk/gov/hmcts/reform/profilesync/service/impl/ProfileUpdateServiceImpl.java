package uk.gov.hmcts.reform.profilesync.service.impl;

import feign.Response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.profilesync.advice.UserProfileSyncException;
import uk.gov.hmcts.reform.profilesync.client.IdamClient;
import uk.gov.hmcts.reform.profilesync.client.UserProfileClient;
import uk.gov.hmcts.reform.profilesync.constants.IdamStatus;
import uk.gov.hmcts.reform.profilesync.domain.ProfileSyncAudit;
import uk.gov.hmcts.reform.profilesync.domain.ProfileSyncAuditDetails;
import uk.gov.hmcts.reform.profilesync.domain.ProfileSyncAuditDetailsId;
import uk.gov.hmcts.reform.profilesync.domain.UserProfile;
import uk.gov.hmcts.reform.profilesync.domain.response.ErrorResponse;
import uk.gov.hmcts.reform.profilesync.domain.response.GetUserProfileResponse;
import uk.gov.hmcts.reform.profilesync.repository.ProfileSyncAuditDetailsRepository;
import uk.gov.hmcts.reform.profilesync.repository.SyncJobRepository;
import uk.gov.hmcts.reform.profilesync.service.ProfileUpdateService;
import uk.gov.hmcts.reform.profilesync.service.UserAcquisitionService;
import uk.gov.hmcts.reform.profilesync.util.JsonFeignResponseUtil;

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

    @Autowired
    private final ProfileSyncAuditDetailsRepository profileSyncAuditDetailsRepository;

    public ProfileSyncAudit updateUserProfile(String searchQuery, String bearerToken, String s2sToken, List<IdamClient.User> users, ProfileSyncAudit syncAudit) throws UserProfileSyncException {
        log.info("Inside updateUserProfile:: ");
        List<ProfileSyncAuditDetails> profileSyncAuditDetails = new ArrayList<>();
        users.forEach(user -> {
            Optional<GetUserProfileResponse> userProfile = userAcquisitionService.findUser(bearerToken, s2sToken, user.getId());

            if (userProfile.isPresent()) {
                StringBuilder sb = new StringBuilder();
                sb.append(user.isActive());
                sb.append(user.isPending());
                UserProfile updatedUserProfile = UserProfile.builder()
                        .email(user.getEmail())
                        .firstName(user.getForename())
                        .lastName(user.getSurname())
                        .idamStatus(resolveIdamStatus(sb))
                        .build();

                try {
                    profileSyncAuditDetails.add(syncUser(bearerToken, s2sToken, user.getId(), updatedUserProfile,syncAudit));

                } catch (UserProfileSyncException e) {
                    syncAudit.setSchedulerStatus("fail");
                    log.error("User Not updated : - {}",e.getErrorMessage());
                }
                log.info("User details captured to update the status for User Profile");
            }
        });
        syncAudit.setProfileSyncAuditDetails(profileSyncAuditDetails);
        return syncAudit;
    }

    private ProfileSyncAuditDetails syncUser(String bearerToken, String s2sToken,
                          String userId, UserProfile updatedUserProfile, ProfileSyncAudit syncAudit) throws UserProfileSyncException {

        log.info("Inside  syncUser:: method");
        String message = "success";
        Response response = userProfileClient.syncUserStatus(bearerToken, s2sToken, userId, updatedUserProfile);
        log.info("Body response::" + response.body());
        if (response.status() > 300) {
            log.error("Exception occurred while updating the user profile: Status - {}", response.status());
            message = response.reason();
            syncAudit.setSchedulerStatus("fail");
            if (response.body() != null) {
                Object  clazz =  ErrorResponse.class;
                ResponseEntity<Object> responseEntity = JsonFeignResponseUtil.toResponseEntity(response, clazz);
                ErrorResponse errorResponse = (ErrorResponse) responseEntity.getBody();
                message = errorResponse.getErrorDescription() != null ? errorResponse.getErrorDescription() : response.reason();
            }

        }
        return  new ProfileSyncAuditDetails(new ProfileSyncAuditDetailsId(syncAudit,userId),response.status(),message, LocalDateTime.now());
    }


    public static String resolveIdamStatus(StringBuilder stringBuilder) {

        switch (stringBuilder.toString().toLowerCase()) {
            case "falsetrue":
                return IdamStatus.PENDING.name();
            case "truefalse":
                return IdamStatus.ACTIVE.name();
            default:
                return IdamStatus.SUSPENDED.name();
        }
    }

}
