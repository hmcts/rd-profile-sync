package uk.gov.hmcts.reform.profilesync.helper;

import static java.time.LocalDateTime.now;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import uk.gov.hmcts.reform.profilesync.client.IdamClient;
import uk.gov.hmcts.reform.profilesync.domain.UserProfile;
import uk.gov.hmcts.reform.profilesync.domain.response.GetUserProfileResponse;

public class MockDataProvider {

    private static UserProfile userProfile;
    private static IdamClient.User idamUser;
    private static GetUserProfileResponse getUserProfileResponse;

    public static final String IDAM_ID = "088ce03b-29a1-427a-9e86-af77e4681585";
    public static final String EMAIL = "some.user@hmcts.net";
    public static final String FIRST_NAME = "Albert";
    public static final String LAST_NAME = "Camus";
    public static final LocalDateTime CURRENT_TIME = now();
    public static final String STATUS = "PENDING";
    public static final int IDAM_REGISTRATION_RESPONSE = 201;
    public static final List<String> defaultRoles = new ArrayList<>(
            Arrays.asList("pui-user-manager", "pui-organisation-manager")
    );

    public static final long userProfileId = 4501;


    // OAUTH2 mock data
    public static final String CLIENT_ID = "5489023";
    public static final String CLIENT_SECRET = "dd7f5a7-8866-11r9-gf42-226bf8964f64";
    public static final String REDIRECT_URI = "http://www.myredirectid.com";
    public static final String AUTHORIZATION = "eyjkl902390jf0ksldj03903.dffkljfke932rjf032j02f3";
    public static final String CLIENT_AUTHORIZATION =
            "eyjfddsfsdfsdfdj03903.dffkljfke932rjf032j02f3--fskfljdskls-fdkldskll";

    private MockDataProvider() {

        userProfile = UserProfile.builder()
                .userIdentifier(IDAM_ID)
                .email(EMAIL)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .idamStatus(STATUS)
                .idamRegistrationResponse(IDAM_REGISTRATION_RESPONSE)
                .build();
    }

    public static UserProfile getUserProfile() {
        if (userProfile == null) {
            userProfile = new MockDataProvider().userProfile;//NB You will break the tests if this gets removed
        }
        return userProfile;
    }

    public static IdamClient.User getIdamUser() {
        if (idamUser == null) {
            idamUser = new IdamClient.User();
            idamUser.setActive(true);
            idamUser.setEmail(EMAIL);
            idamUser.setForename(FIRST_NAME);
            idamUser.setId(IDAM_ID);
            idamUser.setLastModified(CURRENT_TIME.toString());
            idamUser.setPending(true);
            idamUser.setRoles(defaultRoles);
            idamUser.setSurname(LAST_NAME);
        }
        return idamUser;
    }

    public static GetUserProfileResponse getGetUserProfileResponse() {
        if (getUserProfileResponse == null) {
            getUserProfileResponse = new GetUserProfileResponse(getUserProfile());
        }
        return getUserProfileResponse;
    }
}
