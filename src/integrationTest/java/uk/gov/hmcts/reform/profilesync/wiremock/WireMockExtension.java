package uk.gov.hmcts.reform.profilesync.wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.concurrent.TimeUnit;

public class WireMockExtension extends WireMockServer implements BeforeEachCallback, AfterEachCallback {

    public WireMockExtension(int port) {
        super(port);
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        start();
    }

    @Override
    public void afterEach(ExtensionContext context) throws InterruptedException {
        resetAll();
        stop();
        TimeUnit.SECONDS.sleep(1);
    }
}
