package com.yolt.providers.yoltprovider;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.form.EncryptionDetails;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.FormStep;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.domain.dynamic.step.Step;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.MissingDataException;
import lombok.*;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import nl.ing.lovebird.providershared.form.FilledInUserSiteFormValues;
import org.apache.commons.lang3.tuple.Pair;
import org.jose4j.lang.JoseException;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.HOURS;


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = FormFirstScenario.class, name = "FORM_FIRST"),
        @JsonSubTypes.Type(value = RedirectFirstScenario.class, name = "REDIRECT_FIRST"),
        @JsonSubTypes.Type(value = ExtraConsentScenario.class, name = "REDIRECT_EXTRA_CONSENT"),
        @JsonSubTypes.Type(value = EncryptedFormScenario.class, name = "ENCRYPTED_FORM"),
        @JsonSubTypes.Type(value = BerlinFlowScenario.class, name = "EMBEDDED_FLOW_BERLIN")
})
@RequiredArgsConstructor
@Getter
public abstract class Scenario {

    protected static final String STATE = "state";
    protected static final String PERSONA_TOKEN = "persona_token";
    protected static final String REDIRECT_URI = "redirect_uri";
    private static final ObjectMapper OBJECT_MAPPER = new YoltBankBeanConfig().yoltBankObjectMapper();
    protected final String redirectUrl;
    protected final String customerAuthorizationUrl;

    @Setter
    protected String authorizationCode;

    @Setter
    protected AccessMeansDTO accessMeans;

    @SneakyThrows
    String toJson() {
        return OBJECT_MAPPER.writeValueAsString(this);
    }

    @SneakyThrows
    static Scenario fromJson(String json) {
        return OBJECT_MAPPER.readValue(json, Scenario.class);
    }

    public abstract Step createFirstStep(String stateId, String personaToken, Clock clock);

    public AccessMeansOrStepDTO processStep(UrlCreateAccessMeansRequest urlCreateAccessMeansRequest, Clock clock) {
        String redirectUrlFromSite = urlCreateAccessMeansRequest.getRedirectUrlPostedBackFromSite();
        // We want to create access means, that is, for this testbank simply the authorization code.
        // If we have an authorization code, we return it, and otherwise just redirect the user to the bank.
        // There is however, 1 special case. If we have an authorization code BUT we get the query param trigger-dynamic-flow, that means
        // we have to 'postpone' returning the authcode.
        if (queryParamIsSetWithValue("trigger-dynamic-flow", "elaborate-form", redirectUrlFromSite)) {
            final String authCode = getAuthorizationCode(urlCreateAccessMeansRequest);
            this.setAuthorizationCode(authCode);
            final FormStep formStep = new FormStep(
                    YoltTestForm.createFormWithAllComponents(),
                    EncryptionDetails.noEncryption(),
                    now(clock).plus(1, HOURS),
                    this.toJson()
            );
            return new AccessMeansOrStepDTO(formStep);
        }
        if (queryParamIsSetWithValue("trigger-dynamic-flow", "simple-form", redirectUrlFromSite)) {
            final String authCode = getAuthorizationCode(urlCreateAccessMeansRequest);
            this.setAuthorizationCode(authCode);
            final FormStep formStep = new FormStep(
                    YoltTestForm.simpleForm(),
                    EncryptionDetails.noEncryption(),
                    now(clock).plus(1, HOURS),
                    this.toJson()
            );
            return new AccessMeansOrStepDTO(formStep);
        }

        if (this.authorizationCode != null) {
            return new AccessMeansOrStepDTO(YoltProvider.createAccessMeans(urlCreateAccessMeansRequest.getUserId(), authorizationCode, clock));
        }

        // Yolt-Test-Bank also supports a double consent flow.
        if (queryParamIsSetWithValue("consent-redirect", "yes", redirectUrlFromSite)) {
            String authCode = getAuthorizationCode(urlCreateAccessMeansRequest);
            AccessMeansDTO accessMeans = YoltProvider.createAccessMeans(urlCreateAccessMeansRequest.getUserId(), authCode, clock);
            ExtraConsentScenario newScenario = new ExtraConsentScenario(this.redirectUrl, this.customerAuthorizationUrl);
            newScenario.setAccessMeans(accessMeans);
            String redirectUrl = UriComponentsBuilder.fromHttpUrl(customerAuthorizationUrl).path("/second-consent")
                    .queryParam(REDIRECT_URI, this.redirectUrl)
                    .queryParam(STATE, urlCreateAccessMeansRequest.getState())
                    .queryParam("code", authCode)
                    .toUriString();
            return new AccessMeansOrStepDTO(new RedirectStep(redirectUrl, null, newScenario.toJson()));
        }

        if (redirectUrlFromSite != null) {
            final String authCode = getAuthorizationCode(urlCreateAccessMeansRequest);
            return new AccessMeansOrStepDTO(YoltProvider.createAccessMeans(urlCreateAccessMeansRequest.getUserId(), authCode, clock));
        }

        return new AccessMeansOrStepDTO(new RedirectStep(UriComponentsBuilder.fromHttpUrl(customerAuthorizationUrl)
                .queryParam(REDIRECT_URI, redirectUrl)
                .queryParam(STATE, urlCreateAccessMeansRequest.getState())
                .toUriString(), null, this.toJson()));
    }

