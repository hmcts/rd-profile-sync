package uk.gov.hmcts.reform.profilesync.config;

import static org.assertj.core.api.Assertions.assertThat;

import feign.Logger;
import feign.codec.Encoder;
import org.junit.Test;

public class FeignConfigurationTest {

    private FeignConfiguration feignConfiguration = new FeignConfiguration();

    @Test
    public void test_FeignFormEncoder() {
        Encoder result = feignConfiguration.feignFormEncoder();

        assertThat(result).isNotNull();
    }

    @Test
    public void test_feignLoggerLevel() {
        Logger.Level level = feignConfiguration.feignLoggerLevel();

        assertThat(level.name()).isEqualTo("FULL");
    }
}