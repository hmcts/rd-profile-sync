package uk.gov.hmcts.reform.profilesync.service.impl;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.microsoft.applicationinsights.core.dependencies.google.gson.Gson;
import feign.Response;

import io.restassured.RestAssured;
import java.util.*;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.profilesync.client.IdamClient;
import uk.gov.hmcts.reform.profilesync.config.TokenConfigProperties;
import uk.gov.hmcts.reform.profilesync.domain.UserProfileSyncException;
import uk.gov.hmcts.reform.profilesync.repository.SyncJobRepository;
import uk.gov.hmcts.reform.profilesync.service.ProfileSyncService;
import uk.gov.hmcts.reform.profilesync.service.ProfileUpdateService;
import uk.gov.hmcts.reform.profilesync.util.JsonFeignResponseHelper;

@Service
@AllArgsConstructor
@Slf4j
@SuppressWarnings("unchecked")
public class ProfileSyncServiceImpl implements ProfileSyncService {

    @Autowired
    protected final IdamClient idamClient;

    @Autowired
    protected final AuthTokenGenerator tokenGenerator;

    @Autowired
    protected final ProfileUpdateService profileUpdateService;

    @Autowired
    private final TokenConfigProperties props;

    @Autowired
    private final SyncJobRepository syncJobRepository;

    static final String BASIC = "Basic ";
    static final String BEARER = "Bearer ";

    public String getBearerToken() {

        byte[] base64UserDetails = Base64.getDecoder().decode(props.getAuthorization());
        Map<String, String> formParams = new HashMap<>();
        formParams.put("grant_type", "password");
        String[] userDetails = new String(base64UserDetails).split(":");
        formParams.put("username", userDetails[0].trim());
        formParams.put("password", userDetails[1].trim());
        formParams.put("client_id", props.getClientId());
        byte[] base64ClientAuth = Base64.getDecoder().decode(props.getClientAuthorization());
        String[] clientAuth = new String(base64ClientAuth).split(":");
        formParams.put("client_secret", clientAuth[1]);
        formParams.put("redirect_uri", props.getRedirectUri());
        formParams.put("scope", "openid profile roles manage-user create-user search-user");

        io.restassured.response.Response openIdTokenResponse = RestAssured
                .given()
                .relaxedHTTPSValidation()
                .baseUri(props.getUrl())
                .header(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE)
                .params(formParams)
                .post("/o/token")
                .andReturn();

        IdamClient.BearerTokenResponse accessTokenResponse = new Gson().fromJson(openIdTokenResponse.getBody().asString(), IdamClient.BearerTokenResponse.class);

        return accessTokenResponse.getAccessToken();
    }

    public String getS2sToken() {
        return tokenGenerator.generate();
    }


    public List<IdamClient.User> getSyncFeed(String bearerToken, String searchQuery) {
        log.info("Inside getSyncFeed");

        Map<String, String> formParams = new HashMap<>();
        formParams.put("query", searchQuery);

        List<IdamClient.User> updatedUserList = new ArrayList<>();
        int totalCount = 0;
        int counter = 0;
        int recordsPerPage = 20;

        do {
            formParams.put("page", String.valueOf(counter));
            Response response = idamClient.getUserFeed(bearerToken, formParams);
            ResponseEntity responseEntity = JsonFeignResponseHelper.toResponseEntity(response, new TypeReference<List<IdamClient.User>>() {
            });

            if (response.status() < 300 && responseEntity.getStatusCode().is2xxSuccessful()) {

                List<IdamClient.User> users = (List<IdamClient.User>) responseEntity.getBody();
                log.info("Number Of User Records Found in IDAM ::" + users);
                updatedUserList.addAll(users);

                try {
                    totalCount = Integer.parseInt(responseEntity.getHeaders().get("X-Total-Count").get(0));
                    log.info("Header Records count from Idam ::" + totalCount);
                } catch (Exception ex) {
                    //There is No header.
                }
            }
            counter++;

        } while (totalCount > 0 && recordsPerPage * counter < totalCount);

        return updatedUserList;
    }

    public void updateUserProfileFeed(String searchQuery) throws UserProfileSyncException {
        log.info("Inside updateUserProfileFeed");
        String bearerToken = BEARER + getBearerToken();
        profileUpdateService.updateUserProfile(searchQuery, bearerToken, getS2sToken(), getSyncFeed(bearerToken, searchQuery));
        log.info("After updateUserProfileFeed");
    }
}
