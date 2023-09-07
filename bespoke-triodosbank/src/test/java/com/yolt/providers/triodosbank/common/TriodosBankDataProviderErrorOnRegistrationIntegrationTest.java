package com.yolt.providers.triodosbank.common;

import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequest;
import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.AutoOnboardingException;
import com.yolt.providers.triodosbank.nl.TriodosBankNLDataProvider;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.*;
import static com.yolt.providers.triodosbank.common.auth.TriodosBankAuthenticationMeans.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/mappings/400_OnBoarding/",
        files = "classpath:/mappings/400_OnBoarding/",
        httpsPort = 0,
        port = 0)
@ActiveProfiles("triodosbank")
class TriodosBankDataProviderErrorOnRegistrationIntegrationTest {

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private RestTemplateManager restTemplateManager;

    @Autowired
    private TriodosBankNLDataProvider dataProvider;

    @Mock
    private Signer signer;

    private Map<String, BasicAuthenticationMean> authenticationMeans;
    private String pemCertificate;

    @BeforeEach
    void initialize() throws IOException {
        Resource resource = resourceLoader.getResource("classpath:certificates/fake-certificate.pem");
        pemCertificate = String.join("\n", Files.readAllLines(resource.getFile().toPath(), UTF_8));

        authenticationMeans = new HashMap<>();
        authenticationMeans.put(TRANSPORT_CERTIFICATE_NAME, new BasicAuthenticationMean(CERTIFICATE_PEM.getType(), pemCertificate));
        authenticationMeans.put(TRANSPORT_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), "2be4d475-f240-42c7-a22c-882566ac0f95"));
        authenticationMeans.put(SIGNING_CERTIFICATE_NAME, new BasicAuthenticationMean(CERTIFICATE_PEM.getType(), pemCertificate));
        authenticationMeans.put(SIGNING_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), "5391cac7-b840-4628-8036-d4998dfb8959"));
        authenticationMeans.put(CLIENT_ID_STRING_NAME, new BasicAuthenticationMean(CLIENT_ID_STRING.getType(), "client-id"));
        authenticationMeans.put(CLIENT_SECRET_STRING_NAME, new BasicAuthenticationMean(CLIENT_SECRET_STRING.getType(), "client-secret"));
    }

    @Test
    void shouldReturnDefaultAndRegisteredAuthenticationMeans() {
        // given
        Map<String, BasicAuthenticationMean> unregisteredAuthenticationMeans = new HashMap<>(authenticationMeans);
        unregisteredAuthenticationMeans.remove(CLIENT_ID_STRING_NAME);
        unregisteredAuthenticationMeans.remove(CLIENT_SECRET_STRING_NAME);

        UrlAutoOnboardingRequest request = new UrlAutoOnboardingRequestBuilder()
                .setAuthenticationMeans(unregisteredAuthenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setSigner(signer)
                .setRedirectUrls(List.of("https://yolt.com/callback", "https://yolt.com/dev"))
                .build();

        // when
        ThrowableAssert.ThrowingCallable onBoardingCallable = () -> dataProvider.autoConfigureMeans(request);

        // then
        assertThatThrownBy(onBoardingCallable)
                .isExactlyInstanceOf(AutoOnboardingException.class);
    }

}
