package uk.gov.hmcts.reform.profilesync.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CaseWorkerProfile {


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
}
