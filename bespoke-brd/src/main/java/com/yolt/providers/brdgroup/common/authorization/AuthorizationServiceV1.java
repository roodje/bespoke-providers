package com.yolt.providers.brdgroup.common.authorization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.brdgroup.common.BrdGroupAccessMeans;
import com.yolt.providers.brdgroup.common.dto.consent.Access;
import com.yolt.providers.brdgroup.common.dto.consent.CreateConsentRequest;
import com.yolt.providers.brdgroup.common.dto.consent.CreateConsentResponse;
import com.yolt.providers.brdgroup.common.http.BrdGroupHttpClient;
import com.yolt.providers.brdgroup.common.util.BrdGroupDateConverter;
import com.yolt.providers.common.ais.form.EncryptionDetails;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.FormStep;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import nl.ing.lovebird.providershared.form.Form;
import nl.ing.lovebird.providershared.form.TextField;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.UUID;

import static java.time.temporal.ChronoUnit.HOURS;

@RequiredArgsConstructor
public class AuthorizationServiceV1 implements AuthorizationService {

    private static final String LOGIN_ID_FIELD_ID = "LoginID";
    private static final String LOGIN_ID_FIELD_NAME = "LoginID of the client, used in MyBRD applications";
    private static final String LOGIN_ID_REGEX = "^.+$";

    private final BrdGroupConsentStatusValidator consentStatusValidator;
    private final Clock clock;
    private final BrdGroupDateConverter dateConverter;
    private final ObjectMapper objectMapper;

    @Override
    public FormStep generateLoginIdForm() {
        TextField loginIdField = new TextField(LOGIN_ID_FIELD_ID, LOGIN_ID_FIELD_NAME, 0, 255,
                false, LOGIN_ID_REGEX, false);

        Form form = new Form();
        form.setFormComponents(Collections.singletonList(loginIdField));
        return new FormStep(form, EncryptionDetails.noEncryption(), Instant.now(clock).plus(1L, HOURS), null);
    }

    @Override
    public AccessMeansOrStepDTO createAccessMeans(BrdGroupHttpClient httpClient, String psuIpAddress, String loginId, UUID userId) {
        CreateConsentResponse createConsentResponse = createConsent(httpClient, psuIpAddress, loginId);
        String consentId = createConsentResponse.getConsentId();
        if (!StringUtils.hasText(consentId)) {
            throw new GetAccessTokenFailedException("Error creating consent - consentId not present");
        }
        consentStatusValidator.validate(httpClient, consentId);
        return new AccessMeansOrStepDTO(new AccessMeansDTO(userId,
                toAccessMeans(consentId),
                dateConverter.toDate(LocalDate.now(clock)),
                dateConverter.toDate(LocalDate.now(clock).plusDays(89))
        ));
    }

    @Override
    public void deleteConsent(BrdGroupHttpClient httpClient, String consentId) {
        httpClient.deleteConsent(consentId);
    }

    private CreateConsentResponse createConsent(BrdGroupHttpClient httpClient, String psuIpAddress, String loginId) {
        CreateConsentRequest request = CreateConsentRequest.builder()
                .access(Access.builder()
                        .allPsd2("allAccounts")
                        .build())
                .recurringIndicator(true)
                .validUntil(LocalDate.now(clock).plusDays(89).toString())
                .frequencyPerDay(4)
                .combinedServiceIndicator(false)
                .build();

        return httpClient.postConsentCreation(request, psuIpAddress, loginId);
    }

    private String toAccessMeans(String consentId) {
        try {
            return objectMapper.writeValueAsString(new BrdGroupAccessMeans(consentId));
        } catch (JsonProcessingException e) {
            throw new GetAccessTokenFailedException("Error converting Consent to AccessMeans");
        }
    }
}
