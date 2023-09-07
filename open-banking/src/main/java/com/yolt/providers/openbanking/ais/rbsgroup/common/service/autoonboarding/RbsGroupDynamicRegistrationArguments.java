package com.yolt.providers.openbanking.ais.rbsgroup.common.service.autoonboarding;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
public class RbsGroupDynamicRegistrationArguments {

    private SignatureAlgorithm signingAlgorytm = SignatureAlgorithm.SHA256_WITH_RSA_PSS;
    private String tokenEndpointAuthMethod = "private_key_jwt";
    private List<String> grantTypeList = List.of("client_credentials", "authorization_code", "refresh_token");
    private List<String> responseTypesList = Collections.singletonList("code id_token");
    private String scope;
    private String applicationType = "web";
    private SignatureAlgorithm idTokenSignedResponseAlgorithm = SignatureAlgorithm.SHA256_WITH_RSA_PSS;
    private SignatureAlgorithm requestObjectSigningAlgorithm = SignatureAlgorithm.SHA256_WITH_RSA_PSS;
    private SignatureAlgorithm tokenEndpointAuthSigningAlgorithm = SignatureAlgorithm.SHA256_WITH_RSA_PSS;
    private List<String> redirectUris;
    private Signer signer;
    private UUID privateSigningKeyId;
    private String signingKeyHeaderId;
    private String softwareId;
    private String institutionId;
    private String organizationId;
    private String softwareStatementAssertion;
}
