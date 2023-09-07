package com.yolt.providers.openbanking.ais.rbsgroup.ais.v11;

import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequest;
import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequestBuilder;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.providerinterface.AutoOnboardingProvider;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.rbsgroup.RbsApp;
import com.yolt.providers.openbanking.ais.rbsgroup.RbsSampleAuthenticationMeansV4;
import com.yolt.providers.openbanking.ais.rbsgroup.common.RbsGroupDataProviderV5;
import com.yolt.providers.openbanking.ais.rbsgroup.common.auth.RbsGroupAuthMeansBuilderV4;
import nl.ing.lovebird.providerdomain.TokenScope;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = RbsApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("rbsgroup-v5")
@AutoConfigureWireMock(stubs = "classpath:/stubs/rbsgroup/autoonboarding/v2", port = 0, httpsPort = 0)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RbsGroupAutoonboardingV3IntegrationTest {

    private static RestTemplateManagerMock restTemplateManagerMock;
    private static Signer signer;

    @Autowired
    @Qualifier("CouttsDataProviderV3")
    private RbsGroupDataProviderV5 couttsDataProvider;
    @Autowired
    @Qualifier("NatWestDataProviderV11")
    private RbsGroupDataProviderV5 natwestDataProvider;
    @Autowired
    @Qualifier("NatWestCorporateDataProviderV10")
    private RbsGroupDataProviderV5 natwestCorpoDataProvider;
    @Autowired
    @Qualifier("RoyalBankOfScotlandDataProviderV11")
    private RbsGroupDataProviderV5 rbsDataProvider;
    @Autowired
    @Qualifier("RoyalBankOfScotlandCorporateDataProviderV10")
    private RbsGroupDataProviderV5 rbsCorpoDataProvider;
    @Autowired
    @Qualifier("UlsterBankDataProviderV10")
    private RbsGroupDataProviderV5 ulsterDataProvider;

    @BeforeAll
    static void beforeAll() {
        signer = new SignerMock();
        restTemplateManagerMock = new RestTemplateManagerMock(() -> "54321");
    }

    private Stream<Arguments> getProviders() throws IOException, URISyntaxException {
        Map<String, BasicAuthenticationMean> rbsAuthenticationMeans = RbsSampleAuthenticationMeansV4.getRbsSampleAuthenticationMeansForAis();
        rbsAuthenticationMeans.remove(RbsGroupAuthMeansBuilderV4.CLIENT_ID_NAME);
        return Stream.of(
                Arguments.of(couttsDataProvider, rbsAuthenticationMeans, 9),
                Arguments.of(natwestDataProvider, rbsAuthenticationMeans, 9),
                Arguments.of(rbsDataProvider, rbsAuthenticationMeans, 9),
                Arguments.of(ulsterDataProvider, rbsAuthenticationMeans, 9),
                Arguments.of(rbsCorpoDataProvider, rbsAuthenticationMeans, 9),
                Arguments.of(natwestCorpoDataProvider, rbsAuthenticationMeans, 9)
        );
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldReturnRegistrationObjectWithClientIdWhenThereIsNoClientIdInMeans(AutoOnboardingProvider subject,
                                                                                       Map<String, BasicAuthenticationMean> authMeans,
                                                                                       int expectedSizeOfAuthMeans) {
        //given
        authMeans.remove(RbsGroupAuthMeansBuilderV4.CLIENT_ID_NAME);

        UrlAutoOnboardingRequest autoOnboardingRequest = new UrlAutoOnboardingRequestBuilder()
                .setAuthenticationMeans(authMeans)
                .setRedirectUrls(Collections.singletonList("http://fake-redirect.com"))
                .setRestTemplateManager(restTemplateManagerMock)
                .setSigner(signer)
                .setScopes(Set.of(TokenScope.ACCOUNTS, TokenScope.PAYMENTS))
                .build();


        //when
        Map<String, BasicAuthenticationMean> returnedMeans = subject.autoConfigureMeans(autoOnboardingRequest);

        //then
        assertThat(returnedMeans).hasSize(expectedSizeOfAuthMeans);
        assertThat(returnedMeans).containsEntry(RbsGroupAuthMeansBuilderV4.CLIENT_ID_NAME, new BasicAuthenticationMean(TypedAuthenticationMeans.CLIENT_ID_STRING.getType(), "SOME_FAKE_CLIENT_ID"));
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldReturnRegistrationObjectWithNewClientId(AutoOnboardingProvider subject,
                                                              Map<String, BasicAuthenticationMean> authMeans,
                                                              int expectedSizeOfAuthMeans) {
        authMeans.put(RbsGroupAuthMeansBuilderV4.CLIENT_ID_NAME, new BasicAuthenticationMean(TypedAuthenticationMeans.CLIENT_ID_STRING.getType(), "SOME_FAKE_CLIENT_ID"));
        UrlAutoOnboardingRequest autoOnboardingRequest = new UrlAutoOnboardingRequestBuilder()
                .setAuthenticationMeans(authMeans)
                .setRedirectUrls(Collections.singletonList("http://fake-redirect.com"))
                .setScopes(Set.of(TokenScope.ACCOUNTS, TokenScope.PAYMENTS))
                .setRestTemplateManager(restTemplateManagerMock)
                .setSigner(signer)
                .build();
        //when
        Map<String, BasicAuthenticationMean> returnedMeans = subject.autoConfigureMeans(autoOnboardingRequest);

        //then
        assertThat(returnedMeans).hasSize(expectedSizeOfAuthMeans);
        assertThat(returnedMeans).containsEntry(RbsGroupAuthMeansBuilderV4.CLIENT_ID_NAME, new BasicAuthenticationMean(TypedAuthenticationMeans.CLIENT_ID_STRING.getType(), "SOME_FAKE_CLIENT_ID"));
    }
}
