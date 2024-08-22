package uk.gov.hmcts.reform.profilesync.util;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;

//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.AUTO_CONFIGURED)
@Testcontainers
//@Configuration
public class ProfileSyncPostgresqlContainer extends PostgreSQLContainer<ProfileSyncPostgresqlContainer> {
    private static final String IMAGE_VERSION = "postgres:11.1";

    private ProfileSyncPostgresqlContainer() {
        super(IMAGE_VERSION);
    }

    @Container
    private static final ProfileSyncPostgresqlContainer container = new ProfileSyncPostgresqlContainer();

//    @Bean
//    public DataSource dataSource() {
//        return container.
//    }

}
