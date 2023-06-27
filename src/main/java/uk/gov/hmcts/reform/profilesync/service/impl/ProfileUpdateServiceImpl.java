package uk.gov.hmcts.reform.profilesync.service.impl;

import feign.Response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.profilesync.advice.UserProfileSyncException;
import uk.gov.hmcts.reform.profilesync.client.CaseWorkerRefApiClient;
import uk.gov.hmcts.reform.profilesync.client.IdamClient;
import uk.gov.hmcts.reform.profilesync.client.UserProfileClient;
import uk.gov.hmcts.reform.profilesync.constants.IdamStatus;
import uk.gov.hmcts.reform.profilesync.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.profilesync.domain.ProfileSyncAudit;
import uk.gov.hmcts.reform.profilesync.domain.ProfileSyncAuditDetails;
import uk.gov.hmcts.reform.profilesync.domain.ProfileSyncAuditDetailsId;
import uk.gov.hmcts.reform.profilesync.domain.UserProfile;
import uk.gov.hmcts.reform.profilesync.domain.response.GetUserProfileResponse;
import uk.gov.hmcts.reform.profilesync.service.ProfileUpdateService;
import uk.gov.hmcts.reform.profilesync.service.UserAcquisitionService;

@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@Service
public class ProfileUpdateServiceImpl implements ProfileUpdateService {

    @Autowired
    protected UserAcquisitionService userAcquisitionService;

    @Autowired
    private UserProfileClient userProfileClient;

    @Autowired
    private CaseWorkerRefApiClient caseWorkerRefApiClient;

    @Value("${loggingComponentName}")
    private String loggingComponentName;

    public ProfileSyncAudit updateUserProfile(String searchQuery, String bearerToken, String s2sToken,
                                              Set<IdamClient.User> users, ProfileSyncAudit syncAudit)
            throws UserProfileSyncException {
        log.info("{}:: Inside updateUserProfile::{}", loggingComponentName);
        List<ProfileSyncAuditDetails> profileSyncAuditDetails = new ArrayList<>();
        List<ProfileSyncAuditDetails> caseWorkerSyncAuditDetails = new ArrayList<>();

        users.forEach(user -> {
            Optional<GetUserProfileResponse> userProfile = userAcquisitionService.findUser(bearerToken, s2sToken,
                    user.getId());

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
                    //to update user profile details for matching user ids are collecting and storing in the list
                    // from syncUser method.
                    profileSyncAuditDetails.add(syncUser(bearerToken, s2sToken, user.getId(),
                            updatedUserProfile, syncAudit));

                } catch (UserProfileSyncException e) {
                    syncAudit.setSchedulerStatus("fail");
                    log.error("{}:: User Not updated : - {}", loggingComponentName, e.getErrorMessage());
                }
                log.info("{}:: User Status updated in User Profile::{}", loggingComponentName);
            }

        });

        users.forEach(user -> {
            if (user.getRoles().contains("cwd-user")) {
                StringBuilder sb = new StringBuilder();
                sb.append(user.isActive());
                sb.append(user.isPending());

                CaseWorkerProfile updateCaseWorkerProfile = CaseWorkerProfile.builder()
                        .email(user.getEmail())
                        .userId(user.getId())
                        .firstName(user.getForename())
                        .lastName(user.getSurname())
                        .idamStatus(resolveIdamStatusForCaseWorker(sb))
                        .build();

                try {
                    //to update caseworker profile details for matching user ids are collecting and storing in the list
                    // from syncCaseWorkerUser method.
                    caseWorkerSyncAuditDetails.add(syncCaseWorkerUser(bearerToken, s2sToken, user.getId(),
                            updateCaseWorkerProfile, syncAudit));

                } catch (UserProfileSyncException e) {
                    syncAudit.setSchedulerStatus("fail");
                    log.error("{}:: User Not updated : - {}", loggingComponentName, e.getErrorMessage());
                }


            }
        });

        syncAudit.setProfileSyncAuditDetails(profileSyncAuditDetails);
        syncAudit.setProfileSyncAuditDetails(caseWorkerSyncAuditDetails);
        return syncAudit;
    }

    private  ProfileSyncAuditDetails syncUser(String bearerToken, String s2sToken,
                                              String userId, UserProfile updatedUserProfile,
                                              ProfileSyncAudit syncAudit)
            throws UserProfileSyncException {

        log.info("{}:: Inside  syncUser method ::{}", loggingComponentName);
        Response response = userProfileClient.syncUserStatus(bearerToken, s2sToken, userId, updatedUserProfile);
        String message = "success";
        if (response.status() > 300) {
            log.error("{}:: Exception occurred while updating the user profile: Status - {}"
                            + response.status(), loggingComponentName);
            message = "the user profile failed while updating the status";
            syncAudit.setSchedulerStatus("fail");
        }
        return  new ProfileSyncAuditDetails(new ProfileSyncAuditDetailsId(syncAudit,userId),response.status(),message,
                LocalDateTime.now());
    }


    private ProfileSyncAuditDetails syncCaseWorkerUser(String bearerToken, String s2sToken,
                                                                String userId, CaseWorkerProfile caseWorkerProfile,
                                                                ProfileSyncAudit syncAudit)
            throws UserProfileSyncException {

        log.info("{}:: Inside  syncCaseWorkerUser method ::{}", loggingComponentName);
        Response response = caseWorkerRefApiClient
                            .syncCaseWorkerUserStatus(bearerToken, s2sToken, caseWorkerProfile);
        String message = "success";
        if (response.status() > 300) {
            log.error("{}:: Exception occurred while updating the case worker profile: Status - {}"
                    + response.status(), loggingComponentName);
            message = "the case worker failed while updating the status";
        }

        log.info("{}:: CaseWorker Status updated in Case Worker DB::{}", loggingComponentName);
        return  new ProfileSyncAuditDetails(new ProfileSyncAuditDetailsId(syncAudit,userId),response.status(),message,
                LocalDateTime.now());
    }

    public  String resolveIdamStatus(StringBuilder stringBuilder) {

        if (stringBuilder.toString().equalsIgnoreCase("falsetrue")) {
            return IdamStatus.PENDING.name();
        } else if (stringBuilder.toString().equalsIgnoreCase("truefalse")) {
            return IdamStatus.ACTIVE.name();
        } else {
            return IdamStatus.SUSPENDED.name();
        }
    }

    public  boolean resolveIdamStatusForCaseWorker(StringBuilder stringBuilder) {

        if (stringBuilder.toString().equalsIgnoreCase("falsetrue")) {
            return false;
        } else if (stringBuilder.toString().equalsIgnoreCase("truefalse")) {
            return false;
        } else {
            return true;
        }
    }

}
