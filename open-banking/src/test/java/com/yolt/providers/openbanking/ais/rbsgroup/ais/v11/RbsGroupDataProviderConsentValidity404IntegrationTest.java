package com.yolt.providers.openbanking.ais.rbsgroup.ais.v11;

import com.yolt.providers.common.ais.url.UrlGetLoginRequest;
import com.yolt.providers.common.ais.url.UrlGetLoginRequestBuilder;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.rbsgroup.RbsApp;
import com.yolt.providers.openbanking.ais.rbsgroup.RbsSampleAuthenticationMeansV4;
import com.yolt.providers.openbanking.ais.rbsgroup.common.RbsGroupDataProviderV5;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * According to previously logged issue we started to receive 404 when checking for validity of consent.
 * In such situation we should create a new consent.
 * <p>
 * Providers: ALL RBS Group
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = RbsApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("rbsgroup-v5")
@AutoConfigureWireMock(stubs = "classpath:/stubs/rbsgroup/ob_3.1.6/ais/consent_validity_404/", port = 0, httpsPort = 0)
class RbsGroupDataProviderConsentValidity404IntegrationTest {

    private static final String EXTERNAL_CONSENT_ID = "650ac35750b8448db81cf77613dd62b5";

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

    private Stream<RbsGroupDataProviderV5> getProviders() {
        return Stream.of(natwestDataProvider, natwestCorpoDataProvider, rbsDataProvider, rbsCorpoDataProvider, ulsterDataProvider, couttsDataProvider);
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldCreateNewConsentWithExternalConsentNotFound(RbsGroupDataProviderV5 dataProvider) throws Exception {
        // given
        String existingExternalConsentId = "existing-external-consent-id-404";
        RestTemplateManagerMock restTemplateManagerMock = new RestTemplateManagerMock(() -> "12345");
        UrlGetLoginRequest urlGetLogin = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl("http://yolt.com/identifier").setState(UUID.randomUUID().toString())
                .setExternalConsentId(existingExternalConsentId)
                .setRestTemplateManager(restTemplateManagerMock)
                .setAuthenticationMeans(RbsSampleAuthenticationMeansV4.getRbsSampleAuthenticationMeansForAis())
                .setSigner(new SignerMock())
                .build();

        // when
        RedirectStep loginInfo = (RedirectStep) dataProvider.getLoginInfo(urlGetLogin);

        // then
        assertThat(loginInfo.getExternalConsentId()).isEqualTo(EXTERNAL_CONSENT_ID);
    }
}
