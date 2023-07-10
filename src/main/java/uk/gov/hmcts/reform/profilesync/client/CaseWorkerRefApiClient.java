package uk.gov.hmcts.reform.profilesync.client;

import feign.Headers;
import feign.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.profilesync.domain.CaseWorkerProfile;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@FeignClient(name = "caseWorkerRefApiClient", url = "${caseworker.api.url}")
public interface CaseWorkerRefApiClient {

    @PutMapping(value = "/refdata/case-worker/users/sync", consumes = {APPLICATION_JSON_VALUE},
            produces = {APPLICATION_JSON_VALUE})
    @Headers({"authorization: {authorization}", "serviceauthorization: {serviceauthorization}"})
    public Response syncCaseWorkerUserStatus(@RequestHeader("authorization") String authorization,
                                             @RequestHeader("serviceauthorization") String serviceAuthorization,
                                             @RequestBody CaseWorkerProfile body);

    @GetMapping(value = "/refdata/case-worker/profile/search-by-id", produces = {APPLICATION_JSON_VALUE})
    @Headers({"authorization: {authorization}", "serviceauthorization: {serviceauthorization}"})
    public Response findCaseWorkerUser(@RequestHeader("authorization") String authorization,
                                       @RequestHeader("serviceauthorization") String serviceAuthorization,
                                       @RequestParam("id")  String userId);
}
