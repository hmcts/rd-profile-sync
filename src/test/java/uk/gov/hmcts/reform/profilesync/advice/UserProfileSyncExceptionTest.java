package uk.gov.hmcts.reform.profilesync.advice;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.profilesync.advice.UserProfileSyncException;


@RunWith(MockitoJUnitRunner.class)
public class UserProfileSyncExceptionTest {

    @Test
    public void test_create_exception_correctly() {
        String message = "this-is-a-test-message";
        UserProfileSyncException exception = new UserProfileSyncException(HttpStatus.NOT_FOUND, message);

        assertThat(exception).hasMessage(message);
        assertThat(exception).isInstanceOf(RuntimeException.class);
        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getErrorMessage()).isEqualTo("this-is-a-test-message");
    }
}
