package uk.gov.hmcts.reform.profilesync.domain.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ErrorResponse {

    private String errorMessage;

    private String errorDescription;

    private String timeStamp;

    public ErrorResponse(String errorMessage, String errorDescription, String timeStamp) {
        this.errorMessage = errorMessage;
        this.errorDescription = errorDescription;
        this.timeStamp = timeStamp;
    }
}