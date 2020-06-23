package uk.gov.hmcts.reform.profilesync.service.impl;

import feign.FeignException;
import feign.Response;

import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.profilesync.advice.UserProfileSyncException;
import uk.gov.hmcts.reform.profilesync.client.UserProfileClient;
import uk.gov.hmcts.reform.profilesync.domain.response.ErrorResponse;
import uk.gov.hmcts.reform.profilesync.domain.response.GetUserProfileResponse;
import uk.gov.hmcts.reform.profilesync.service.UserAcquisitionService;
import uk.gov.hmcts.reform.profilesync.util.JsonFeignResponseUtil;

@Slf4j
@AllArgsConstructor
@Service
public class UserAcquisitionServiceImpl implements UserAcquisitionService {

    @Autowired
    private final UserProfileClient userProfileClient;

    public Optional<GetUserProfileResponse> findUser(String bearerToken, String s2sToken, String id) throws UserProfileSyncException {

        GetUserProfileResponse userProfile = null;
        try (Response response = userProfileClient.findUser(bearerToken, s2sToken, id)) {

            Object  clazz = response.status() > 200 ? ErrorResponse.class : GetUserProfileResponse.class;
            ResponseEntity<Object> responseEntity = JsonFeignResponseUtil.toResponseEntity(response, clazz);

            if (response.status() == 400) {

                log.error("Bad Request to Update in User Profile:{}");
                ErrorResponse errorResponse = (ErrorResponse) responseEntity.getBody();
                throw new UserProfileSyncException(HttpStatus.valueOf(response.status()),errorResponse.getErrorDescription());

            } else if (responseEntity.getStatusCode().is2xxSuccessful()) {
                log.info(" User record to Update in User Profile:{}");
                userProfile = (GetUserProfileResponse) responseEntity.getBody();

            }

        } catch (FeignException ex) {
            //Do nothing, but log or insert an audit record.
            log.error("Exception occurred in findUser Service Call in UserProfile", ex);
            throw new UserProfileSyncException(HttpStatus.valueOf(500),"Failed UP Call");
        }

        return Optional.ofNullable(userProfile);
    }
}