    protected String getAuthorizationCode(UrlCreateAccessMeansRequest urlCreateAccessMeansRequest) {
        return Optional.of(urlCreateAccessMeansRequest)
                .map(urlCreateAccessMeansRequest1 -> UriComponentsBuilder
                        .fromUriString(urlCreateAccessMeansRequest1.getRedirectUrlPostedBackFromSite())
                        .build()
                        .getQueryParams()
                        .toSingleValueMap())
                .map(p -> p.get("code"))
                .orElseGet(() -> fragmentMap(urlCreateAccessMeansRequest)
                        .map(p -> p.get("code"))
                        .orElseThrow(() -> new MissingDataException("Missing query parameter 'code'")));
    }

    private Optional<Map<String, String>> fragmentMap(UrlCreateAccessMeansRequest urlCreateAccessMeansRequest) {
        return Optional.of(urlCreateAccessMeansRequest)
                .flatMap(request -> fragmentMap(request.getRedirectUrlPostedBackFromSite()))
                .map(MultiValueMap::toSingleValueMap);
    }

    public static Optional<MultiValueMap<String, String>> fragmentMap(String url) {
        return Optional.of(url)
                .map(u -> {
                            String fragment = UriComponentsBuilder
                                    .fromUriString(u)
                                    .build()
                                    .getFragment();
                            return UriComponentsBuilder.fromUriString("https://www.example.com?" + fragment).build()
                                    .getQueryParams();
                        }
                );
    }

    private static boolean queryParamIsSetWithValue(String queryParamKey, String value, String uriString) {
        return uriString != null
                && value.equals(UriComponentsBuilder
                .fromUriString(uriString)
                .build()
                .getQueryParams()
                .toSingleValueMap()
                .get(queryParamKey));
    }
}

@Getter
class FormFirstScenario extends Scenario {
    @JsonProperty("form")
    private final ScenarioForm form;

    FormFirstScenario(@NonNull @JsonProperty("redirectUrl") String redirectUrl,
                      @NonNull @JsonProperty("customerAuthorizationUrl") String customerAuthorizationUrl,
                      @JsonProperty("form") ScenarioForm form) {
        super(redirectUrl, customerAuthorizationUrl);

        // Default to region selection form for backwards compatibility
        this.form = Optional.ofNullable(form).orElseGet(RegionSelectionForm::new);
    }

    @Override
    public Step createFirstStep(String stateId, String personaToken, Clock clock) {
        return new FormStep(
                form.buildForm(),
                EncryptionDetails.noEncryption(),
                Instant.now(clock).plus(10, ChronoUnit.MINUTES),
                this.toJson());
    }

