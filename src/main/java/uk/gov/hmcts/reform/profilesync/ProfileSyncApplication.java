package uk.gov.hmcts.reform.profilesync;

import com.microsoft.applicationinsights.attach.ApplicationInsights;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.idam.client.IdamApi;

@EnableJpaAuditing
@EnableJpaRepositories
@SpringBootApplication(scanBasePackages = {"uk.gov.hmcts.reform.profilesync", "uk.gov.hmcts.reform.idam"})
@EnableScheduling
@EnableFeignClients(basePackages = {
    "uk.gov.hmcts.reform.profilesync" }, basePackageClasses = { IdamApi.class, ServiceAuthorisationApi.class })
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
public class ProfileSyncApplication {

    public static void main(final String[] args) {
        ApplicationInsights.attach();
        SpringApplication.run(ProfileSyncApplication.class, args);

    }
}