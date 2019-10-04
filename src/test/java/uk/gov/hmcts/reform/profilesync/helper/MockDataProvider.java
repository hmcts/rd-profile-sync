package uk.gov.hmcts.reform.profilesync.helper;

import static java.time.LocalDateTime.now;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import uk.gov.hmcts.reform.profilesync.client.IdamClient;
import uk.gov.hmcts.reform.profilesync.domain.GetUserProfileResponse;
import uk.gov.hmcts.reform.profilesync.domain.UserProfile;

public class MockDataProvider {

    private static UserProfile userProfile;
    private static IdamClient.User idamUser;
    private static GetUserProfileResponse getUserProfileResponse;

    public static final String idamId = "088ce03b-29a1-427a-9e86-af77e4681585";
    public static final String email = "some.user@hmcts.net";
    public static final String firstName = "Albert";
    public static final String lastName = "Camus";
    public static final boolean emailCommsConsent = true;
    public static final LocalDateTime currentTime = now();
    public static final boolean postalCommsConsent = false;
    public static final String status = "PENDING";
    public static final int idamRegistrationResponse = 201;
    public static final List<String> defaultRoles = new ArrayList<>(
            Arrays.asList("pui-user-manager", "pui-organisation-manager")
    );

    public static final long userProfileId = 4501;


    // OAUTH2 mock data
    public static final String clientId = "5489023";
    public static final String clientSecret = "dd7f5a7-8866-11r9-gf42-226bf8964f64";
    public static final String redirectUri = "http://www.myredirectid.com";
    public static final String authorization = "eyjkl902390jf0ksldj03903.dffkljfke932rjf032j02f3";
    public static final String clientAuthorization = "eyjfddsfsdfsdfdj03903.dffkljfke932rjf032j02f3--fskfljdskls-fdkldskll";

    private MockDataProvider() {

        userProfile = UserProfile.builder()
                .idamId(idamId)
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .idamStatus(status)
                .idamRegistrationResponse(idamRegistrationResponse)
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
            idamUser.setEmail(email);
            idamUser.setForename(firstName);
            idamUser.setId(idamId);
            idamUser.setLastModified(currentTime.toString());
            idamUser.setPending(true);
            idamUser.setRoles(defaultRoles);
            idamUser.setSurname(lastName);
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
