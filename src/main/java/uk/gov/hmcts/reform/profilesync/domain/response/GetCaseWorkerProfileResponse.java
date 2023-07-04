package uk.gov.hmcts.reform.profilesync.domain.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.profilesync.domain.CaseWorkerProfile;

import static java.util.Objects.requireNonNull;

@Getter
@NoArgsConstructor
public class GetCaseWorkerProfileResponse {

    @JsonProperty("suspended")
    private boolean idamStatus;

    @JsonProperty("email_id")
    private String email;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("case_worker_id")
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
