package com.yolt.providers.dkbgroup.common.auth;

import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.tracing.ExternalTracingUtil;
import com.yolt.providers.dkbgroup.common.http.DKBGroupHttpClient;
import com.yolt.providers.dkbgroup.common.model.DKBAccessMeans;
import com.yolt.providers.dkbgroup.common.model.api.*;
import com.yolt.providers.dkbgroup.common.model.authorization.ValidateCredentialsAuthRequest;
import com.yolt.providers.dkbgroup.common.model.authorization.ValidationResponse;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providershared.form.FilledInUserSiteFormValues;
import org.springframework.http.HttpHeaders;

import java.time.LocalDate;
import java.util.Collections;

import static com.yolt.providers.dkbgroup.common.DKBGroupProvider.PASSWORD_STRING;
import static com.yolt.providers.dkbgroup.common.DKBGroupProvider.USERNAME;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@RequiredArgsConstructor
public class DKBGroupAuthenticationService implements AuthenticationService {

    public static final String X_REQUEST_ID = "X-Request-ID";
    public static final String PSU_IP_ADDRESS = "PSU-IP-Address";
    public static final String PSD2_AUTHORIZATION = "PSD2-Authorization";
    public static final String BEARER = "Bearer ";
    private static final String EXAMPLE_APP_PRD_PUBLIC_IP_ADDRESS = "35.156.177.247";
    public static final int MAXIMAL_FREQUENCY_PER_DAY = 4;
    public static final LocalDate MAXIMAL_VALIDITY_DATE = LocalDate.of(9999, 12, 31);

    @Override
    public String getEmbeddedToken(final DKBGroupHttpClient httpClient,
                                   final FilledInUserSiteFormValues filledInUserSiteFormValues) throws TokenInvalidException {
        ValidateCredentialsAuthRequest body = getTokenRequestBody(filledInUserSiteFormValues);
        ValidationResponse response = httpClient.postForToken(getBasicHeaders(), body);
        return response.getAccessToken();
    }

    @Override
    public String getConsentId(final DKBGroupHttpClient httpClient,
                               final String embeddedToken,
                               final String psuIpAddress) throws TokenInvalidException {
        HttpHeaders headers = getConsentHeaders(embeddedToken, psuIpAddress);
        var body = getConsentsBodyForAllAccounts();
        ConsentsResponse201 response = httpClient.postForConsentId(headers, body);
        return response.getConsentId();
    }

    @Override
    public void register(final DKBGroupHttpClient httpClient,
                         final String providerIdentifierDisplayName) throws TokenInvalidException {
        HttpHeaders headers = getBasicHeaders();
        headers.add(PSU_IP_ADDRESS, EXAMPLE_APP_PRD_PUBLIC_IP_ADDRESS);
        var body = getConsentsBodyForAllAccounts();
        httpClient.postForConsentId(headers, body);
    }

    @Override
    public StartScaprocessResponse startSCAProcess(final DKBGroupHttpClient httpClient,
                                                   final String embeddedToken,
                                                   final String psuIpAddress,
                                                   final String consentId) throws TokenInvalidException {
        HttpHeaders headers = getConsentHeaders(embeddedToken, psuIpAddress);
        return httpClient.postForAuthorisationId(headers, consentId);
    }

    @Override
    public String chooseSCAMethod(final DKBGroupHttpClient httpClient,
                                  final String embeddedToken,
                                  final String psuIpAddress,
                                  final String consentId,
                                  final StartScaprocessResponse startScaprocessResponse) throws TokenInvalidException {
        HttpHeaders headers = getConsentHeaders(embeddedToken, psuIpAddress);
        SelectPsuAuthenticationMethod body = createBodyWithExtractedAuthenticationMethodId(startScaprocessResponse);
        SelectPsuAuthenticationMethodResponse response = httpClient.chooseSCAMethod(headers, body, consentId, startScaprocessResponse.getAuthorisationId());
        if (ScaStatus.SCAMETHODSELECTED.equals(response.getScaStatus())) {
            return response.getChosenScaMethod().getExplanation();
        }
        throw new GetAccessTokenFailedException("SCA method selection failed.");
    }

    @Override
    public void authoriseConsent(final DKBGroupHttpClient httpClient,
                                 final String otp,
                                 final String psuIpAddress,
                                 final DKBAccessMeans accessMeans) throws TokenInvalidException {
        HttpHeaders headers = getConsentHeaders(accessMeans.getEmbeddedToken(), psuIpAddress);
        var body = new AuthorisationConfirmation();
        body.setConfirmationCode(otp);
        AuthorisationConfirmationResponse response = httpClient.authoriseConsent(headers, body, accessMeans.getConsentId(), accessMeans.getAuthorisationId());
        if (!ScaStatusAuthorisationConfirmation.FINALISED.equals(response.getScaStatus())) {
            throw new GetAccessTokenFailedException("Consent authorisation failed.");
        }
    }

    private SelectPsuAuthenticationMethod createBodyWithExtractedAuthenticationMethodId(final StartScaprocessResponse startScaprocessResponse) {
        String authenticationMethodId = startScaprocessResponse.getScaMethods().stream()
                .filter(authenticationObject -> AuthenticationType.PUSH_OTP.equals(authenticationObject.getAuthenticationType()))
                .map(AuthenticationObject::getAuthenticationMethodId)
                .findAny()
                .orElse(startScaprocessResponse.getScaMethods().get(0).getAuthenticationMethodId());
        var body = new SelectPsuAuthenticationMethod();
        body.setAuthenticationMethodId(authenticationMethodId);
        return body;
    }

    private Consents getConsentsBodyForAllAccounts() {
        var consents = new Consents();
        var access = new AccountAccess();
        access.setAllPsd2(AccountAccess.AllPsd2Enum.ALLACCOUNTS);
        consents.setAccess(access);
        consents.setRecurringIndicator(true);
        consents.setValidUntil(MAXIMAL_VALIDITY_DATE);
        consents.setFrequencyPerDay(MAXIMAL_FREQUENCY_PER_DAY);
        consents.setCombinedServiceIndicator(false);
        return consents;
    }

    private HttpHeaders getConsentHeaders(String token, String ipAddress) {
        HttpHeaders headers = getBasicHeaders();
        headers.add(PSU_IP_ADDRESS, ipAddress);
        headers.add(PSD2_AUTHORIZATION, BEARER + token);
        return headers;
    }

    private HttpHeaders getBasicHeaders() {
        var headers = new HttpHeaders();
        headers.add(X_REQUEST_ID, ExternalTracingUtil.createLastExternalTraceId());
        headers.setAccept(Collections.singletonList(APPLICATION_JSON));
        headers.setContentType(APPLICATION_JSON);
        return headers;
    }

    private ValidateCredentialsAuthRequest getTokenRequestBody(final FilledInUserSiteFormValues filledInUserSiteFormValues) {
        var body = new ValidateCredentialsAuthRequest();
        body.setUsername(filledInUserSiteFormValues.get(USERNAME));
        body.setPassword(filledInUserSiteFormValues.get(PASSWORD_STRING));
        return body;
    }
}
