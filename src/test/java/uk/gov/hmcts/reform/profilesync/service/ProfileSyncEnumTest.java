package uk.gov.hmcts.reform.profilesync.service;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.profilesync.service.ProfileSyncEnum.BASIC;
import static uk.gov.hmcts.reform.profilesync.service.ProfileSyncEnum.BEARER;

import org.junit.Test;

public class ProfileSyncEnumTest {

    @Test
    public void test_profileSyncEnum() {
        ProfileSyncEnum basic = BASIC;
        ProfileSyncEnum bearer = BEARER;

        assertThat(basic).isEqualTo(BASIC);
        assertThat(bearer).isEqualTo(BEARER);
    }
}
