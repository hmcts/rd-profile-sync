package uk.gov.hmcts.reform.profilesync.domain.response;

import static java.util.Objects.requireNonNull;

import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.profilesync.domain.UserProfile;

@Getter
@NoArgsConstructor
public class GetUserProfileResponse {

    private String userIdentifier;
    private String email;
    private String firstName;
    private String lastName;
    private String idamStatus;

    public GetUserProfileResponse(UserProfile userProfile) {

        requireNonNull(userProfile, "userProfile must not be null");
        this.userIdentifier = userProfile.getUserIdentifier();
        this.email = userProfile.getEmail();
        this.firstName = userProfile.getFirstName();
        this.lastName = userProfile.getLastName();
        this.idamStatus = userProfile.getIdamStatus();
    }

}
