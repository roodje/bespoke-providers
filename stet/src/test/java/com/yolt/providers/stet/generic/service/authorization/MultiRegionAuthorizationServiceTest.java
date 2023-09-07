package com.yolt.providers.stet.generic.service.authorization;

import com.yolt.providers.common.ais.form.EncryptionDetails;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.FormStep;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.domain.dynamic.step.Step;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.domain.AuthorizationRedirect;
import com.yolt.providers.stet.generic.domain.DataProviderState;
import com.yolt.providers.stet.generic.domain.Region;
import com.yolt.providers.stet.generic.domain.Scope;
import com.yolt.providers.stet.generic.dto.TestTokenResponseDTO;
import com.yolt.providers.stet.generic.dto.token.TokenResponseDTO;
import com.yolt.providers.stet.generic.mapper.DateTimeSupplier;
import com.yolt.providers.stet.generic.mapper.providerstate.ProviderStateMapper;
import com.yolt.providers.stet.generic.service.authorization.refresh.RefreshTokenStrategy;
import com.yolt.providers.stet.generic.service.authorization.request.AccessMeansOrStepRequest;
import com.yolt.providers.stet.generic.service.authorization.request.AccessMeansRequest;
import com.yolt.providers.stet.generic.service.authorization.request.AccessTokenRequest;
import com.yolt.providers.stet.generic.service.authorization.request.StepRequest;
import com.yolt.providers.stet.generic.service.authorization.rest.AuthorizationRestClient;
import com.yolt.providers.stet.generic.service.authorization.tool.AuthorizationCodeExtractor;
import com.yolt.providers.stet.generic.service.authorization.tool.AuthorizationRedirectUrlSupplier;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import nl.ing.lovebird.providershared.form.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Date;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.yolt.providers.stet.generic.service.authorization.MultiRegionAuthorizationService.REGION_FIELD_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MultiRegionAuthorizationServiceTest {

    private MultiRegionAuthorizationService authorizationService;

    @Mock
    private RefreshTokenStrategy refreshTokenStrategy;

    @Mock
    private DefaultProperties properties;

    @Mock
    private AuthorizationRedirectUrlSupplier authorizationRedirectUrlSupplier;

    @Mock
    private ProviderStateMapper providerStateMapper;

    @Mock
    private AuthorizationCodeExtractor authorizationCodeExtractor;

    @Mock
    private AuthorizationRestClient authorizationRestClient;

    @Mock
    private HttpClient httpClient;

    @Mock
    private Signer signer;

    @Captor
    private ArgumentCaptor<DataProviderState> providerStateArgumentCaptor;

    @Captor
    private ArgumentCaptor<AccessTokenRequest> accessTokenRequestDTOArgumentCaptor;

    @Captor
    private ArgumentCaptor<StepRequest> stepRequestArgumentCaptor;

    private final Clock clock = Clock.fixed(Instant.parse("2020-01-15T12:00:00.00Z"), ZoneId.systemDefault());

    @BeforeEach
    void beforeEach() {
        authorizationService = new MultiRegionAuthorizationService(
                refreshTokenStrategy,
                authorizationRestClient,
                providerStateMapper,
                Scope.AISP,
                properties,
                authorizationCodeExtractor,
                authorizationRedirectUrlSupplier,
                new DateTimeSupplier(clock));
    }

    @Test
    void shouldReturnFormStepForGetStepWhenCorrectDataProvided() {
        // given
        String jsonProviderState = "providerState";
        StepRequest stepRequest = StepRequest.baseStepRequest(DefaultAuthenticationMeans.builder().build(), "", "");
        List<Region> availableRegions = prepareAvailableRegions();

        when(properties.getRegions())
                .thenReturn(availableRegions);
        when(properties.getFormStepExpiryDurationMillis())
                .thenReturn(10000L);
        when(providerStateMapper.mapToJson(any(DataProviderState.class)))
                .thenReturn(jsonProviderState);

        // when
        Step result = authorizationService.getStep(stepRequest);

        // then
        assertThat(result).isInstanceOf(FormStep.class);
        FormStep formStep = (FormStep) result;
        assertThat(formStep.getEncryptionDetails())
                .extracting(EncryptionDetails::getJweDetails)
                        .isNull();

        assertThat(formStep.getTimeoutTime()).isEqualTo(Instant.now(clock).plus(Duration.ofMillis(10000L)));
        assertThat(formStep.getProviderState()).isEqualTo(jsonProviderState);
        Form form = formStep.getForm();
        assertThat(form.getExplanationField()).isNull();
        assertThat(form.getHiddenComponents()).isNull();
        assertThat(form.getFormComponents()).hasSize(1);
        FormComponent formComponent = form.getFormComponents().get(0);
        assertThat(formComponent).isInstanceOf(SelectField.class);
        SelectField selectField = (SelectField) formComponent;
        assertThat(selectField).extracting(Field::getId, Field::getDisplayName, SelectField::getLength, SelectField::getMaxLength, Field::isOptional, Field::isPersist)
                .contains(REGION_FIELD_ID, MultiRegionAuthorizationService.REGION_FIELD_DISPLAY_NAME, 0, 0, false, true);
        assertThat(selectField.getSelectOptionValues()).extracting(SelectOptionValue::getValue, SelectOptionValue::getDisplayName)
                .containsExactlyInAnyOrder(tuple("REGION1", "Region 1"), tuple("REGION2", "Region 2"));
        verify(properties).getRegions();
        verify(providerStateMapper).mapToJson(providerStateArgumentCaptor.capture());
        DataProviderState capturedProviderState = providerStateArgumentCaptor.getValue();
        assertThat(capturedProviderState).extracting(DataProviderState::getRegion, DataProviderState::getAccessToken, DataProviderState::getRefreshToken, DataProviderState::getCodeVerifier)
                .contains(null, null, null, null);
    }

    @Test
    void shouldReturnWrapperWithRedirectStepForCreateAccessMeansOrGetStepWhenFilledInFormProvided() throws TokenInvalidException {
        // given
        String authorizationUrl = "https://example/authorize";
        Region region = createRegion(1);
        FilledInUserSiteFormValues formValues = createFilledInUserSiteFormValues(region);
        AccessMeansOrStepRequest accessMeansOrStepRequest = createAccessMeansOrStepRequest(null, formValues);

        when(properties.getRegionByCode(anyString()))
                .thenReturn(region);
        when(authorizationRedirectUrlSupplier.createAuthorizationRedirectUrl(anyString(), any(Scope.class), any(StepRequest.class)))
                .thenReturn(AuthorizationRedirect.create(authorizationUrl));
        when(providerStateMapper.mapToJson(any(DataProviderState.class)))
                .thenReturn(accessMeansOrStepRequest.getProviderState());

        // when
        AccessMeansOrStepDTO result = authorizationService.createAccessMeansOrGetStep(httpClient, accessMeansOrStepRequest);

        // then
        assertThat(result.getAccessMeans()).isNull();
        assertThat(result.getStep()).isInstanceOf(RedirectStep.class);

        RedirectStep redirectStep = (RedirectStep) result.getStep();
        assertThat(redirectStep.getRedirectUrl()).isEqualTo(authorizationUrl);
        assertThat(redirectStep.getExternalConsentId()).isNull();
        assertThat(redirectStep.getProviderState()).isEqualTo(accessMeansOrStepRequest.getProviderState());

        verify(properties).getRegionByCode(region.getCode());
        verify(authorizationRedirectUrlSupplier).createAuthorizationRedirectUrl(eq(region.getAuthUrl()), eq(Scope.AISP), stepRequestArgumentCaptor.capture());
        verify(providerStateMapper).mapToJson(providerStateArgumentCaptor.capture());

        StepRequest stepRequest = stepRequestArgumentCaptor.getValue();
        assertThat(stepRequest.getAuthMeans()).isEqualTo(accessMeansOrStepRequest.getAuthMeans());
        assertThat(stepRequest.getRegionCode()).isEqualTo(accessMeansOrStepRequest.getFilledInUserSiteFormValues().get(REGION_FIELD_ID));
        assertThat(stepRequest.getState()).isEqualTo(accessMeansOrStepRequest.getState());
        assertThat(stepRequest.getBaseClientRedirectUrl()).isEqualTo(accessMeansOrStepRequest.getBaseClientRedirectUrl());

        DataProviderState capturedProviderState = providerStateArgumentCaptor.getValue();
        assertThat(capturedProviderState.getRegion()).isEqualTo(region);
        assertThat(capturedProviderState.getCodeVerifier()).isNull();
        assertThat(capturedProviderState.getAccessToken()).isNull();
        assertThat(capturedProviderState.getAccessToken()).isNull();
    }

    private FilledInUserSiteFormValues createFilledInUserSiteFormValues(Region region) {
        FilledInUserSiteFormValues formValues = new FilledInUserSiteFormValues();
        formValues.add(REGION_FIELD_ID, region.getCode());
        return formValues;
    }

    @Test
    void shouldReturnWrapperWithAccessMeansForCreateAccessMeansOrGetStepWhenRedirectUrlPostedBackFromSiteProvided() throws TokenInvalidException {
        // given
        UUID userId = UUID.randomUUID();
        AccessMeansOrStepRequest accessMeansOrStepRequest = new AccessMeansOrStepRequest(
                "fakeProviderState",
                createDefaultAuthenticationMeans(),
                "someRedirectUrlPostedBack",
                "someBaseClientRedirectUrl",
                userId,
                "fakeState",
                null,
                signer);

        Region region = createRegion(1);

        DataProviderState preAuthorizedProviderState = DataProviderState.preAuthorizedProviderState(region, AuthorizationRedirect.create("someRedirectUrlPostedBack"));
        when(providerStateMapper.mapToDataProviderState(anyString()))
                .thenReturn(preAuthorizedProviderState);
        when(authorizationCodeExtractor.extractAuthorizationCode(anyString()))
                .thenReturn("authCode");
        TokenResponseDTO tokenResponseDTO = createTokenResponseDTO();
        when(authorizationRestClient.getAccessToken(any(HttpClient.class), any(AccessTokenRequest.class), any(DefaultAuthenticationMeans.class), any()))
                .thenReturn(tokenResponseDTO);
        when(providerStateMapper.mapToJson(any(DataProviderState.class)))
                .thenReturn("providerState");

        // when
        AccessMeansOrStepDTO result = authorizationService.createAccessMeansOrGetStep(httpClient, accessMeansOrStepRequest);

        // then
        assertThat(result.getStep()).isNull();
        assertThat(result.getAccessMeans())
                .extracting(AccessMeansDTO::getUserId, AccessMeansDTO::getAccessMeans, AccessMeansDTO::getUpdated, AccessMeansDTO::getExpireTime)
                .contains(userId, "providerState", Date.from(Instant.now(clock)), Date.from(Instant.now(clock).plusSeconds(1)));
        verify(providerStateMapper).mapToDataProviderState("fakeProviderState");
        verify(authorizationCodeExtractor).extractAuthorizationCode("someRedirectUrlPostedBack");
        verify(authorizationRestClient).getAccessToken(any(HttpClient.class), accessTokenRequestDTOArgumentCaptor.capture(), any(DefaultAuthenticationMeans.class), eq(TokenResponseDTO.class));
        AccessTokenRequest capturedAccessTokenRequestDTO = accessTokenRequestDTOArgumentCaptor.getValue();
        assertThat(capturedAccessTokenRequestDTO)
                .extracting(AccessTokenRequest::getTokenUrl, AccessTokenRequest::getAuthorizationCode, it -> it.getAuthMeans().getClientId(), AccessTokenRequest::getRedirectUrl, AccessTokenRequest::getAccessTokenScope, AccessTokenRequest::getProviderState)
                .contains("http://localhost/region1/token", "authCode", "fakeClient", "someBaseClientRedirectUrl", Scope.AISP, preAuthorizedProviderState);
        verify(providerStateMapper).mapToJson(providerStateArgumentCaptor.capture());
        DataProviderState capturedProviderState = providerStateArgumentCaptor.getValue();
        assertThat(capturedProviderState).extracting(DataProviderState::getRegion, DataProviderState::getAccessToken, DataProviderState::getRefreshToken)
                .contains(region, "accessToken", "refreshToken");
        assertThat(capturedProviderState.getCodeVerifier()).isNull();
    }

    private AccessMeansOrStepRequest createAccessMeansOrStepRequest(String callbackUrl, FilledInUserSiteFormValues formValues) {
        return new AccessMeansOrStepRequest(
                "providerState",
                createDefaultAuthenticationMeans(),
                callbackUrl,
                "http://localhost/redirect",
                UUID.randomUUID(),
                "fakeState",
                formValues,
                signer);
    }

    @Test
    void shouldReturnAccessMeansDTOForRefreshAccessMeansWhenCorrectDataProvided() throws TokenInvalidException {
        // given
        UUID userId = UUID.randomUUID();
        AccessMeansRequest accessMeansRequest = new AccessMeansRequest(
                createDefaultAuthenticationMeans(),
                new AccessMeansDTO(userId, "providerState", Date.from(Instant.now(clock)), Date.from(Instant.now(clock).plusSeconds(1))),
                DataProviderState.emptyState(),
                signer);
        AccessMeansDTO expectedAccessMeansDTO = new AccessMeansDTO(userId, "updatedProviderState", Date.from(Instant.now(clock)), Date.from(Instant.now(clock).plusSeconds(1)));
        when(refreshTokenStrategy.refreshAccessMeans(any(HttpClient.class), any(AccessMeansRequest.class)))
                .thenReturn(expectedAccessMeansDTO);

        // when
        AccessMeansDTO result = authorizationService.refreshAccessMeans(httpClient, accessMeansRequest);

        // then
        assertThat(result).extracting(AccessMeansDTO::getUserId, AccessMeansDTO::getAccessMeans, AccessMeansDTO::getUpdated, AccessMeansDTO::getExpireTime)
                .contains(userId, "updatedProviderState", Date.from(Instant.now(clock)), Date.from(Instant.now(clock).plusSeconds(1)));
        verify(refreshTokenStrategy).refreshAccessMeans(httpClient, accessMeansRequest);
    }

    private TokenResponseDTO createTokenResponseDTO() {
        return TestTokenResponseDTO.builder()
                .accessToken("accessToken")
                .refreshToken("refreshToken")
                .expiresIn(1)
                .build();
    }

    private DefaultAuthenticationMeans createDefaultAuthenticationMeans() {
        return DefaultAuthenticationMeans.builder()
                .clientId("fakeClient")
                .build();
    }

    private List<Region> prepareAvailableRegions() {
        List<Region> regions = new ArrayList<>();
        regions.add(createRegion(1));
        regions.add(createRegion(2));
        return regions;
    }

    private Region createRegion(int number) {
        Region region = new Region();
        region.setTokenUrl(String.format("http://localhost/region%d/token", number));
        region.setName(String.format("Region %d", number));
        region.setCode(String.format("REGION%d", number));
        region.setBaseUrl(String.format("http://localhost/region%d", number));
        region.setAuthUrl(String.format("http://localhost/region%d/authorize", number));
        return region;
    }
}
