package uk.gov.hmcts.reform.profilesync.service;

import uk.gov.hmcts.reform.profilesync.domain.response.GetCaseWorkerProfileResponse;

import java.util.Optional;

public interface CaseWorkerAcquisitionService {

    Optional<GetCaseWorkerProfileResponse> findCaseWorkerUser(String bearerToken, String s2sToken, String id);
}
