package uk.gov.hmcts.reform.profilesync.domain.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import uk.gov.hmcts.reform.profilesync.domain.response.ErrorResponse;

public class ErrorResponseTest {

    @Test
    public void testErrorResponseTest() {

        ErrorResponse errorResponseTest1 = new ErrorResponse("errorMessage", "errorDescription", "timeStamp");

        assertThat(errorResponseTest1.getErrorMessage()).isEqualTo("errorMessage");
        assertThat(errorResponseTest1.getErrorDescription()).isEqualTo("errorDescription");
        assertThat(errorResponseTest1.getTimeStamp()).isEqualTo("timeStamp");
    }
}