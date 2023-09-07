package com.yolt.providers.knabgroup.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.knabgroup.common.auth.KnabGroupAuthenticationMeans;
import com.yolt.providers.knabgroup.common.auth.KnabGroupAuthenticationServiceV2;
import com.yolt.providers.knabgroup.common.data.KnabGroupFetchDataServiceV2;
import com.yolt.providers.knabgroup.samples.SampleAuthenticationMeans;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Clock;
import java.util.Map;

import static com.yolt.providers.common.versioning.ProviderVersion.VERSION_2;
import static com.yolt.providers.knabgroup.common.auth.KnabGroupAuthenticationMeans.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
public class KnabGroupDataProviderV2Test {

    private static final String REDIRECT_URL = "https://www.redirectUrl.com";
    private static final String STATE = "state";
    private static final String PSU_IP_ADDRESS = "psuIpAddress";

    private static final String IDENTIFIER = "Identifier";
    private static final String IDENTIFIER_DISPLAY_NAME = "IdentifierDisplayName";
    private static final ProviderVersion VERSION = VERSION_2;

    private static final String REDIRECT_URL_POSTED_BACK_FROM_SITE_WITH_ERROR = UriComponentsBuilder.fromHttpUrl(REDIRECT_URL)
            .queryParam("error", "invalid_grant")
            .build()
            .encode()
            .toString();

    private static final Map<String, BasicAuthenticationMean> BASIC_AUTHENTICATION_MEANS = SampleAuthenticationMeans.getSampleAuthenticationMeans();

    private static final Clock clock = Clock.systemUTC();

    private KnabGroupDataProviderV2 dataProvider;

    @Mock
    private KnabGroupAuthenticationServiceV2 authenticationService;
    @Mock
    private KnabGroupFetchDataServiceV2 fetchDataService;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private RestTemplateManager restTemplateManager;

    @Mock
    private Signer signer;

    @BeforeEach
    public void setUp() {
        dataProvider = new KnabGroupDataProviderV2(authenticationService,
                fetchDataService,
                mapper,
                clock,
                IDENTIFIER,
                IDENTIFIER_DISPLAY_NAME,
                VERSION);
    }

    @Test
    public void shouldThrowGetAccessTokenFailedExceptionWhenAuthorizationIsBroken() {
        //given
        UrlCreateAccessMeansRequest request = new UrlCreateAccessMeansRequestBuilder()
                .setAuthenticationMeans(BASIC_AUTHENTICATION_MEANS)
                .setBaseClientRedirectUrl(REDIRECT_URL)
                .setRedirectUrlPostedBackFromSite(REDIRECT_URL_POSTED_BACK_FROM_SITE_WITH_ERROR)
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setState(STATE)
                .setSigner(signer)
                .build();

        //when
        ThrowableAssert.ThrowingCallable createNewAccessMeansCallable = () -> dataProvider.createNewAccessMeans(request);

        //then
        assertThatThrownBy(createNewAccessMeansCallable)
                .isExactlyInstanceOf(GetAccessTokenFailedException.class);
    }

    @Test
    public void shouldReturnProperAuthenticationMeans() {
        //when
        Map<String, TypedAuthenticationMeans> authenticationMeans = dataProvider.getTypedAuthenticationMeans();

        //then
        assertThat(authenticationMeans).hasSize(6);
        assertThat(authenticationMeans).containsOnlyKeys(
                SIGNING_CERTIFICATE_NAME,
                SIGNING_KEY_ID,
                TRANSPORT_CERTIFICATE_NAME,
                TRANSPORT_KEY_ID,
                KnabGroupAuthenticationMeans.CLIENT_ID,
                CLIENT_SECRET);
    }

    @Test
    public void shouldReturnProperProviderIdentifier() {
        //when
        String identifier = dataProvider.getProviderIdentifier();

        //then
        assertThat(identifier).isEqualTo(IDENTIFIER);
    }

    @Test
    public void shouldReturnProperProviderIdentifierDisplayName() {
        //when
        String displayName = dataProvider.getProviderIdentifierDisplayName();

        //then
        assertThat(displayName).isEqualTo(IDENTIFIER_DISPLAY_NAME);
    }

    @Test
    public void shouldReturnProperVersion() {
        //when
        ProviderVersion version = dataProvider.getVersion();

        //then
        assertThat(version).isEqualTo(VERSION);
    }
}