    @Override
    public AccessMeansOrStepDTO processStep(UrlCreateAccessMeansRequest urlCreateAccessMeansRequest, Clock clock) {
        // FormFirstScenario, we either get the filled in form, or the redirect from the bank
        FilledInUserSiteFormValues filledInUserSiteFormValues = urlCreateAccessMeansRequest.getFilledInUserSiteFormValues();
        if (filledInUserSiteFormValues == null) {
            // It's the redirect, call super which handles the redirect.
            return super.processStep(urlCreateAccessMeansRequest, clock);
        }
        // It's the form, handle it.

        if (!form.isValid(filledInUserSiteFormValues)) {
            throw new RuntimeException("Submitted form is not valid");
        }

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(customerAuthorizationUrl)
                .queryParam(REDIRECT_URI, this.redirectUrl)
                .queryParam(STATE, urlCreateAccessMeansRequest.getState());

        form.extractQueryParameter(filledInUserSiteFormValues)
                .ifPresent(queryParameter -> uriComponentsBuilder.queryParam(queryParameter.getLeft(), queryParameter.getRight()));

        return new AccessMeansOrStepDTO(new RedirectStep(uriComponentsBuilder.toUriString(), null, this.toJson()));
    }

    static FormFirstScenario regionSelection(String redirectUrl, String customerAuthorizationUrl) {
        return new FormFirstScenario(redirectUrl, customerAuthorizationUrl, new RegionSelectionForm());
    }

    static FormFirstScenario iban(String redirectUrl, String customerAuthorizationUrl) {
        return new FormFirstScenario(redirectUrl, customerAuthorizationUrl, new IbanForm());
    }

    static FormFirstScenario bankSelection(String redirectUrl, String customerAuthorizationUrl) {
        return new FormFirstScenario(redirectUrl, customerAuthorizationUrl, new BankSelectionForm());
    }

    static FormFirstScenario accountTypeSelection(String redirectUrl, String customerAuthorizationUrl) {
        return new FormFirstScenario(redirectUrl, customerAuthorizationUrl, new AccountTypeSelectionForm());
    }

    static FormFirstScenario ibanAndLanguage(String redirectUrl, String customerAuthorizationUrl) {
        return new FormFirstScenario(redirectUrl, customerAuthorizationUrl, new IbanAndLanguageForm());
    }

    static FormFirstScenario branchAndAccount(String redirectUrl, String customerAuthorizationUrl) {
        return new FormFirstScenario(redirectUrl, customerAuthorizationUrl, new BranchAndAccountForm());
    }

    static FormFirstScenario ibanAndUsername(String redirectUrl, String customerAuthorizationUrl) {
        return new FormFirstScenario(redirectUrl, customerAuthorizationUrl, new IbanAndUsernameForm());
    }

    static FormFirstScenario username(String redirectUrl, String customerAuthorizationUrl) {
        return new FormFirstScenario(redirectUrl, customerAuthorizationUrl, new UsernameForm());
    }

    static FormFirstScenario email(String redirectUrl, String customerAuthorizationUrl) {
        return new FormFirstScenario(redirectUrl, customerAuthorizationUrl, new EmailForm());
    }

    static FormFirstScenario loginId(String redirectUrl, String customerAuthorizationUrl) {
        return new FormFirstScenario(redirectUrl, customerAuthorizationUrl, new LoginIdForm());
    }

    static FormFirstScenario languageSelection(String redirectUrl, String customerAuthorizationUrl) {
        return new FormFirstScenario(redirectUrl, customerAuthorizationUrl, new LanguageSelectionForm());
    }
}

class RedirectFirstScenario extends Scenario {

    RedirectFirstScenario(@NonNull @JsonProperty("redirectUrl") String redirectUrl,
                          @NonNull @JsonProperty("customerAuthorizationUrl") String customerAuthorizationUrl) {
        super(redirectUrl, customerAuthorizationUrl);
    }

    @Override
    public Step createFirstStep(String stateId, String personaToken, Clock clock) {
        return new RedirectStep(UriComponentsBuilder.fromHttpUrl(customerAuthorizationUrl)
                .queryParam(REDIRECT_URI, redirectUrl)
                .queryParam(STATE, stateId)
                .queryParam(PERSONA_TOKEN, personaToken)
                .toUriString(), null, this.toJson());
    }
}

class ExtraConsentScenario extends Scenario {

    ExtraConsentScenario(@NonNull @JsonProperty("redirectUrl") String redirectUrl,
                         @NonNull @JsonProperty("customerAuthorizationUrl") String customerAuthorizationUrl) {
        super(redirectUrl, customerAuthorizationUrl);
    }

