package com.yolt.providers.redsys.cajarural.consentretrieval;

import com.yolt.providers.common.ais.form.EncryptionDetails;
import com.yolt.providers.common.constants.OAuth;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.FormStep;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.redsys.cajarural.CajaRuralProperties;
import com.yolt.providers.redsys.cajarural.RuralBank;
import com.yolt.providers.redsys.common.dto.ResponseGetConsent;
import com.yolt.providers.redsys.common.model.RedsysAccessMeans;
import com.yolt.providers.redsys.common.model.Token;
import com.yolt.providers.redsys.common.newgeneric.ConsentProcess;
import com.yolt.providers.redsys.common.newgeneric.ResdysGenericStepDataProvider;
import com.yolt.providers.redsys.common.newgeneric.service.RedsysAuthorizationServiceV2;
import com.yolt.providers.redsys.common.util.RedsysPKCE;
import com.yolt.securityutils.oauth2.OAuth2ProofKeyCodeExchange;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import nl.ing.lovebird.providershared.form.Form;
import nl.ing.lovebird.providershared.form.SelectField;
import nl.ing.lovebird.providershared.form.SelectOptionValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class CajaRuralConsentRetrievalProcess extends ConsentProcess<CajaRuralSerializableConsentProcessData> {
    public static final String REGION_FIELD = "region";
    private static final String AUTHORIZE_ENDPOINT = "/authorize";
    public static final String REGION_SELECT_FIELD_DISPLAY_NAME = "Région";
    public static final String CODE_CHALLENGE = "code_challenge";
    public static final String CODE_CHALLENGE_METHOD = "code_challenge_method";

    public CajaRuralConsentRetrievalProcess(CajaRuralConsentProcessArgumentsMapper argumentsMapper,
                                            RedsysAuthorizationServiceV2 authorizationService,
                                            CajaRuralProperties properties,
                                            Clock clock) {
        super(List.of(processArguments -> {
                    var selectField = new SelectField(REGION_FIELD, REGION_SELECT_FIELD_DISPLAY_NAME, 0, 0, false, false);
                    Arrays.stream(RuralBank.values())
                            .forEach(bank -> selectField.addSelectOptionValue(new SelectOptionValue(bank.name(), bank.getDisplayName())));
                    final var selectForm = new Form(Collections.singletonList(selectField), null, null);
                    return new AccessMeansOrStepDTO(
                            new FormStep(selectForm, EncryptionDetails.noEncryption(), Instant.now(clock).plus(ResdysGenericStepDataProvider.FORM_STEP_EXPIRY_DURATION),
                                    argumentsMapper.serializeState(processArguments)));
                },

                processArguments -> {
                    final OAuth2ProofKeyCodeExchange oAuth2ProofKeyCodeExchange = RedsysPKCE.createRandomS256();

                    MultiValueMap<String, String> varMap = new LinkedMultiValueMap<>();
                    varMap.add(OAuth.RESPONSE_TYPE, "code");
                    varMap.add(OAuth.SCOPE, "AIS");
                    varMap.add(OAuth.STATE, processArguments.getState());
                    varMap.add(OAuth.CLIENT_ID, processArguments.getAuthenticationMeans().getClientId());
                    varMap.add(CODE_CHALLENGE, oAuth2ProofKeyCodeExchange.getCodeChallenge());
                    varMap.add(CODE_CHALLENGE_METHOD, oAuth2ProofKeyCodeExchange.getCodeChallengeMethod());
                    String encodedRedirectUrl;
                    try {
                        encodedRedirectUrl = URLEncoder.encode(processArguments.getBaseClientRedirectUrl(), StandardCharsets.UTF_8.toString());
                    } catch (UnsupportedEncodingException e) {
                        throw new IllegalStateException("Could not encode redirect URL");
                    }
                    varMap.add(OAuth.REDIRECT_URI, encodedRedirectUrl);

                    CajaRuralSerializableConsentProcessData processData = processArguments.getConsentProcessData();
                    String authorizationUrl = UriComponentsBuilder.fromHttpUrl(
                            properties.getAuthorizationUrl() + '/' + processData.getAspspName() + AUTHORIZE_ENDPOINT)
                            .queryParams(varMap)
                            .build()
                            .toString();

                    processData.setAccessMeans(new RedsysAccessMeans(oAuth2ProofKeyCodeExchange.getCodeVerifier()));
                    String providerState = argumentsMapper.serializeState(processArguments);

                    return new AccessMeansOrStepDTO(new RedirectStep(authorizationUrl, null, providerState));
                },

                processArguments -> {
                    CajaRuralSerializableConsentProcessData processData = processArguments.getConsentProcessData();
                    String redirectUrl = processArguments.getRedirectUriPostedBackFromSite();
                    int queryParamStartIndex = redirectUrl.indexOf('?');
                    redirectUrl = redirectUrl.substring(0, queryParamStartIndex);

                    String redirectUrlWithState = UriComponentsBuilder.fromUriString(redirectUrl)
                            .queryParam("state", processArguments.getState())
                            .toUriString();

                    LocalDate consentValidUntilDate = LocalDate.now(clock).plusDays(90);
                    Token accessToken = authorizationService.createAccessToken(processArguments);

                    ResponseGetConsent consent = authorizationService.getConsentId(
                            processArguments,
                            accessToken.getAccessToken(),
                            consentValidUntilDate,
                            redirectUrlWithState);

                    processData.setAccessMeans(new RedsysAccessMeans(
                            accessToken,
                            redirectUrl,
                            consent.getConsentId(),
                            processData.getAccessMeans().getCodeVerifier(),
                            Instant.now(clock),
                            processData.getAccessMeans().getFormValues()
                    ));
                    String providerState = argumentsMapper.serializeState(processArguments);

                    if (consent == null || consent.getLinks() == null || consent.getLinks().getScaRedirect() == null || consent.getLinks().getScaRedirect().getHref() == null) {
                        throw new IllegalStateException("Consent response or SCA redirect link is empty.");
                    }

                    String confirmConsent = consent.getLinks().getScaRedirect().getHref();
                    return new AccessMeansOrStepDTO(new RedirectStep(confirmConsent, null, providerState));
                },

                processArguments -> new AccessMeansOrStepDTO(
                        new AccessMeansDTO(
                                processArguments.getUserId(),
                                argumentsMapper.serializeState(processArguments),
                                new Date(),
                                Date.from(Instant.now(clock).plusSeconds(
                                        processArguments.getConsentProcessData().getAccessMeans().getToken().getExpiresIn()))
                        )
                )
                )
        );
    }
}
