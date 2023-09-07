package com.yolt.providers.fabric.common.onboarding;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.rest.tracing.ExternalTracingUtil;
import com.yolt.providers.fabric.common.auth.FabricGroupAuthenticationMeans;
import com.yolt.providers.fabric.common.beanconfig.FabricGroupProperties;
import com.yolt.providers.fabric.common.http.FabricGroupHttpClientFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@RequiredArgsConstructor
public class FabricGroupOnboardingHttpClient {

    private final RestTemplate restTemplate;
    private static final String X_REQUEST_ID_HEADER = "X-Request-ID";

    public static FabricGroupOnboardingHttpClient createHttpClient(final RestTemplateManager restTemplateManager,
                                                                   final FabricGroupAuthenticationMeans clientConfiguration,
                                                                   final FabricGroupProperties properties) {
        return new FabricGroupOnboardingHttpClient(FabricGroupHttpClientFactory
                .createRestTemplateWithManagedMutualTLSTemplateForOnboarding(restTemplateManager, clientConfiguration, properties.getBaseUrl()));
    }

    public ResponseEntity<JsonNode> createRegistration(final FabricGroupAuthenticationMeans clientConfiguration,
                                                       final FabricGroupProperties properties) {

        final String lastExternalTraceId = ExternalTracingUtil.createLastExternalTraceId();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);
        headers.set(X_REQUEST_ID_HEADER, lastExternalTraceId);

        FabricGroupOnboardingRequest request = FabricGroupOnboardingRequest.builder()
                .aisp(true)
                .piisp(false)
                .pisp(true)
                .build();

        try {
            return ResponseEntity.ok().build();
        } catch (HttpStatusCodeException e) {
            throw new FabricGroupAutoOnBoardingException("Onboarding has failed.");
        }
    }
}
