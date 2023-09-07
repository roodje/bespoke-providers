package com.yolt.providers.yoltprovider;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlGetLoginRequest;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequest;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.RestTemplateManagerConfiguration;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.Step;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.common.versioning.ProviderVersion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import nl.ing.lovebird.providershared.api.AuthenticationMeansReference;
import org.springframework.web.client.RestTemplate;

import java.security.cert.X509Certificate;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.yolt.providers.common.versioning.ProviderVersion.VERSION_1;
import static java.util.Optional.ofNullable;

@Slf4j
@RequiredArgsConstructor
public class YoltProvider implements UrlDataProvider {

    private static final String IP_ADDRESS_FOR_FORM_BEFORE_URL_TESTSCENARIO = "127.0.0.2";
    private static final String IP_ADDRESS_CONSENT_EXPIRED = "127.0.0.3";
    private static final String IP_ADDRESS_TECHNICAL_ERROR = "127.0.0.4";
    /**
     * Requirements is that this Ip address results in either:
     * 1) TECHNICAL_ERROR
     * 2) CONSENT_EXPIRED
     * 3) normal successful data fetc.
     */
    private static final String IP_ADDRESS_RANDOM_RESULT = "127.0.0.5";

    // --
    // Form first scenarios
    // --
    private static final String IP_ADDRESS_REGION_SELECTION_THEN_REDIRECT = "127.0.1.1";
    private static final String IP_ADDRESS_IBAN_THEN_REDIRECT = "127.0.1.2";
    private static final String IP_BANK_SELECTION_THEN_REDIRECT = "127.0.1.3";
    private static final String IP_ACCOUNT_TYPE_SELECTION_THEN_REDIRECT = "127.0.1.4";
    private static final String IP_IBAN_AND_LANGUAGE_SELECTION_THEN_REDIRECT = "127.0.1.5";
    private static final String IP_BRANCH_AND_ACCOUNT_THEN_REDIRECT = "127.0.1.6";
    private static final String IP_USERNAME_THEN_REDIRECT = "127.0.1.7";
    private static final String IP_EMAIL_THEN_REDIRECT = "127.0.1.8";
    private static final String IP_LANGUAGE_THEN_REDIRECT = "127.0.1.9";
    private static final String IP_LOGIN_ID_THEN_REDIRECT = "127.0.1.10";
    private static final String IP_IBAN_AND_USERNAME_THEN_REDIRECT = "127.0.1.11";
    private static final String IP_USERNAME_AND_PASSWORD_THEN_REDIRECT = "127.0.1.12";
    private static final String BERLIN_FLOW_SCENARIO = "127.0.1.13";

    private final YoltProviderConfigurationProperties properties;
    private final YoltProviderFetchDataService yoltProviderFetchDataService;
    private final YoltProviderAuthorizationService yoltProviderAuthorizationService;
    private final Clock clock;

    public static AccessMeansDTO createAccessMeans(final UUID userId, final String accessMeans, final Clock clock) {
        Date expirationDate = calculateExpirationDate(90, ChronoUnit.DAYS, clock);
        return new AccessMeansDTO(userId, accessMeans, Date.from(Instant.now(clock)), expirationDate);
    }

    private static Date calculateExpirationDate(final int expiresIn, final ChronoUnit unit, final Clock clock) {
        return Date.from(Instant.now(clock).plus(expiresIn, unit));
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        return AuthenticationMeans.getTypedAuthenticationMeans();
    }

    @Override
    public ConsentValidityRules getConsentValidityRules() {
        return ConsentValidityRules.EMPTY_RULES_SET;
    }

    @Override
    public Optional<KeyRequirements> getSigningKeyRequirements() {
        return AuthenticationMeans.getSigningKeyRequirements();

    }

    @Override
    public Optional<KeyRequirements> getTransportKeyRequirements() {
        return AuthenticationMeans.getTransportKeyRequirements();
    }

