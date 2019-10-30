package uk.gov.hmcts.reform.profilesync.service.impl;

import feign.FeignException;
import feign.Response;

import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.profilesync.client.UserProfileClient;
import uk.gov.hmcts.reform.profilesync.domain.GetUserProfileResponse;
import uk.gov.hmcts.reform.profilesync.service.UserAcquisitionService;
import uk.gov.hmcts.reform.profilesync.util.JsonFeignResponseHelper;

@Slf4j
@AllArgsConstructor
@Service
public class UserAcquisitionServiceImpl implements UserAcquisitionService {

    @Autowired
    private final UserProfileClient userProfileClient;

    public Optional<GetUserProfileResponse> findUser(String bearerToken, String s2sToken, String id) {

        log.info("In side findUser::UserAcquisitionServiceImpl");
        GetUserProfileResponse userProfile = null;
        try (Response response = userProfileClient.findUser(bearerToken, s2sToken, id)) {

            ResponseEntity responseEntity = JsonFeignResponseHelper.toResponseEntity(response, GetUserProfileResponse.class);

            if (response.status() > 300) {

                log.error("No record to Update in User Profile:{}");

            } else if (responseEntity.getStatusCode().is2xxSuccessful()) {
                userProfile = (GetUserProfileResponse) responseEntity.getBody();
                log.info("Found record in User Profile with idamId = {}", userProfile.getIdamId());

            }

        } catch (FeignException ex) {
            //Do nothing, but log or insert an audit record.
            log.error("Exception occurred : Status - {}, Content - {}", ex.status());
        }

        return Optional.ofNullable(userProfile);
    }
}
