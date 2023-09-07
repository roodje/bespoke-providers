package com.yolt.providers.belfius.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.belfius.common.auth.BelfiusGroupAuthMeans;
import com.yolt.providers.belfius.common.auth.HsmUtils;
import com.yolt.providers.belfius.common.configuration.BelfiusBaseProperties;
import com.yolt.providers.belfius.common.exception.ConsentResponseSizeException;
import com.yolt.providers.belfius.common.exception.LoginNotFoundException;
import com.yolt.providers.belfius.common.exception.UnexpectedJsonElementException;
import com.yolt.providers.belfius.common.http.BelfiusGroupRestTemplateFactory;
import com.yolt.providers.belfius.common.http.client.BelfiusGroupHttpClient;
import com.yolt.providers.belfius.common.http.client.BelfiusGroupTokenHttpClient;
import com.yolt.providers.belfius.common.model.BelfiusGroupAccessMeans;
import com.yolt.providers.belfius.common.model.BelfiusGroupConsentLanguage;
import com.yolt.providers.belfius.common.service.BelfiusGroupAuthorizationService;
import com.yolt.providers.belfius.common.service.BelfiusGroupFetchDataService;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.form.EncryptionDetails;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlGetLoginRequest;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequest;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.FormStep;
import com.yolt.providers.common.domain.dynamic.step.Step;
import com.yolt.providers.common.exception.MessageSuppressingException;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import nl.ing.lovebird.providershared.form.Form;
import nl.ing.lovebird.providershared.form.SelectField;
import nl.ing.lovebird.providershared.form.SelectOptionValue;
import nl.ing.lovebird.providershared.form.TextField;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static com.yolt.providers.belfius.common.auth.BelfiusGroupAuthMeans.*;

@RequiredArgsConstructor
public abstract class BelfiusGroupDataProvider implements UrlDataProvider {

    private final ObjectMapper objectMapper;
    private final BelfiusBaseProperties properties;
    private final BelfiusGroupAuthorizationService authorizationService;
    private final BelfiusGroupFetchDataService fetchDataService;
    private final Clock clock;

    @Override
    public Step getLoginInfo(UrlGetLoginRequest urlGetLogin) {
        TextField textField = new TextField("Iban", "IBAN", 34, 34, false, false);
        SelectField selectField = new SelectField("ConsentLanguage", "Consent Language", 0, 0, false, false);
        Arrays.stream(BelfiusGroupConsentLanguage.values())
                .map(belfiusGroupConsentLanguage -> new SelectOptionValue(belfiusGroupConsentLanguage.getValue(), belfiusGroupConsentLanguage.name()))
                .forEachOrdered(selectField::addSelectOptionValue);

        Form form = new Form(Arrays.asList(textField, selectField), null, null);
        try {
            return new FormStep(form, EncryptionDetails.noEncryption(), Instant.now(clock).plus(Duration.ofHours(1)), null);
        } catch (Exception e) {
            throw new LoginNotFoundException("There was an error during dynamic form creation", new MessageSuppressingException(e));
        }
    }

    @Override
    public AccessMeansOrStepDTO createNewAccessMeans(UrlCreateAccessMeansRequest urlCreateAccessMeans) {
        return StringUtils.isEmpty(urlCreateAccessMeans.getFilledInUserSiteFormValues())
                ? actuallyCreateAccessMeans(urlCreateAccessMeans)
                : actuallyGetLoginUrl(urlCreateAccessMeans);
    }

    private AccessMeansOrStepDTO actuallyGetLoginUrl(UrlCreateAccessMeansRequest urlCreateAccessMeans) {
        BelfiusGroupAuthMeans authMeans = BelfiusGroupAuthMeans
                .fromAuthenticationMeans(urlCreateAccessMeans.getAuthenticationMeans(), getProviderIdentifier());
        RestTemplate restTemplate = new BelfiusGroupRestTemplateFactory(objectMapper, properties)
                .createRestTemplateWithManagedMutualTLSTemplate(urlCreateAccessMeans.getRestTemplateManager(), authMeans);

        String language = urlCreateAccessMeans.getFilledInUserSiteFormValues().get("ConsentLanguage");
        BelfiusGroupHttpClient belfiusGroupHttpClient = new BelfiusGroupHttpClient(
                restTemplate, authMeans, language, urlCreateAccessMeans.getBaseClientRedirectUrl(), properties, clock);

        try {
            return new AccessMeansOrStepDTO(authorizationService.getLoginUrlForUser(belfiusGroupHttpClient, urlCreateAccessMeans));
        } catch (ConsentResponseSizeException e) {
            throw new LoginNotFoundException(e.getMessage(), e);
        } catch (Exception e) {
            throw new LoginNotFoundException("There was an error with retrieving login url", new MessageSuppressingException(e));
        }
    }

