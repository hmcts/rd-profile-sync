package uk.gov.hmcts.reform.profilesync.service;

import java.util.Optional;
import uk.gov.hmcts.reform.profilesync.domain.response.GetUserProfileResponse;

public interface UserAcquisitionService {

    Optional<GetUserProfileResponse> findUser(String bearerToken, String s2sToken, String id);

}
