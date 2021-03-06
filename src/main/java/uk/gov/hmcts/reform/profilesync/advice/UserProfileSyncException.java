package uk.gov.hmcts.reform.profilesync.advice;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class UserProfileSyncException extends RuntimeException {

    private final HttpStatus httpStatus;

    private final String errorMessage;

    public UserProfileSyncException(HttpStatus httpStatus, String errorMessage) {
        super(errorMessage);
        this.httpStatus = httpStatus;
        this.errorMessage = errorMessage;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

}