    @Override
    public Step createFirstStep(String stateId, String personaToken, Clock clock) {
        return new RedirectStep(UriComponentsBuilder.fromHttpUrl(customerAuthorizationUrl)
                .queryParam(REDIRECT_URI, redirectUrl)
                .queryParam(STATE, stateId)
                .queryParam(PERSONA_TOKEN, personaToken)
                .toUriString(), null, this.toJson());
    }

    @Override
    public AccessMeansOrStepDTO processStep(UrlCreateAccessMeansRequest urlCreateAccessMeansRequest, Clock clock) {
        if (Objects.isNull(accessMeans)) {
            throw new GetAccessTokenFailedException("Providerstate for second confirmation is missing the accessmeans, this should be impossible.");
        }
        return new AccessMeansOrStepDTO(accessMeans);
    }
}

class EncryptedFormScenario extends Scenario {

    @JsonProperty("form")
    private final ScenarioForm form;

    @JsonProperty("privateKey")
    private String privateKey;

    EncryptedFormScenario(@NonNull @JsonProperty("redirectUrl") String redirectUrl,
                          @NonNull @JsonProperty("customerAuthorizationUrl") String customerAuthorizationUrl,
                          @JsonProperty("form") ScenarioForm form) {
        super(redirectUrl, customerAuthorizationUrl);
        this.form = form;
    }

    @Override
    @SneakyThrows(NoSuchAlgorithmException.class)
    public Step createFirstStep(String stateId, String personaToken, Clock clock) {
        Pair<EncryptionDetails, String> encryptionDetails = FormEncryption.createEncryptionDetails();
        privateKey = encryptionDetails.getRight();
        return new FormStep(
                form.buildForm(),
                encryptionDetails.getLeft(),
                Instant.now(clock).plus(10, ChronoUnit.MINUTES),
                this.toJson());
    }

    @Override
    @SneakyThrows({NoSuchAlgorithmException.class, InvalidKeySpecException.class, JoseException.class})
    public AccessMeansOrStepDTO processStep(UrlCreateAccessMeansRequest urlCreateAccessMeansRequest, Clock clock) {
        // We either get the filled in form, or some redirect url.
        FilledInUserSiteFormValues encryptedFormValues = urlCreateAccessMeansRequest.getFilledInUserSiteFormValues();
        if (encryptedFormValues == null) {
            // process the redirect url.
            return super.processStep(urlCreateAccessMeansRequest, clock);
        }
        // process the form.
        FilledInUserSiteFormValues decryptedValues = FormEncryption.decryptValues(encryptedFormValues, privateKey);
        if (!form.isValid(decryptedValues)) {
            throw new RuntimeException("Submitted form is not valid");
        }
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(customerAuthorizationUrl)
                .queryParam(REDIRECT_URI, this.redirectUrl)
                .queryParam(STATE, urlCreateAccessMeansRequest.getState());


        return new AccessMeansOrStepDTO(new RedirectStep(uriComponentsBuilder.toUriString(), null, this.toJson()));
    }


}

/**
 * This scenario has a couple of steps:
 * 1 ) bank selection
 * 2 ) encrypted username and password
 * 3 ) SCA method selection
 * 4 ) the actual SCA. There are 3 different challanges, depending on what the user chose in step 3.
 */
class BerlinFlowScenario extends Scenario {

    public static final String BERLIN_FLOW_AUTHORIZATION_CODE = "fake-auth-code";

    enum Stage {
        BANK_SELECTION,
        USERNAME_AND_PASSWORD,
        SCA_METHOD_SELECTION,
        CHALLENGE
    }

    @JsonProperty("stage")
    private Stage stage = Stage.BANK_SELECTION;
    private BankSelectionForm bankSelectionForm = new BankSelectionForm();
    @JsonProperty("privateKey")
    private String privateKey;
    @JsonProperty("encryptionDetails")
    private EncryptionDetails encryptionDetails;
    private UsernameAndPasswordForm usernameAndPasswordForm = new UsernameAndPasswordForm();
    private SCASelectionForm scaSelectionForm = new SCASelectionForm();
    @JsonProperty("selectedChallengeMethod")
    private SCASelectionForm.ChallengeMethod selectedChallengeMethod;
    @JsonProperty("selectedChallengeMethodForm")
    private ScenarioForm selectedChallengeMethodForm;

