package com.yolt.providers.openbanking.ais.lloydsbankinggroup.ais;

import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequestBuilder;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.GenericBaseDataProvider;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.lloydsbankinggroup.LloydsGroupApp;
import com.yolt.providers.openbanking.ais.lloydsbankinggroup.LloydsSampleTypedAuthenticationMeans;
import com.yolt.providers.openbanking.ais.utils.OpenBankingTestObjectMapper;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

/**
 * This test contains case when create access means method was called with wrong parameter returned from consent redirect url.
 * For such case we want to throw {@link GetAccessTokenFailedException})
 * <p>
 * Disclaimer: most providers in LBG group are the same from code and stubs perspective (then only difference is configuration)
 * Due to that fact this test class is parametrised, so all providers in group are tested.
 * <p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {LloydsGroupApp.class, OpenbankingConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("lloydsgroup")
public class LloydsGroupDataProviderAccessMeansErrorsIntegrationTest {

    private RestTemplateManagerMock restTemplateManagerMock;

    @Autowired
    @Qualifier("BankOfScotlandDataProviderV10")
    private GenericBaseDataProvider bankOfScotlandDataProviderV10;
    @Autowired
    @Qualifier("BankOfScotlandCorpoDataProviderV8")
    private GenericBaseDataProvider bankOfScotlandCorpoDataProviderV8;
    @Autowired
    @Qualifier("HalifaxDataProviderV10")
    private GenericBaseDataProvider halifaxDataProviderV10;
    @Autowired
    @Qualifier("LloydsBankDataProviderV10")
    private GenericBaseDataProvider lloydsBankDataProviderV10;
    @Autowired
    @Qualifier("LloydsBankCorpoDataProviderV8")
    private GenericBaseDataProvider lloydsBankCorpoDataProviderV8;
    @Autowired
    @Qualifier("MbnaCreditCardDataProviderV6")
    private GenericBaseDataProvider mbnaCreditCardDataProviderV6;

    @Mock
    private Signer signer;

    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @BeforeAll
    public void beforeAll() throws IOException, URISyntaxException {
        authenticationMeans = new LloydsSampleTypedAuthenticationMeans().getAuthenticationMeans();
        restTemplateManagerMock = new RestTemplateManagerMock(() -> null);
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    public void shouldThrowGetAccessTokenFailedExceptionWhenCreateNewAccessMeansWithErrorInQueryParameters(UrlDataProvider dataProvider) {
        // given
        String redirectUrl = "https://www.yolt.com/callback/68eef1a1-0b13-4d4b-9cc2-09a8b2604ca0?error=invalid_grant";
        UUID userId = UUID.randomUUID();
        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(userId)
                .setRedirectUrlPostedBackFromSite(redirectUrl)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManagerMock)
                .build();

        // when
        ThrowableAssert.ThrowingCallable fetchDataCallable = () -> dataProvider.createNewAccessMeans(urlCreateAccessMeans);

        // then
        assertThatThrownBy(fetchDataCallable).isExactlyInstanceOf(GetAccessTokenFailedException.class);
    }

    @Test
    public void shouldDeserializeTokenWithUnknownFields() throws IOException {
        // given
        String expectedAccessToken = "at12345";
        String expectedExpireTime = "2018-01-11T12:13:14.123Z";
        String input = String.format(
                "{\"unknownField\": null, \"unknownField2\": null, \"accessToken\": \"%s\", \"expireTime\": \"%s\"}",
                expectedAccessToken,
                expectedExpireTime);

        // when
        AccessMeans output = OpenBankingTestObjectMapper.INSTANCE.readValue(input, AccessMeans.class);

        // then
        assertThat(output.getAccessToken()).isEqualTo(expectedAccessToken);
        assertThat(output.getExpireTime()).isEqualTo(Date.from(Instant.parse(expectedExpireTime)));
    }

    private Stream<UrlDataProvider> getDataProviders() {
        return Stream.of(
                bankOfScotlandDataProviderV10, bankOfScotlandCorpoDataProviderV8,
                halifaxDataProviderV10, lloydsBankDataProviderV10,
                lloydsBankCorpoDataProviderV8, mbnaCreditCardDataProviderV6);
    }
}