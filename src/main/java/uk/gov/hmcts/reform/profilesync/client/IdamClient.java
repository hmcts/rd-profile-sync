package uk.gov.hmcts.reform.profilesync.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import feign.Headers;
import feign.Response;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient(name = "idamClient", url = "${auth.idam.client.baseUrl}")
public interface IdamClient {

    @GetMapping(value = "/api/v1/users", consumes = {"application/x-www-form-urlencoded"})
    @Headers("authorization: {authorization}")
    public Response getUserFeed(@RequestHeader("authorization") String authorization, @RequestParam  Map<String, String> params);

    @Data
    class AuthenticateUserResponse {
        @JsonProperty("code")
        private String code;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    class BearerTokenResponse {
        @SerializedName("access_token")
        private String accessToken;
    }

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
