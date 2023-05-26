package uk.gov.hmcts.reform.profilesync;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(classes = ProfileSyncApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class SpringBootIntTest {

    @LocalServerPort
    protected int port;

}
