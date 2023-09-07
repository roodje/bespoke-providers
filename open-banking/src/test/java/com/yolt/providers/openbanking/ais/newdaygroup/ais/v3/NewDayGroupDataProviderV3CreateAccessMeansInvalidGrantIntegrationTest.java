package com.yolt.providers.openbanking.ais.newdaygroup.ais.v3;

import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequestBuilder;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.GenericBaseDataProvider;
import com.yolt.providers.openbanking.ais.newdaygroup.NewDayGroupApp;
import com.yolt.providers.openbanking.ais.newdaygroup.amazoncreditcard.AmazonCreditCardDataProviderV3;
import org.assertj.core.api.AssertionsForClassTypes;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.stream.Stream;

/**
 * This test contains handling of invalid grant error in redirect_url query param during access means creation
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {NewDayGroupApp.class,

        OpenbankingConfiguration.class},
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("newdaygroup-v1")
public class NewDayGroupDataProviderV3CreateAccessMeansInvalidGrantIntegrationTest {

    @Autowired
    @Qualifier("AmazonCreditCardDataProviderV3")
    private AmazonCreditCardDataProviderV3 amazonDataProvider;

    @Autowired
    @Qualifier("AquaCreditCardDataProviderV3")
    private GenericBaseDataProvider aquaDataProvider;

    @Autowired
    @Qualifier("ArgosDataProviderV3")
    private GenericBaseDataProvider argosDataProvider;

    @Autowired
    @Qualifier("HouseOfFaserDataProviderV3")
    private GenericBaseDataProvider fraserDataProvider;

    @Autowired
    @Qualifier("MarblesDataProviderV3")
    private GenericBaseDataProvider marblesDataProvider;

    @Autowired
    @Qualifier("DebenhamsDataProviderV3")
    private GenericBaseDataProvider debenhamsDataProvider;

    public Stream<UrlDataProvider> getNewDayDataProviders() {
        return Stream.of(amazonDataProvider,
                aquaDataProvider,
                argosDataProvider,
                fraserDataProvider,
                marblesDataProvider,
                debenhamsDataProvider);
    }

    @ParameterizedTest
    @MethodSource("getNewDayDataProviders")
    public void shouldThrowGetAccessTokenFailedExceptionWhenErrorIsInvalidGrant(UrlDataProvider dataProvider) {
        // given
        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setRedirectUrlPostedBackFromSite("http://example.com?error=invalid_grant")
                .build();

        // when
        ThrowableAssert.ThrowingCallable createNewAccessMeansCallable =
                () -> dataProvider.createNewAccessMeans(urlCreateAccessMeans);

        // then
        AssertionsForClassTypes.assertThatThrownBy(createNewAccessMeansCallable)
                .isExactlyInstanceOf(GetAccessTokenFailedException.class);
    }
}