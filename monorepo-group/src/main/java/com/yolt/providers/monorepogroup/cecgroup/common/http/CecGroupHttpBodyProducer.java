package com.yolt.providers.monorepogroup.cecgroup.common.http;

import com.yolt.providers.monorepogroup.cecgroup.common.domain.dto.consent.ConsentCreationRequest;
import org.springframework.util.MultiValueMap;

import java.time.LocalDate;

public interface CecGroupHttpBodyProducer {

    ConsentCreationRequest createConsentBody(LocalDate consentTo);

    MultiValueMap<String, Object> createTokenBody(String clientId,
                                                  String clientSecret,
                                                  String redirectUri,
                                                  String authCode);

    MultiValueMap<String, Object> refreshTokenBody(String refreshToken);
}
