package com.yolt.providers.openbanking.ais.rbsgroup.common.service.autoonboarding;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.dto.AutoOnboardingResponse;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.service.AuthenticationService;
import com.yolt.providers.openbanking.ais.rbsgroup.common.properties.RbsGroupPropertiesV2;
import com.yolt.providers.openbanking.ais.rbsgroup.common.service.restclient.RbsGroupRestClientV5;
import lombok.RequiredArgsConstructor;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class RbsGroupAutoonboardingServiceV2 {

    private static final String TYP = "typ";
    private static final String JWT = "JWT";
    private static final String CLIENT_ID_URL = "/{client-id}";

    private final RbsGroupRestClientV5 restClient;
    private final RbsGroupPropertiesV2 properties;
    private final JwtCreator jwtCreator;
    private final AuthenticationService authenticationService;

    public Optional<AutoOnboardingResponse> register(HttpClient httpClient,
                                                     RbsGroupDynamicRegistrationArguments registrationArguments,
                                                     DefaultAuthMeans authMeans,
                                                     TokenScope tokenScope) throws TokenInvalidException {
        JsonWebSignature jws = createJws(registrationArguments);
        UUID signingKeyId = registrationArguments.getPrivateSigningKeyId();
        String payload = registrationArguments.getSigner().sign(jws, signingKeyId, registrationArguments.getSigningAlgorytm()).getCompactSerialization();
        if (StringUtils.isEmpty(authMeans.getClientId())) {
            return restClient.register(httpClient, payload, properties.getRegistrationUrl());
        }
        AccessMeans accessMeans = authenticationService.getClientAccessTokenWithoutCache(httpClient, authMeans, tokenScope, registrationArguments.getSigner());
        String updateUrl = properties.getRegistrationUrl() + CLIENT_ID_URL;
        return restClient.updateRegistration(httpClient, payload, accessMeans, updateUrl, authMeans.getClientId());

    }

    private JsonWebSignature createJws(RbsGroupDynamicRegistrationArguments registrationArguments) {
        JsonWebSignature jws = new JsonWebSignature();
        jws.setPayload(jwtCreator.prepareJwt(registrationArguments).toJson());
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_PSS_USING_SHA256);
        jws.setKeyIdHeaderValue(registrationArguments.getSigningKeyHeaderId());
        jws.setHeader(TYP, JWT);
        return jws;

    }
}
