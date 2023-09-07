package com.yolt.providers.stet.bnpparibasgroup.ais;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.stet.bnpparibasgroup.BnpParibasGroupDataProvider;
import com.yolt.providers.stet.bnpparibasgroup.BnpParibasGroupTestConfig;
import com.yolt.providers.stet.bnpparibasgroup.bnpparibas.BnpParibasDataProviderV6;
import com.yolt.providers.stet.bnpparibasgroup.common.configuration.BnpParibasGroupSampleAuthenticationMeans;
import com.yolt.providers.stet.bnpparibasgroup.hellobank.HelloBankDataProviderV6;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {BnpParibasGroupTestConfig.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("bnpparibasgroup")
public class BnpParibasGroupCreateAuthMeansCallbackUrlValidationExceptionsTest {

    private static final String STATE = UUID.fromString("e35eaf8f-5e22-411d-88cc-7301a5c72728").toString();
    private static final UUID USER_ID = UUID.fromString("07a540a2-7b91-11e9-8f9e-2a86e4085a59");

    private final BnpParibasGroupSampleAuthenticationMeans sampleAuthenticationMeans = new BnpParibasGroupSampleAuthenticationMeans();
    @Mock
    private RestTemplateManager restTemplateManagerMock;
    @Mock
    Signer signer;

    @Autowired
    private BnpParibasDataProviderV6 bnpParibasDataProvider;

    @Autowired
    private HelloBankDataProviderV6 helloBankDataProvider;

    @Autowired
    @Qualifier("HelloBankStetProperties")
    DefaultProperties helloBankProperties;

    @Autowired
    @Qualifier("BnpParibasStetProperties")
    DefaultProperties bnpParibasProperties;

    @Autowired
    @Qualifier("StetObjectMapper")
    private ObjectMapper objectMapper;

    public Stream<Arguments> getProvidersWithProperties() {
        return Stream.of(
                Arguments.of(bnpParibasDataProvider, bnpParibasProperties),
                Arguments.of(helloBankDataProvider, helloBankProperties)
        );
    }

    @ParameterizedTest
    @MethodSource("getProvidersWithProperties")
    void shouldThrowGetAccessTokenFailedExceptionForErrorCallback(BnpParibasGroupDataProvider dataProvider, DefaultProperties properties) throws IOException, URISyntaxException {
        // given
        String callbackUrl = String.format("http://yolt.com/redirect/bnp-paribas?error=%s&state=%s", "access_denied", STATE);
        UrlCreateAccessMeansRequest createAccessMeansRequest = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(USER_ID)
                .setRedirectUrlPostedBackFromSite(callbackUrl)
                .setAuthenticationMeans(sampleAuthenticationMeans.getBnpSampleAuthenticationMeans())
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManagerMock)
                .setProviderState(BnpParibasGroupSampleAuthenticationMeans.createPreAuthorizedJsonProviderState(objectMapper, properties))
                .build();

        // when
        ThrowableAssert.ThrowingCallable createNewAccessMeansCallable = () -> dataProvider.createNewAccessMeans(createAccessMeansRequest);

        // then
        assertThatThrownBy(createNewAccessMeansCallable).isInstanceOf(GetAccessTokenFailedException.class)
                .withFailMessage("Cannot extract authorization code due to an error in callback URL. Error details: access_denied");
    }

    @ParameterizedTest
    @MethodSource("getProvidersWithProperties")
    void shouldThrowTokenFailedExceptionWhenAuthorizationCodeIsMissing(BnpParibasGroupDataProvider dataProvider, DefaultProperties properties) throws IOException, URISyntaxException {
        // given
        UrlCreateAccessMeansRequest createAccessMeansRequest = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(USER_ID)
                .setRedirectUrlPostedBackFromSite(String.format("http://yolt.com/redirect/bnp-paribas?state%s", STATE))
                .setAuthenticationMeans(sampleAuthenticationMeans.getBnpSampleAuthenticationMeans())
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManagerMock)
                .setProviderState(BnpParibasGroupSampleAuthenticationMeans.createPreAuthorizedJsonProviderState(objectMapper, properties))
                .build();

        // when
        ThrowableAssert.ThrowingCallable createAccessMeansCallable = () -> dataProvider.createNewAccessMeans(createAccessMeansRequest);

        // then
        assertThatThrownBy(createAccessMeansCallable).isInstanceOf(GetAccessTokenFailedException.class)
                .withFailMessage("Cannot extract authorization code due to an error in callback URL. Error details: Authorization code not provided");
    }

}