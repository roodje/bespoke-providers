package com.yolt.providers.direkt1822group.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.form.EncryptionDetails;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.DistinguishedNameElement;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyAlgorithm;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyMaterialRequirements;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.FormStep;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.domain.dynamic.step.Step;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.direkt1822group.common.dto.ConsentData;
import com.yolt.providers.direkt1822group.common.dto.Direkt1822GroupLoginFormDTO;
import com.yolt.providers.direkt1822group.common.exception.LoginNotFoundException;
import com.yolt.providers.direkt1822group.common.service.Direkt1822GroupAuthenticationService;
import com.yolt.providers.direkt1822group.common.service.Direkt1822GroupFetchDataService;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import nl.ing.lovebird.providershared.form.Form;
import nl.ing.lovebird.providershared.form.TextField;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.yolt.providers.direkt1822group.common.Direkt1822GroupAuthenticationMeans.*;

@RequiredArgsConstructor
public class Direkt1822GroupDataProvider implements UrlDataProvider {

    private static final String IBAN_ID = "Iban";

    private final Direkt1822GroupAuthenticationService direkt1822AuthenticationService;
    private final Direkt1822GroupFetchDataService direkt1822GroupFetchDataService;
    private final ObjectMapper objectMapper;
    private final String providerIdentifier;
    private final String providerDisplayName;
    private final ProviderVersion version;
    private final Clock clock;

    @Override
    public Step getLoginInfo(UrlGetLoginRequest urlGetLogin) {
        TextField textField = new TextField(IBAN_ID, "IBAN", 34, 34, false, false);
        Form form = new Form(Collections.singletonList(textField), null, null);
        try {
            return new FormStep(form, EncryptionDetails.noEncryption(), Instant.now(clock).plus(Duration.ofHours(1)),
                    objectMapper.writeValueAsString(new Direkt1822GroupLoginFormDTO(urlGetLogin.getAuthenticationMeansReference(), urlGetLogin.getBaseClientRedirectUrl())));
        } catch (JsonProcessingException e) {
            throw new LoginNotFoundException(e);
        }
    }

    @Override
    public AccessMeansOrStepDTO createNewAccessMeans(UrlCreateAccessMeansRequest urlCreateAccessMeans) {
        return StringUtils.isEmpty(urlCreateAccessMeans.getFilledInUserSiteFormValues())
                ? createProperAccessMeans(urlCreateAccessMeans)
                : returnProperLoginUrl(urlCreateAccessMeans);
    }

    @Override
    public AccessMeansDTO refreshAccessMeans(UrlRefreshAccessMeansRequest urlRefreshAccessMeans) throws TokenInvalidException {
        throw new TokenInvalidException("Refreshing tokens is not supported by bank");
    }

    @Override
    public DataProviderResponse fetchData(UrlFetchDataRequest urlFetchData) throws ProviderFetchDataException, TokenInvalidException {
        Direkt1822GroupAccessMeans accessMeans = deserializeAccessMeans(urlFetchData.getAccessMeans().getAccessMeans());
        Direkt1822GroupAuthenticationMeans authenticationMeans = createGroupAuthenticationMeans(urlFetchData.getAuthenticationMeans(), getProviderIdentifier());

        return direkt1822GroupFetchDataService.fetchData(
                getProviderIdentifier(),
                accessMeans,
                authenticationMeans,
                urlFetchData.getRestTemplateManager(),
                urlFetchData.getTransactionsFetchStartTime(),
                urlFetchData.getPsuIpAddress()
        );
    }

    @Override
    public void onUserSiteDelete(UrlOnUserSiteDeleteRequest urlOnUserSiteDeleteRequest) throws TokenInvalidException {

        Direkt1822GroupAuthenticationMeans authMeans = Direkt1822GroupAuthenticationMeans
                .createAuthMeans(urlOnUserSiteDeleteRequest.getAuthenticationMeans(), getProviderIdentifier());

        direkt1822AuthenticationService.deleteConsent(authMeans,
                getProviderIdentifier(),
                urlOnUserSiteDeleteRequest.getRestTemplateManager(),
                deserializeAccessMeans(urlOnUserSiteDeleteRequest.getAccessMeans().getAccessMeans()),
                urlOnUserSiteDeleteRequest.getPsuIpAddress());
    }

    @Override
    public String getProviderIdentifier() {
        return providerIdentifier;
    }

    @Override
    public String getProviderIdentifierDisplayName() {
        return providerDisplayName;
    }

