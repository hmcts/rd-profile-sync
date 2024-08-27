package uk.gov.hmcts.reform.profilesync;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class ProfileSyncPostgresqlContainer extends PostgreSQLContainer<ProfileSyncPostgresqlContainer> {
    private static final String IMAGE_VERSION = "postgres:16";

    private ProfileSyncPostgresqlContainer() {
        super(IMAGE_VERSION);
    }

    @Container
    private static final ProfileSyncPostgresqlContainer container = new ProfileSyncPostgresqlContainer();

}
