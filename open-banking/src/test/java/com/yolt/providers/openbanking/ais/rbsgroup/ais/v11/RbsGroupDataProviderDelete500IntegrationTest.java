package com.yolt.providers.openbanking.ais.rbsgroup.ais.v11;

import com.yolt.providers.common.ais.url.UrlOnUserSiteDeleteRequest;
import com.yolt.providers.common.ais.url.UrlOnUserSiteDeleteRequestBuilder;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.rbsgroup.RbsApp;
import com.yolt.providers.openbanking.ais.rbsgroup.RbsSampleAuthenticationMeansV4;
import com.yolt.providers.openbanking.ais.rbsgroup.common.RbsGroupDataProviderV5;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpServerErrorException;

import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

/**
 * According to previously logged issue we started to receive 500 when checking for validity of consent.
 * This means that there are some server issue on bank side, so we want to retry the deletion in future,
 * thus we throw {@link HttpServerErrorException.InternalServerError})
 * <p>
 * Providers: ALL RBS Group
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = RbsApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("rbsgroup-v5")
@AutoConfigureWireMock(stubs = "classpath:/stubs/rbsgroup/ob_3.1.6/ais/delete_500/", port = 0, httpsPort = 0)
class RbsGroupDataProviderDelete500IntegrationTest {

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
    void shouldThrowExceptionWhenOnDeleteReturns500(RbsGroupDataProviderV5 dataProvider) throws Exception {
        // given
        RestTemplateManagerMock restTemplateManagerMock = new RestTemplateManagerMock(() -> "12345");
        UrlOnUserSiteDeleteRequest urlOnUserSiteDelete = new UrlOnUserSiteDeleteRequestBuilder()
                .setExternalConsentId("650ac35750b8448db81cf77613dd62b5")
                .setSigner(new SignerMock())
                .setRestTemplateManager(restTemplateManagerMock)
                .setAuthenticationMeans(RbsSampleAuthenticationMeansV4.getRbsSampleAuthenticationMeansForAis())
                .build();

        // when
        ThrowableAssert.ThrowingCallable onUserSiteDelete = () -> dataProvider.onUserSiteDelete(urlOnUserSiteDelete);

        // then
        assertThatThrownBy(onUserSiteDelete)
                .isExactlyInstanceOf(HttpServerErrorException.InternalServerError.class);
    }
}
