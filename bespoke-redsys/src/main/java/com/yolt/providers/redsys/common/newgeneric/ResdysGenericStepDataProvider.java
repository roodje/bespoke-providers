package com.yolt.providers.redsys.common.newgeneric;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlGetLoginRequest;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequest;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.Step;
import com.yolt.providers.common.exception.MissingDataException;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.redsys.common.ProviderIdentification;
import com.yolt.providers.redsys.common.auth.RedsysAuthenticationMeans;
import com.yolt.providers.redsys.common.model.Token;
import com.yolt.providers.redsys.common.newgeneric.service.RedsysAuthorizationServiceV2;
import com.yolt.providers.redsys.common.newgeneric.service.RedsysFetchDataServiceV4;
import com.yolt.providers.redsys.common.util.HsmUtils;
import lombok.AllArgsConstructor;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.apache.commons.lang3.StringUtils;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static com.yolt.providers.redsys.common.auth.RedsysAuthenticationMeans.*;

//It differs from previous version with consentRetrievalProcess
//It defines list of steps that have to be executed in order to createAccessMeans
//Process starts during 'getLoginInfo'
//After that 'createAccessMeans' method is called by Site management several times,
//All steps will be performed sequentially from 0 to n (where n is number of actions in list)
//Each time we get into it we have different state and we intent to execute different logic
@AllArgsConstructor
public class ResdysGenericStepDataProvider<T extends SerializableConsentProcessData> implements UrlDataProvider {

    private final RedsysAuthorizationServiceV2<T> authorizationService;
    private final RedsysFetchDataServiceV4 fetchDataService;
    private final ProviderIdentification providerIdentification;
    private final ConsentProcess<T> consentRetrievalProcess;
    private final ProcessDataMapper<T> processDataMapper;
    private final Clock clock;
    public static final Duration FORM_STEP_EXPIRY_DURATION = Duration.ofHours(1);

    @Override
    public DataProviderResponse fetchData(final UrlFetchDataRequest urlFetchData) throws ProviderFetchDataException, TokenInvalidException {

        RedsysAuthenticationMeans authenticationMeans = RedsysAuthenticationMeans.fromAuthenticationMeans(
                urlFetchData.getAuthenticationMeans(), getProviderIdentifier());


        T processData = processDataMapper.deserializeState(urlFetchData.getAccessMeans().getAccessMeans());

        return fetchDataService.fetchData(authenticationMeans, processData, getProviderIdentifierDisplayName(), urlFetchData);
    }

    @Override
    public Step getLoginInfo(UrlGetLoginRequest urlGetLogin) {
        return consentRetrievalProcess.start(processDataMapper.map(urlGetLogin));
    }

    @Override
    public AccessMeansOrStepDTO createNewAccessMeans(final UrlCreateAccessMeansRequest urlCreateAccessMeans) {
        if (Objects.isNull(urlCreateAccessMeans)) {
            throw new MissingDataException("Missing UrlCreateAccessMeansRequest");
        }

        if (StringUtils.isEmpty(urlCreateAccessMeans.getProviderState())) {
            throw new MissingDataException("Missing provider state");
        }
        return consentRetrievalProcess.continueFrom(processDataMapper.map(urlCreateAccessMeans));
    }

    @Override
    public AccessMeansDTO refreshAccessMeans(final UrlRefreshAccessMeansRequest urlRefreshAccessMeans) throws TokenInvalidException {
        RedsysAuthenticationMeans authenticationMeans = RedsysAuthenticationMeans.fromAuthenticationMeans(
                urlRefreshAccessMeans.getAuthenticationMeans(), getProviderIdentifier());

        AccessMeansDTO accessMeansDTO = urlRefreshAccessMeans.getAccessMeans();
        T consentProcessData = processDataMapper.deserializeState(accessMeansDTO.getAccessMeans());

        Token accessToken = authorizationService.createNewAccessTokenFromRefreshToken(
                urlRefreshAccessMeans.getRestTemplateManager(),
                urlRefreshAccessMeans, authenticationMeans, consentProcessData);

        if (accessToken.getRefreshToken() == null) {
            throw new TokenInvalidException();
        }
        consentProcessData.getAccessMeans().setToken(accessToken);
        consentProcessData.getAccessMeans().setCodeVerifier(null);

        return new AccessMeansDTO(
                accessMeansDTO.getUserId(),
                processDataMapper.serializeState(consentProcessData),
                new Date(),
                Date.from(Instant.now(clock).plusSeconds(accessToken.getExpiresIn()))
        );
    }

    @Override
    public ConsentValidityRules getConsentValidityRules() {
        return ConsentValidityRules.EMPTY_RULES_SET;
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        Map<String, TypedAuthenticationMeans> typedAuthenticationMeans = new HashMap<>();
        typedAuthenticationMeans.put(CLIENT_ID_NAME, TypedAuthenticationMeans.CLIENT_ID_STRING);
        typedAuthenticationMeans.put(TRANSPORT_CERTIFICATE_NAME, TypedAuthenticationMeans.CLIENT_TRANSPORT_CERTIFICATE_PEM);
        typedAuthenticationMeans.put(TRANSPORT_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID);
        typedAuthenticationMeans.put(SIGNING_CERTIFICATE_NAME, TypedAuthenticationMeans.CLIENT_SIGNING_CERTIFICATE_PEM);
        typedAuthenticationMeans.put(SIGNING_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID);
        return typedAuthenticationMeans;
    }

    @Override
    public Optional<KeyRequirements> getTransportKeyRequirements() {
        return HsmUtils.getKeyRequirements(TRANSPORT_KEY_ID_NAME, TRANSPORT_CERTIFICATE_NAME);
    }

    @Override
    public Optional<KeyRequirements> getSigningKeyRequirements() {
        return HsmUtils.getKeyRequirements(SIGNING_KEY_ID_NAME, SIGNING_CERTIFICATE_NAME);
    }

    @Override
    public final String getProviderIdentifier() {
        return providerIdentification.getIdentifier();
    }

    @Override
    public final String getProviderIdentifierDisplayName() {
        return providerIdentification.getDisplayName();
    }

    @Override
    public final ProviderVersion getVersion() {
        return providerIdentification.getVersion();
    }
}
