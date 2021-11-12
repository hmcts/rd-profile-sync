package uk.gov.hmcts.reform.profilesync.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.profilesync.service.ProfileSyncEnum.BASIC;
import static uk.gov.hmcts.reform.profilesync.service.ProfileSyncEnum.BEARER;

class ProfileSyncEnumTest {

    @Test
    void test_profileSyncEnum() {

        assertThat(BASIC).isEqualTo(BASIC);
        assertThat(BEARER).isEqualTo(BEARER);
    }
}
