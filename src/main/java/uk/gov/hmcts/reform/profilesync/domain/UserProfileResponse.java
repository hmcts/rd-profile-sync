package uk.gov.hmcts.reform.profilesync.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserProfileResponse {

    private String idamId;
    private Integer idamRegistrationResponse;

    public UserProfileResponse(UserProfile userProfile) {

        this.idamId = userProfile.getUserIdentifier();
        this.idamRegistrationResponse = userProfile.getIdamRegistrationResponse();
    }
}
