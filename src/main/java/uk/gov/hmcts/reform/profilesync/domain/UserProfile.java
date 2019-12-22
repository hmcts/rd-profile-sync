package uk.gov.hmcts.reform.profilesync.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserProfile {

    private String userIdentifier;
    private String email;
    private String firstName;
    private String lastName;
    private String idamStatus;
    private Integer idamRegistrationResponse;
}
