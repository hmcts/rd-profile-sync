package uk.gov.hmcts.reform.profilesync.service.wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class WireMockExtension extends WireMockServer implements BeforeAllCallback, AfterAllCallback {

    public WireMockExtension(int port) {
        super(port);
    }

    @Override
    public void afterAll(ExtensionContext context) {
        resetAll();
        stop();
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        start();
    }
}
