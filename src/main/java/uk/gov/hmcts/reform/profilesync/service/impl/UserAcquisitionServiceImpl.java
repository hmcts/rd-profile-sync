package uk.gov.hmcts.reform.profilesync.service.impl;

import feign.FeignException;
import feign.Response;

import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
@NoArgsConstructor
@AllArgsConstructor
@Service
public class UserAcquisitionServiceImpl implements UserAcquisitionService {

    @Autowired
    private UserProfileClient userProfileClient;

    @Value("${loggingComponentName}")
    protected String loggingComponentName;

    public Optional<GetUserProfileResponse> findUser(String bearerToken, String s2sToken, String id) throws
            UserProfileSyncException {

        GetUserProfileResponse userProfile = null;
        ResponseEntity<Object> responseEntity = null;
        String message = "Failed UP Call";
        try {
            Response response = userProfileClient.findUser(bearerToken, s2sToken, id);
            Object  clazz = response.status() > 200 ? ErrorResponse.class : GetUserProfileResponse.class;
            if (response.status() == 400 || response.status() == 401) {
                message = "Service failed in findUser method";
                log.error("{}:: Service failed in findUser method ::", loggingComponentName);
                throw new UserProfileSyncException(HttpStatus.valueOf(response.status()),message);
            } else if (response.status() == 200) {
                log.info("{}: User record to Update in User Profile:{}", loggingComponentName);
                responseEntity = JsonFeignResponseUtil.toResponseEntity(response, clazz);
                userProfile = (GetUserProfileResponse) responseEntity.getBody();

            } else {
                log.info("{}:: User record Not found to Update in User Profile:", loggingComponentName);
            }

        } catch (FeignException ex) {
            //Do nothing, but log or insert an audit record.
            log.error("{}:: Exception occurred in findUser Service Call in UserProfile::{}", loggingComponentName, ex);
            throw new UserProfileSyncException(HttpStatus.valueOf(500),message);
        }

        return Optional.ofNullable(userProfile);
    }
}