    @Override
    public ProviderVersion getVersion() {
        return version;
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        Map<String, TypedAuthenticationMeans> typedAuthenticationMeans = new HashMap<>();
        typedAuthenticationMeans.put(CLIENT_TRANSPORT_CERTIFICATE_NAME, TypedAuthenticationMeans.CLIENT_TRANSPORT_CERTIFICATE_PEM);
        typedAuthenticationMeans.put(CLIENT_TRANSPORT_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID_HEADER_STRING);
        return typedAuthenticationMeans;
    }

    private AccessMeansOrStepDTO returnProperLoginUrl(UrlCreateAccessMeansRequest urlCreateAccessMeans) {
        Direkt1822GroupAuthenticationMeans authMeans = Direkt1822GroupAuthenticationMeans
                .createAuthMeans(urlCreateAccessMeans.getAuthenticationMeans(), getProviderIdentifier());

        try {
            Direkt1822GroupLoginFormDTO direkt1822GroupLoginFormDTO = objectMapper.readValue(urlCreateAccessMeans.getProviderState(), Direkt1822GroupLoginFormDTO.class);
            String iban = urlCreateAccessMeans.getFilledInUserSiteFormValues().get(IBAN_ID).replaceAll("\\s+", "").toUpperCase();
            ConsentData consentUrlData = direkt1822AuthenticationService.generateLoginUrl(
                    authMeans,
                    getProviderIdentifier(),
                    urlCreateAccessMeans.getRestTemplateManager(),
                    direkt1822GroupLoginFormDTO,
                    iban,
                    urlCreateAccessMeans.getPsuIpAddress(),
                    urlCreateAccessMeans.getState());
            return new AccessMeansOrStepDTO(new RedirectStep(consentUrlData.getLoginUrl(), consentUrlData.getConsentId(), consentUrlData.getConsentId()));
        } catch (TokenInvalidException e) {
            throw new LoginNotFoundException(e);
        } catch (JsonProcessingException e) {
            throw new LoginNotFoundException("Could not deserialize provider state");
        }
    }

    private AccessMeansOrStepDTO createProperAccessMeans(UrlCreateAccessMeansRequest urlCreateAccessMeans) {

        String consentId = urlCreateAccessMeans.getProviderState();
        try {
            String serializedAccessMeans = objectMapper.writeValueAsString(new Direkt1822GroupAccessMeans(consentId));
            return new AccessMeansOrStepDTO(
                    new AccessMeansDTO(
                            urlCreateAccessMeans.getUserId(),
                            serializedAccessMeans,
                            new Date(),
                            Date.from(Instant.now(clock).plus(89, ChronoUnit.DAYS))
                    )
            );
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Couldn't create access means due to JSON processing error");
        }
    }

    private Direkt1822GroupAccessMeans deserializeAccessMeans(String serializedAccessMeans) throws TokenInvalidException {
        try {
            return objectMapper.readValue(serializedAccessMeans, Direkt1822GroupAccessMeans.class);
        } catch (JsonProcessingException e) {
            throw new TokenInvalidException("Could not deserialize access means");
        }
    }

    @Override
    public Optional<KeyRequirements> getTransportKeyRequirements() {
        return Optional.of(new KeyRequirements(getKeyRequirements(), CLIENT_TRANSPORT_KEY_ID_NAME, CLIENT_TRANSPORT_CERTIFICATE_NAME));
    }

    @Override
    public ConsentValidityRules getConsentValidityRules() {
        return ConsentValidityRules.EMPTY_RULES_SET;
    }

    private static KeyMaterialRequirements getKeyRequirements() {
        Set<KeyAlgorithm> supportedAlgorithms = new HashSet<>();
        supportedAlgorithms.add(KeyAlgorithm.RSA2048);
        supportedAlgorithms.add(KeyAlgorithm.RSA4096);

        Set<SignatureAlgorithm> supportedSignatureAlgorithms = new HashSet<>();
        supportedSignatureAlgorithms.add(SignatureAlgorithm.SHA256_WITH_RSA);
        supportedSignatureAlgorithms.add(SignatureAlgorithm.SHA512_WITH_RSA);

        List<DistinguishedNameElement> requiredDNs = new ArrayList<>();
        requiredDNs.add(new DistinguishedNameElement("C", "", "Country", true));
        requiredDNs.add(new DistinguishedNameElement("ST", "", "State / Province", true));
        requiredDNs.add(new DistinguishedNameElement("L", "", "Locality name", true));
        requiredDNs.add(new DistinguishedNameElement("O", "", "Organization name", true));
        requiredDNs.add(new DistinguishedNameElement("OU", "", "Organizational unit", true));
        requiredDNs.add(new DistinguishedNameElement("CN", "", "Common name", true));

        return new KeyMaterialRequirements(supportedAlgorithms, supportedSignatureAlgorithms, requiredDNs);
    }
}
