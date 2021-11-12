package uk.gov.hmcts.reform.profilesync.advice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(MockitoExtension.class)
class UserProfileSyncExceptionTest {

    @Test
    void test_create_exception_correctly() {
        String message = "this-is-a-test-message";
        UserProfileSyncException exception = new UserProfileSyncException(HttpStatus.NOT_FOUND, message);

        assertThat(exception)
                .hasMessage(message)
                .isInstanceOf(RuntimeException.class);
        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getErrorMessage()).isEqualTo("this-is-a-test-message");
    }
}
