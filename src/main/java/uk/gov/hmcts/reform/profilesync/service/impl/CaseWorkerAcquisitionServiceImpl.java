package uk.gov.hmcts.reform.profilesync.service.impl;

import feign.FeignException;
import feign.Response;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.profilesync.advice.UserProfileSyncException;
import uk.gov.hmcts.reform.profilesync.client.CaseWorkerRefApiClient;
import uk.gov.hmcts.reform.profilesync.domain.response.ErrorResponse;
import uk.gov.hmcts.reform.profilesync.domain.response.GetCaseWorkerProfileResponse;
import uk.gov.hmcts.reform.profilesync.service.CaseWorkerAcquisitionService;
import uk.gov.hmcts.reform.profilesync.util.JsonFeignResponseUtil;

import java.util.Optional;


@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@Service
public class CaseWorkerAcquisitionServiceImpl implements CaseWorkerAcquisitionService {

    @Autowired
    CaseWorkerRefApiClient caseWorkerRefApiClient;

    @Value("${loggingComponentName}")
    protected String loggingComponentName;

    @Override
    public Optional<GetCaseWorkerProfileResponse> findCaseWorkerUser(String bearerToken, String s2sToken, String id)
            throws
            UserProfileSyncException {

        GetCaseWorkerProfileResponse caseWorkerProfileResponse = null;
        ResponseEntity<Object> responseEntity = null;
        String message = "Failed CaseWorker Call";

        try {

            Response response = caseWorkerRefApiClient.findCaseWorkerUser(bearerToken, s2sToken, id);
            Object  clazz = response.status() > 200 ? ErrorResponse.class : GetCaseWorkerProfileResponse.class;
            if (response.status() == 400 || response.status() == 401) {
                message = "Service failed in findCaseWorkerUser method";
                log.error("{}:: Service failed in findCaseWorkerUser method ::", loggingComponentName);
                throw new UserProfileSyncException(HttpStatus.valueOf(response.status()),message);
            } else if (response.status() == 200) {
                log.info("{}: User record to Update in User Profile:{}", loggingComponentName);
                responseEntity = JsonFeignResponseUtil.toResponseEntity(response, clazz);
                caseWorkerProfileResponse = (GetCaseWorkerProfileResponse) responseEntity.getBody();

            } else {
                log.info("{}:: User record Not found to Update in CaseWorker profile:", loggingComponentName);
            }

        } catch (FeignException ex) {
            //Do nothing, but log or insert an audit record.
            log.error("{}:: Exception occurred in findCaseWorkerUser Service Call in CaseWokerProfile::{}",
                            loggingComponentName, ex);
            throw new UserProfileSyncException(HttpStatus.valueOf(500),message);
        }

        return Optional.ofNullable(caseWorkerProfileResponse);
    }
}