    @Override
    public Step getLoginInfo(final UrlGetLoginRequest urlGetLogin) {
        var redirectUrl = urlGetLogin.getBaseClientRedirectUrl();
        var customerAuthorizationUrl = properties.getCustomerAuthorizationUrl();

        var authenticationMeans = AuthenticationMeans.fromAuthenticationMeans(urlGetLogin.getAuthenticationMeans());
        var restTemplate = getRestTemplate(
                urlGetLogin.getRestTemplateManager(),
                authenticationMeans.getTransportKid(),
                authenticationMeans.getClientTransportCertificate());

        var personaToken = ofNullable(urlGetLogin.getAuthenticationMeansReference())
                .map(AuthenticationMeansReference::getClientId)
                .flatMap(clientId -> yoltProviderFetchDataService.getPersonaTokenFromYoltBank(restTemplate, clientId))
                .orElse(null);

        // These PSU IP addresses are set in site-management based on siteId that is defined in {@link YoltProviderAisDetailsProvider::AIS_SITE_DETAILS}.
        if (IP_ADDRESS_FOR_FORM_BEFORE_URL_TESTSCENARIO.equals(urlGetLogin.getPsuIpAddress()) || IP_ADDRESS_REGION_SELECTION_THEN_REDIRECT.equals(urlGetLogin.getPsuIpAddress())) {
            return FormFirstScenario.regionSelection(redirectUrl, customerAuthorizationUrl)
                    .createFirstStep(urlGetLogin.getState(), personaToken, clock);
        } else if (IP_ADDRESS_IBAN_THEN_REDIRECT.equals(urlGetLogin.getPsuIpAddress())) {
            return FormFirstScenario.iban(redirectUrl, customerAuthorizationUrl)
                    .createFirstStep(urlGetLogin.getState(), personaToken, clock);
        } else if (IP_BANK_SELECTION_THEN_REDIRECT.equals(urlGetLogin.getPsuIpAddress())) {
            return FormFirstScenario.bankSelection(redirectUrl, customerAuthorizationUrl)
                    .createFirstStep(urlGetLogin.getState(), personaToken, clock);
        } else if (IP_ACCOUNT_TYPE_SELECTION_THEN_REDIRECT.equals(urlGetLogin.getPsuIpAddress())) {
            return FormFirstScenario.accountTypeSelection(redirectUrl, customerAuthorizationUrl)
                    .createFirstStep(urlGetLogin.getState(), personaToken, clock);
        } else if (IP_IBAN_AND_LANGUAGE_SELECTION_THEN_REDIRECT.equals(urlGetLogin.getPsuIpAddress())) {
            return FormFirstScenario.ibanAndLanguage(redirectUrl, customerAuthorizationUrl)
                    .createFirstStep(urlGetLogin.getState(), personaToken, clock);
        } else if (IP_BRANCH_AND_ACCOUNT_THEN_REDIRECT.equals(urlGetLogin.getPsuIpAddress())) {
            return FormFirstScenario.branchAndAccount(redirectUrl, customerAuthorizationUrl)
                    .createFirstStep(urlGetLogin.getState(), personaToken, clock);
        } else if (IP_IBAN_AND_USERNAME_THEN_REDIRECT.equals(urlGetLogin.getPsuIpAddress())) {
            return FormFirstScenario.ibanAndUsername(redirectUrl, customerAuthorizationUrl)
                    .createFirstStep(urlGetLogin.getState(), personaToken, clock);
        } else if (IP_USERNAME_THEN_REDIRECT.equals(urlGetLogin.getPsuIpAddress())) {
            return FormFirstScenario.username(redirectUrl, customerAuthorizationUrl)
                    .createFirstStep(urlGetLogin.getState(), personaToken, clock);
        } else if (IP_EMAIL_THEN_REDIRECT.equals(urlGetLogin.getPsuIpAddress())) {
            return FormFirstScenario.email(redirectUrl, customerAuthorizationUrl)
                    .createFirstStep(urlGetLogin.getState(), personaToken, clock);
        } else if (IP_LANGUAGE_THEN_REDIRECT.equals(urlGetLogin.getPsuIpAddress())) {
            return FormFirstScenario.languageSelection(redirectUrl, customerAuthorizationUrl)
                    .createFirstStep(urlGetLogin.getState(), personaToken, clock);
        } else if (IP_LOGIN_ID_THEN_REDIRECT.equals(urlGetLogin.getPsuIpAddress())) {
            return FormFirstScenario.loginId(redirectUrl, customerAuthorizationUrl)
                    .createFirstStep(urlGetLogin.getState(), personaToken, clock);
        } else if (IP_USERNAME_AND_PASSWORD_THEN_REDIRECT.equals(urlGetLogin.getPsuIpAddress())) {
            return new EncryptedFormScenario(redirectUrl, customerAuthorizationUrl, new UsernameAndPasswordForm())
                    .createFirstStep(urlGetLogin.getState(), personaToken, clock);
        } else if (BERLIN_FLOW_SCENARIO.equals(urlGetLogin.getPsuIpAddress())) {
            return new BerlinFlowScenario(redirectUrl, customerAuthorizationUrl)
                    .createFirstStep(urlGetLogin.getState(), personaToken, clock);
        }

        return new RedirectFirstScenario(redirectUrl, customerAuthorizationUrl)
                .createFirstStep(urlGetLogin.getState(), personaToken, clock);
    }