    private AccessMeansOrStepDTO actuallyCreateAccessMeans(UrlCreateAccessMeansRequest urlCreateAccessMeans) {
        BelfiusGroupAuthMeans authMeans = BelfiusGroupAuthMeans
                .fromAuthenticationMeans(urlCreateAccessMeans.getAuthenticationMeans(), getProviderIdentifier());

        RestTemplate restTemplate = new BelfiusGroupRestTemplateFactory(objectMapper, properties)
                .createRestTemplateWithManagedMutualTLSTemplate(urlCreateAccessMeans.getRestTemplateManager(), authMeans);
        BelfiusGroupTokenHttpClient httpClient = new BelfiusGroupTokenHttpClient(restTemplate, authMeans, properties);

        BelfiusGroupAccessMeans accessMeans = authorizationService.getAccessToken(httpClient, urlCreateAccessMeans);

        return new AccessMeansOrStepDTO(toAccessMeans(urlCreateAccessMeans.getUserId(), accessMeans));
    }

    @Override
    public AccessMeansDTO refreshAccessMeans(UrlRefreshAccessMeansRequest urlRefreshAccessMeans) throws TokenInvalidException {
        BelfiusGroupAuthMeans authMeans = BelfiusGroupAuthMeans
                .fromAuthenticationMeans(urlRefreshAccessMeans.getAuthenticationMeans(), getProviderIdentifier());
        RestTemplate restTemplate = new BelfiusGroupRestTemplateFactory(objectMapper, properties)
                .createRestTemplateWithManagedMutualTLSTemplate(urlRefreshAccessMeans.getRestTemplateManager(), authMeans);
        BelfiusGroupTokenHttpClient belfiusGroupHttpClient = new BelfiusGroupTokenHttpClient(restTemplate, authMeans, properties);

        AccessMeansDTO accessMeansDTO = urlRefreshAccessMeans.getAccessMeans();
        BelfiusGroupAccessMeans accessMeans = toOAuthToken(accessMeansDTO.getAccessMeans());

        BelfiusGroupAccessMeans refreshedOAuthToken = authorizationService.getAccessTokenUsingRefreshToken(
                belfiusGroupHttpClient, accessMeans);
        return toAccessMeans(accessMeansDTO.getUserId(), refreshedOAuthToken);
    }

    @Override
    public DataProviderResponse fetchData(UrlFetchDataRequest urlFetchData) throws ProviderFetchDataException, TokenInvalidException {
        BelfiusGroupAuthMeans authMeans = BelfiusGroupAuthMeans
                .fromAuthenticationMeans(urlFetchData.getAuthenticationMeans(), getProviderIdentifier());
        RestTemplate restTemplate = new BelfiusGroupRestTemplateFactory(objectMapper, properties)
                .createRestTemplateWithManagedMutualTLSTemplate(urlFetchData.getRestTemplateManager(), authMeans);
        BelfiusGroupAccessMeans accessMeans = toOAuthToken(urlFetchData.getAccessMeans().getAccessMeans());
        BelfiusGroupHttpClient belfiusGroupHttpClient = new BelfiusGroupHttpClient(
                restTemplate, authMeans, accessMeans.getLanguage(), accessMeans.getRedirectUrl(), properties, clock);

        return fetchDataService.fetchData(belfiusGroupHttpClient, accessMeans, urlFetchData.getTransactionsFetchStartTime());
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        Map<String, TypedAuthenticationMeans> typedAuthenticationMeans = new HashMap<>();
        typedAuthenticationMeans.put(CLIENT_ID_NAME, TypedAuthenticationMeans.CLIENT_ID_STRING);
        typedAuthenticationMeans.put(CLIENT_SECRET_NAME, TypedAuthenticationMeans.CLIENT_SECRET_STRING);
        typedAuthenticationMeans.put(CLIENT_TRANSPORT_CERTIFICATE_NAME, TypedAuthenticationMeans.CLIENT_TRANSPORT_CERTIFICATE_PEM);
        typedAuthenticationMeans.put(CLIENT_TRANSPORT_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID_HEADER_STRING);
        return typedAuthenticationMeans;
    }

    @Override
    public Optional<KeyRequirements> getTransportKeyRequirements() {
        return HsmUtils.getKeyRequirements(CLIENT_TRANSPORT_KEY_ID_NAME, CLIENT_TRANSPORT_CERTIFICATE_NAME);
    }

    private AccessMeansDTO toAccessMeans(UUID userId, BelfiusGroupAccessMeans accessMeans) {
        Date expirationDate = Date.from(Instant.now(clock).plusSeconds(accessMeans.getAccessToken().getExpiresIn()));
        try {
            return new AccessMeansDTO(userId, objectMapper.writeValueAsString(accessMeans), new Date(), expirationDate);
        } catch (JsonProcessingException e) {
            throw new UnexpectedJsonElementException("Couldn't parse OAuth Token to JSON");
        }
    }

    private BelfiusGroupAccessMeans toOAuthToken(String accessMeans) throws TokenInvalidException {
        try {
            return objectMapper.readValue(accessMeans, BelfiusGroupAccessMeans.class);
        } catch (JsonProcessingException e) {
            throw new TokenInvalidException("Couldn't parse access means to Token object");
        }
    }
}
