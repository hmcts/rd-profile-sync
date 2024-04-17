package uk.gov.hmcts.reform.profilesync.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import feign.Response;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.TokenRequest;
import uk.gov.hmcts.reform.profilesync.advice.UserProfileSyncException;
import uk.gov.hmcts.reform.profilesync.client.IdamClient;
import uk.gov.hmcts.reform.profilesync.config.TokenConfigProperties;
import uk.gov.hmcts.reform.profilesync.domain.ProfileSyncAudit;
import uk.gov.hmcts.reform.profilesync.domain.response.OpenIdAccessTokenResponse;
import uk.gov.hmcts.reform.profilesync.service.ProfileSyncService;
import uk.gov.hmcts.reform.profilesync.service.ProfileUpdateService;
import uk.gov.hmcts.reform.profilesync.util.JsonFeignResponseUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
@SuppressWarnings("unchecked")
public class ProfileSyncServiceImpl implements ProfileSyncService {

    @Autowired
    protected IdamClient idamClient;

    @Autowired
    protected AuthTokenGenerator tokenGenerator;

    @Autowired
    protected ProfileUpdateService profileUpdateService;

    @Autowired
    private TokenConfigProperties props;

    @Value("${loggingComponentName}")
    protected String loggingComponentName;

    @Value("${recordsPerPage}")
    protected int recordsPerPage;

    static final String BEARER = "Bearer ";

    private static final String SCOPE = "openid profile roles manage-user create-user search-user";


    public String getBearerToken() throws UserProfileSyncException {
        String authorisation = props.getAuthorization();
        log.info("Authorization <<< >>> {}", authorisation);
        String[] userDetails = authorisation.split(":");
        TokenRequest tokenRequest = new TokenRequest(props.getClientId(),
                props.getClientAuthorization(),
                "password",
                props.getRedirectUri(),
                userDetails[0].trim(),
                userDetails[1].trim(),
                SCOPE, "", "");

        OpenIdAccessTokenResponse openIdTokenResponse = idamClient.getOpenIdToken(tokenRequest);

        if (openIdTokenResponse == null) {
            throw new UserProfileSyncException(HttpStatus.valueOf(500),
                    "Idam Service Failed while bearer token generate");
        }

        return openIdTokenResponse.getAccessToken();
    }

    public String getS2sToken() {
        log.info("generating S2S Token from updateUserProfileFeed method");
        String s2sToken = tokenGenerator.generate();
        if (StringUtils.isNotEmpty(s2sToken)) {
            log.info("The length of S2S token is: {}", s2sToken.length());
        }
        return s2sToken;
    }


    public Set<IdamClient.User> getSyncFeed(String bearerToken, String searchQuery) throws UserProfileSyncException {
        int totalCount = 0;
        int pageNumber = 0;

        Map<String, String> formParams = new HashMap<>();
        formParams.put("query", searchQuery);
        formParams.put("size", String.valueOf(recordsPerPage));
        Set<IdamClient.User> updatedUsers = new HashSet<>();

        do {
            log.info("{}:: Page Number :: " + pageNumber, loggingComponentName);

            formParams.put("page", String.valueOf(pageNumber));

            Response response = idamClient.getUserFeed(bearerToken, formParams);
            logIdamResponse(response);

            ResponseEntity<Object> responseEntity = JsonFeignResponseUtil.toResponseEntity(response,
                    new TypeReference<Set<IdamClient.User>>() {
                    });

            if (response.status() == 200) {

                Set<IdamClient.User> users = (Set<IdamClient.User>) responseEntity.getBody();
                updatedUsers.addAll(users);

                try {
                    List<String> headerCount = responseEntity.getHeaders().get("X-Total-Count");
                    if (headerCount != null && !headerCount.isEmpty()
                            && !headerCount.get(0).isEmpty()) {

                        totalCount = Integer.parseInt(headerCount.get(0));
                        log.info("{}:: Header Records total count from Idam :: " + totalCount, loggingComponentName);
                    }

                } catch (Exception ex) {
                    //There is No header.
                    log.error("{}:: X-Total-Count header not return Idam Search Service::{}", loggingComponentName, ex);
                }
            } else {
                log.error("{}:: Idam Search Service Failed :: ", loggingComponentName);
                throw new UserProfileSyncException(HttpStatus.valueOf(response.status()), "Idam search query failure");

            }
            pageNumber++;

        } while (totalCount > 0 && recordsPerPage * pageNumber < totalCount);
        return updatedUsers;
    }

    private void logIdamResponse(Response response) {
        log.info("Logging Response from IDAM");
        if (response != null) {
            log.info("Response code from idamClient.getUserFeed {}", response.status());
            if (response.status() != 200 && response.body() != null) {
                log.info("Response body from Idam Client ::{}", response.status());
            }
        }
    }

    public ProfileSyncAudit updateUserProfileFeed(String searchQuery, ProfileSyncAudit syncAudit)
            throws UserProfileSyncException {
        log.info("{}:: Inside updateUserProfileFeed ::", loggingComponentName);
        String bearerToken = BEARER + getBearerToken();
        return profileUpdateService.updateUserProfile(searchQuery, bearerToken, getS2sToken(),
                getSyncFeed(bearerToken, searchQuery), syncAudit);
    }
}
