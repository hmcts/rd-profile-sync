package uk.gov.hmcts.reform.profilesync.config;

import feign.Logger;
import feign.codec.Encoder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FeignConfigurationTest {

    private final FeignConfiguration feignConfiguration = new FeignConfiguration();

    @Test
    void testFeignFormEncoder() {
        Encoder result = feignConfiguration.feignFormEncoder();

        assertThat(result).isNotNull();
    }

    @Test
    void test_feignLoggerLevel() {
        Logger.Level level = feignConfiguration.feignLoggerLevel();

        assertThat(level.name()).isEqualTo("FULL");
    }
}
