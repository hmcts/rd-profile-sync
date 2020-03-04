package uk.gov.hmcts.reform.profilesync.constants;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.profilesync.constants.ProfileSyncEnum.BASIC;
import static uk.gov.hmcts.reform.profilesync.constants.ProfileSyncEnum.BEARER;

import org.junit.Test;
import uk.gov.hmcts.reform.profilesync.constants.ProfileSyncEnum;

public class ProfileSyncEnumTest {

    @Test
    public void profileSyncEnumTest() {
        ProfileSyncEnum basic = BASIC;
        ProfileSyncEnum bearer = BEARER;

        assertThat(basic).isEqualTo(BASIC);
        assertThat(bearer).isEqualTo(BEARER);
    }
}
