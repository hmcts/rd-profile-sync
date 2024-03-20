package uk.gov.hmcts.reform.profilesync.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import feign.Headers;
import feign.Response;
import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.idam.client.models.TokenRequest;
import uk.gov.hmcts.reform.profilesync.domain.response.OpenIdAccessTokenResponse;

import java.util.List;
import java.util.Map;


@FeignClient(name = "idamClient", url = "${idam.api.url}")
public interface IdamClient {

    @GetMapping(value = "/api/v1/users", consumes = {"application/x-www-form-urlencoded"})
    @Headers("authorization: {authorization}")
    public Response getUserFeed(@RequestHeader("authorization") String authorization,
                                @RequestParam  Map<String, String> params);

    @PostMapping(value = "/o/token", consumes = {"application/x-www-form-urlencoded"})
    OpenIdAccessTokenResponse getOpenIdToken(@RequestBody TokenRequest tokenRequest);

    @Data
    class User {
        @JsonProperty("active")
        private boolean active;

        @JsonProperty("email")
        private String email;

        @JsonProperty("forename")
        private String forename;

        @JsonProperty("id")
        private String id;

        @JsonProperty("lastModified")
        private String lastModified;

        @JsonProperty("pending")
        private boolean pending;

        @JsonProperty("roles")
        private List<String> roles;

        @JsonProperty("surname")
        private String surname;
    }
}
