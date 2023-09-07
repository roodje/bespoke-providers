package com.yolt.providers.monorepogroup.cecgroup.common.http;

import com.yolt.providers.common.constants.OAuth;
import com.yolt.providers.monorepogroup.cecgroup.common.domain.dto.consent.Access;
import com.yolt.providers.monorepogroup.cecgroup.common.domain.dto.consent.ConsentCreationRequest;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.LocalDate;

public class CecGroupHttpBodyProducerV1 implements CecGroupHttpBodyProducer {

    @Override
    public ConsentCreationRequest createConsentBody(LocalDate consentTo) {
        return ConsentCreationRequest.builder()
                .access(Access.builder()
                        .allPsd2("allAccounts")
                        .build())
                .combinedServiceIndicator(false)
                .recurringIndicator(true)
                .frequencyPerDay(4)
                .validUntil(consentTo.toString())
                .build();
    }

    @Override
    public MultiValueMap<String, Object> createTokenBody(String clientId,
                                                         String clientSecret,
                                                         String redirectUri,
                                                         String authCode) {
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);
        map.add("redirect_uri", redirectUri);
        map.add("grant_type", OAuth.CODE);
        map.add("code", authCode);
        return map;
    }

    @Override
    public MultiValueMap<String, Object> refreshTokenBody(String refreshToken) {
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("grant_type", OAuth.REFRESH_TOKEN);
        map.add("refresh_token", refreshToken);
        return map;
    }
}