    BerlinFlowScenario(@NonNull @JsonProperty("redirectUrl") String redirectUrl,
                       @NonNull @JsonProperty("customerAuthorizationUrl") String customerAuthorizationUrl) {
        super(redirectUrl, customerAuthorizationUrl);
    }

    @Override
    public Step createFirstStep(String stateId, String personaToken, Clock clock) {
        return new FormStep(
                bankSelectionForm.buildForm(),
                EncryptionDetails.noEncryption(),
                Instant.now(clock).plus(10, ChronoUnit.MINUTES),
                this.toJson());
    }

    @SneakyThrows
    @Override
    public AccessMeansOrStepDTO processStep(UrlCreateAccessMeansRequest urlCreateAccessMeansRequest, Clock clock) {
        switch (stage) {
            case BANK_SELECTION -> {
                if (!bankSelectionForm.isValid(urlCreateAccessMeansRequest.getFilledInUserSiteFormValues())) {
                    throw new RuntimeException("Submitted bank selection form is not valid");
                }
                Pair<EncryptionDetails, String> encryptionDetailsPair = FormEncryption.createEncryptionDetails();
                privateKey = encryptionDetailsPair.getRight();
                encryptionDetails = encryptionDetailsPair.getLeft();
                stage = Stage.USERNAME_AND_PASSWORD;
                return new AccessMeansOrStepDTO(
                        new FormStep(
                                usernameAndPasswordForm.buildForm(),
                                encryptionDetails,
                                Instant.now(clock).plus(10, ChronoUnit.MINUTES),
                                this.toJson()));
            }
            case USERNAME_AND_PASSWORD -> {
                FilledInUserSiteFormValues decryptedValues = FormEncryption.decryptValues(urlCreateAccessMeansRequest.getFilledInUserSiteFormValues(), privateKey);
                if (!usernameAndPasswordForm.isValid(decryptedValues)) {
                    throw new RuntimeException("Submitted username password form is not valid");
                }
                stage = Stage.SCA_METHOD_SELECTION;
                return new AccessMeansOrStepDTO(
                        new FormStep(
                                scaSelectionForm.buildForm(),
                                EncryptionDetails.noEncryption(),
                                Instant.now(clock).plus(10, ChronoUnit.MINUTES),
                                this.toJson()));
            }
            case SCA_METHOD_SELECTION -> {
                if (!scaSelectionForm.isValid(urlCreateAccessMeansRequest.getFilledInUserSiteFormValues())) {
                    throw new RuntimeException("Submitted sca form is not valid");
                }
                selectedChallengeMethod = scaSelectionForm.getSelectedValue(urlCreateAccessMeansRequest.getFilledInUserSiteFormValues());
                selectedChallengeMethodForm = switch (selectedChallengeMethod) {
                    case SMS_OTP -> new SimpleChallengeForm("Please enter the SMS code you received by phone (this test implementation accepts any code)", "code");
                    case CHIP_OTP_FLICKER -> new FlickerChallengeForm();
                    case CHIP_OTP_SIMPLE -> new SimpleChallengeForm("Please use your chip tan generator to generate a challenge response code for challenge 1238 9124 0314 " +
                            "(this test implementation accepts any response code)", "response code");
                    case PHOTO_OTP -> new PhotoOTPChallengeForm();
                    case PUSH_OTP -> new SimpleChallengeForm("Please enter the push message you received by phone (this test implementation accepts any code)", "code");
                };

                stage = Stage.CHALLENGE;
                return new AccessMeansOrStepDTO(
                        new FormStep(
                                selectedChallengeMethodForm.buildForm(),
                                encryptionDetails,
                                Instant.now(clock).plus(10, ChronoUnit.MINUTES),
                                this.toJson()));
            }
            case CHALLENGE -> {
                if (!selectedChallengeMethodForm.isValid(urlCreateAccessMeansRequest.getFilledInUserSiteFormValues())) {
                    throw new RuntimeException("Submitted challenge form is not valid");
                }
                return new AccessMeansOrStepDTO(YoltProvider.createAccessMeans(urlCreateAccessMeansRequest.getUserId(), BERLIN_FLOW_AUTHORIZATION_CODE, clock));


            }
        }
        return super.processStep(urlCreateAccessMeansRequest, clock);
    }
}
