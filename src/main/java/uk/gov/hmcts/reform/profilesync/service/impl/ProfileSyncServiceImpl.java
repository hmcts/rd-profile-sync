package uk.gov.hmcts.reform.profilesync.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import feign.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.profilesync.client.IdamClient;
import uk.gov.hmcts.reform.profilesync.config.TokenConfigProperties;
import uk.gov.hmcts.reform.profilesync.domain.ErrorResponse;
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

    public String authorize() {

        Map<String, String> formParams = new HashMap<>();
        formParams.put("client_id", props.getClientId());
        formParams.put("redirect_uri", props.getRedirectUri());
        formParams.put("response_type", "code");
        formParams.put("scope", "openid profile roles create-user manage-user search-user");

        IdamClient.AuthenticateUserResponse response = idamClient.authorize(BASIC + props.getAuthorization(), formParams, "");

        return response.getCode();
    }

    public String getBearerToken() {

        Map<String, String> formParams = new HashMap<>();
        formParams.put("client_id", props.getClientId());
        formParams.put("redirect_uri", props.getRedirectUri());
        formParams.put("code", authorize());
        formParams.put("grant_type", "authorization_code");

        IdamClient.TokenExchangeResponse response = idamClient.getToken(BASIC + props.getClientAuthorization(), formParams, "");

        return response.getAccessToken();
    }

    public String getS2sToken() {
        return tokenGenerator.generate();
    }


    public List<IdamClient.User> getSyncFeed(String bearerToken, String searchQuery) throws UserProfileSyncException {
        log.info("Inside getSyncFeed");

        Map<String, String> formParams = new HashMap<>();
        formParams.put("query", searchQuery);

        List<IdamClient.User> updatedUserList = new ArrayList<>();
        int totalCount = 0;
        int counter = 0;
        int recordsPerPage = 20;

        do {
            formParams.put("page", String.valueOf(counter));
            Response response  = idamClient.getUserFeed(bearerToken, formParams);
            ResponseEntity responseEntity = JsonFeignResponseHelper.toResponseEntity(response, new TypeReference<List<IdamClient.User>>() { });
            Class clazz = response.status() > 200 ? ErrorResponse.class : IdamClient.User.class;

            if (response.status() == 200) {

                List<IdamClient.User> users =  (List<IdamClient.User>) responseEntity.getBody();
                log.info("Number Of User Records Found in IDAM ::" + users);
                updatedUserList.addAll(users);

                try {
                    totalCount = Integer.parseInt(responseEntity.getHeaders().get("X-Total-Count").get(0));
                    log.info("Header Records count from Idam ::" + totalCount);
                } catch (Exception ex) {
                    //There is No header.
                    log.error("X-Total-Count header not return Idam Search Service", ex);
                }
            } else {

                log.error("Idam Search Service Failed :");
                throw new UserProfileSyncException(HttpStatus.valueOf(response.status()), "Idam search query failure");

            }
            counter++;

        } while (totalCount > 0 && (recordsPerPage * counter) < totalCount);

        return updatedUserList;
    }

    public void updateUserProfileFeed(String searchQuery) throws UserProfileSyncException {
        log.info("Inside updateUserProfileFeed");
        String bearerToken = BEARER + getBearerToken();
        profileUpdateService.updateUserProfile(searchQuery, bearerToken, getS2sToken(), getSyncFeed(bearerToken, searchQuery));
        log.info("After updateUserProfileFeed");
    }
}
