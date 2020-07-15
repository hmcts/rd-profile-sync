package uk.gov.hmcts.reform.profilesync.domain.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import uk.gov.hmcts.reform.profilesync.domain.response.ErrorResponse;

public class ErrorResponseTest {

    private String errorMessage = "errorMessage";
    private String errorDescription = "errorDescription";
    private String timeStamp = "timeStamp";

    @Test
    public void test_ErrorResponseTest() {
        ErrorResponse errorResponseTest1 = new ErrorResponse(errorMessage, errorDescription, timeStamp);

        assertThat(errorResponseTest1.getErrorMessage()).isEqualTo(errorMessage);
        assertThat(errorResponseTest1.getErrorDescription()).isEqualTo(errorDescription);
        assertThat(errorResponseTest1.getTimeStamp()).isEqualTo(timeStamp);
    }
}