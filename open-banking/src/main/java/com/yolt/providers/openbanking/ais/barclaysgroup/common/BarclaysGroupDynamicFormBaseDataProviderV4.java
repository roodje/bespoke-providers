package com.yolt.providers.openbanking.ais.barclaysgroup.common;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.form.EncryptionDetails;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlGetLoginRequest;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.FormStep;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.domain.dynamic.step.Step;
import com.yolt.providers.openbanking.ais.barclaysgroup.barclays.config.BarclaysPropertiesV3;
import com.yolt.providers.openbanking.ais.barclaysgroup.common.dto.BarclaysLoginFormDTO;
import com.yolt.providers.openbanking.ais.barclaysgroup.common.service.BarclaysGroupAuthenticationServiceV4;
import com.yolt.providers.openbanking.ais.exception.LoginNotFoundException;
import com.yolt.providers.openbanking.ais.generic2.GenericBaseDataProviderV2;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.common.ProviderIdentification;
import com.yolt.providers.openbanking.ais.generic2.domain.LoginInfoState;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClientFactory;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountrequestservice.AccountRequestService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountrequestservice.ConsentPermissions;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.FetchDataServiceV2;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken.AccessMeansStateMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken.AccessMeansStateProvider;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken.LoginInfoStateMapper;
import nl.ing.lovebird.providershared.form.Form;
import nl.ing.lovebird.providershared.form.SelectField;
import nl.ing.lovebird.providershared.form.SelectOptionValue;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class BarclaysGroupDynamicFormBaseDataProviderV4 extends GenericBaseDataProviderV2 {

    private static final Duration FORM_STEP_EXPIRY_DURATION = Duration.ofHours(1);
    private final BarclaysPropertiesV3 properties;
    private final LoginInfoStateMapper loginInfoStateMapper;
    private final Function<List<String>, LoginInfoState> loginInfoStateProvider;
    private final ConsentPermissions consentPermissions;
    private final ObjectMapper objectMapper;
    private final HttpClientFactory httpClientFactory;
    private final TokenScope scope;
    private final AccountRequestService accountRequestService;
    private final BarclaysGroupAuthenticationServiceV4 authenticationService;
    private final Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> getAuthenticationMeans;
    private final Clock clock;

    public BarclaysGroupDynamicFormBaseDataProviderV4(BarclaysPropertiesV3 properties,
                                                        FetchDataServiceV2 fetchDataService,
                                                      AccountRequestService accountRequestService,
                                                      BarclaysGroupAuthenticationServiceV4 authenticationService,
                                                      HttpClientFactory httpClientFactory,
                                                      TokenScope scope,
                                                      ProviderIdentification providerIdentification,
                                                      Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> getAuthenticationMeans,
                                                      Supplier<Map<String, TypedAuthenticationMeans>> typedAuthenticationMeansSupplier,
                                                      AccessMeansStateMapper accessMeansStateMapper,
                                                      AccessMeansStateProvider accessMeansStateProvider,
                                                      Supplier<Optional<KeyRequirements>> getSigningKeyRequirements,
                                                      Supplier<Optional<KeyRequirements>> getTransportKeyRequirements,
                                                      Supplier<ConsentValidityRules> consentValidityRulesSupplier,
                                                      LoginInfoStateMapper loginInfoStateMapper,
                                                      Function<List<String>, LoginInfoState> loginInfoStateProvider,
                                                      ObjectMapper objectMapper,
                                                      ConsentPermissions consentPermissions,
                                                      Clock clock) {
        super(fetchDataService, accountRequestService, authenticationService, httpClientFactory, scope,
                providerIdentification, getAuthenticationMeans, typedAuthenticationMeansSupplier, accessMeansStateMapper, accessMeansStateProvider,
                getSigningKeyRequirements, getTransportKeyRequirements, consentValidityRulesSupplier, loginInfoStateMapper, loginInfoStateProvider, consentPermissions);
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.getAuthenticationMeans = getAuthenticationMeans;
        this.httpClientFactory = httpClientFactory;
        this.accountRequestService = accountRequestService;
        this.authenticationService = authenticationService;
        this.scope = scope;
        this.loginInfoStateMapper = loginInfoStateMapper;
        this.loginInfoStateProvider = loginInfoStateProvider;
        this.consentPermissions = consentPermissions;
        this.clock = clock;
    }

    @Override
    public Step getLoginInfo(final UrlGetLoginRequest urlGetLogin) {
        SelectField selectField = new SelectField("AccountType", "Account Type", 0, 0, false, false);
        properties.getCustomerTypes().stream()
                .map(customerType -> new SelectOptionValue(customerType.getCode(), customerType.getType()))
                .forEachOrdered(selectField::addSelectOptionValue);
        Form selectForm = new Form(Collections.singletonList(selectField), null, null);
        try {
            return new FormStep(selectForm, EncryptionDetails.noEncryption(), Instant.now(clock).plus(FORM_STEP_EXPIRY_DURATION),
                    objectMapper.writeValueAsString(new BarclaysLoginFormDTO(urlGetLogin.getAuthenticationMeansReference(), urlGetLogin.getBaseClientRedirectUrl())));
        } catch (JsonProcessingException e) {
            throw new LoginNotFoundException(e);
        }
    }

    @Override
    public AccessMeansOrStepDTO createNewAccessMeans(final UrlCreateAccessMeansRequest urlCreateAccessMeans) {
        return shouldReturnLoginUrl(urlCreateAccessMeans) ?
                getStepWithProperLoginUrl(urlCreateAccessMeans) : super.createNewAccessMeans(urlCreateAccessMeans);
    }

    private boolean shouldReturnLoginUrl(UrlCreateAccessMeansRequest urlCreateAccessMeans) {
        return !StringUtils.isEmpty(urlCreateAccessMeans.getFilledInUserSiteFormValues());
    }

    private AccessMeansOrStepDTO getStepWithProperLoginUrl(final UrlCreateAccessMeansRequest request) {
        DefaultAuthMeans authenticationMeans = getAuthenticationMeans.apply(request.getAuthenticationMeans());
        try {
            HttpClient httpClient = httpClientFactory.createHttpClient(request.getRestTemplateManager(), authenticationMeans, getProviderIdentifierDisplayName());
            BarclaysLoginFormDTO loginFormDTO = objectMapper.readValue(request.getProviderState(), BarclaysLoginFormDTO.class);
            String accountRequestId = accountRequestService.requestNewAccountRequestId(httpClient, authenticationMeans,
                    loginFormDTO.getAuthenticationMeansReference(), scope, request.getSigner());
            String baseAuthorizationUrl = properties.getCustomerTypeByCode(request.getFilledInUserSiteFormValues().get("AccountType")).getAuthorizationUrl();
            String authorizationUrl = authenticationService.generateAuthorizationUrlBasedOnForm(authenticationMeans, accountRequestId, request.getState(),
                    loginFormDTO.getRedirectUrl(), scope, request.getSigner(), baseAuthorizationUrl);
            return new AccessMeansOrStepDTO(new RedirectStep(authorizationUrl, accountRequestId,
                    loginInfoStateMapper.toJson(loginInfoStateProvider.apply(consentPermissions.getPermissions()))));
        } catch (Exception e) {
            throw new LoginNotFoundException(e);
        }
    }
}