    @Override
    public AccessMeansOrStepDTO createNewAccessMeans(final UrlCreateAccessMeansRequest urlCreateAccessMeansRequest) {
        Scenario scenario = Scenario.fromJson(urlCreateAccessMeansRequest.getProviderState());

        final AccessMeansOrStepDTO authorizationCodeOrStep = scenario.processStep(urlCreateAccessMeansRequest, clock);

        AccessMeansDTO accessMeans = authorizationCodeOrStep.getAccessMeans();
        if (accessMeans != null) {
            AuthenticationMeans authenticationMeans = AuthenticationMeans.fromAuthenticationMeans(urlCreateAccessMeansRequest.getAuthenticationMeans());
            RestTemplate restTemplate = getRestTemplate(
                    urlCreateAccessMeansRequest.getRestTemplateManager(),
                    authenticationMeans.getTransportKid(),
                    authenticationMeans.getClientTransportCertificate()
            );

            if (BERLIN_FLOW_SCENARIO.equals(urlCreateAccessMeansRequest.getPsuIpAddress())) {
                // No redirect, so no auth_code..
                return new AccessMeansOrStepDTO(accessMeans);
            }
            final String accessToken = yoltProviderAuthorizationService.exchangeAuthorizationCodeForAccessToken(restTemplate, accessMeans.getAccessMeans());
            accessMeans.setAccessMeans(accessToken);
            return new AccessMeansOrStepDTO(accessMeans);
        }

        return authorizationCodeOrStep;
    }

    private RestTemplate getRestTemplate(final RestTemplateManager restTemplateManager, final UUID keyId, final X509Certificate certificate) {
        return restTemplateManager.manage(new RestTemplateManagerConfiguration(
                keyId,
                certificate,
                externalRestTemplateBuilderFactory -> externalRestTemplateBuilderFactory
                        .rootUri(properties.getBaseUrl())
                        .build()));
    }

    @Override
    public DataProviderResponse fetchData(final UrlFetchDataRequest urlFetchData) throws ProviderFetchDataException, TokenInvalidException {

        String psuIpAddress = urlFetchData.getPsuIpAddress();
        if (IP_ADDRESS_CONSENT_EXPIRED.equals(psuIpAddress)) {
            throw new TokenInvalidException("Mocking token invalid because psu-ip-address=" + IP_ADDRESS_CONSENT_EXPIRED);
        }
        if (IP_ADDRESS_TECHNICAL_ERROR.equals(psuIpAddress)) {
            throw new ProviderFetchDataException("Mocking a technical error because psu-ip-address = " + IP_ADDRESS_TECHNICAL_ERROR);
        }
        if (IP_ADDRESS_RANDOM_RESULT.equals(psuIpAddress)) {
            int randomChoice = new Random().nextInt(3); //NOSONAR
            if (randomChoice == 0) {
                throw new ProviderFetchDataException("Mocking a technical error because psu-ip-address = " + IP_ADDRESS_RANDOM_RESULT);
            } else if (randomChoice == 1) {
                throw new TokenInvalidException("Mocking token invalid because psu-ip-address=" + IP_ADDRESS_RANDOM_RESULT);
            } else {
                log.info("Proceeding with a succesfull datafetch because psu-ip-address=" + IP_ADDRESS_RANDOM_RESULT);
            }
        }

        AuthenticationMeans authenticationMeans = AuthenticationMeans.fromAuthenticationMeans(urlFetchData.getAuthenticationMeans());
        String accessMeans = urlFetchData.getAccessMeans().getAccessMeans();
        if (BerlinFlowScenario.BERLIN_FLOW_AUTHORIZATION_CODE.equals(accessMeans)) {
            // The berlin flow does not contain any redirect. Thus, we didn't have any change to select a persona in yoltbank.
            // The selection in yoltbank basically creates a user (auth code) and this determines what accounts and transactions
            // are returned. We can't do that here, so we return a simple account.
            return new DataProviderResponse(List.of(StubData.dummyAccount(clock)));
        }

        RestTemplate restTemplate = getRestTemplate(
                urlFetchData.getRestTemplateManager(),
                authenticationMeans.getTransportKid(),
                authenticationMeans.getClientTransportCertificate());

        return yoltProviderFetchDataService.getAccountsAndTransactions(restTemplate,
                accessMeans,
                urlFetchData.getTransactionsFetchStartTime(),
                authenticationMeans.getClientSigningCertificate(),
                urlFetchData.getSigner(),
                authenticationMeans.getSigningKid());
    }

    @Override
    public AccessMeansDTO refreshAccessMeans(final UrlRefreshAccessMeansRequest urlRefreshAccessMeans) {
        AccessMeansDTO accessMeans = urlRefreshAccessMeans.getAccessMeans();
        return createAccessMeans(accessMeans.getUserId(), accessMeans.getAccessMeans(), clock);
    }

    @Override
    public String getProviderIdentifier() {
        return "YOLT_PROVIDER";
    }

    @Override
    public String getProviderIdentifierDisplayName() {
        return "Yolt Test Bank Provider";
    }

    @Override
    public ProviderVersion getVersion() {
        return VERSION_1;
    }
}
