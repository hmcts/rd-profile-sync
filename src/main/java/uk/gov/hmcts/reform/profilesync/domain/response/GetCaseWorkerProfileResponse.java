package uk.gov.hmcts.reform.profilesync.domain.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.profilesync.domain.CaseWorkerProfile;

import static java.util.Objects.requireNonNull;

@Getter
@NoArgsConstructor
public class GetCaseWorkerProfileResponse {

    private boolean idamStatus;
    private String email;
    private String firstName;
    private String lastName;
    private String userId;

    public GetCaseWorkerProfileResponse(CaseWorkerProfile caseWorkerProfile) {

        requireNonNull(caseWorkerProfile, "CaseworkerProfile must not be null");
        this.email = caseWorkerProfile.getEmail();
        this.firstName = caseWorkerProfile.getFirstName();
        this.lastName = caseWorkerProfile.getLastName();
        this.idamStatus = caseWorkerProfile.isIdamStatus();
        this.userId = caseWorkerProfile.getUserId();

    }


}
