package com.yolt.providers.openbanking.ais.bankofirelandgroup.bankofirelandroi.autoonboarding;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.openbanking.ais.bankofirelandgroup.common.BankOfIrelandGroupProperties;
import com.yolt.providers.openbanking.ais.bankofirelandgroup.common.service.autoonboarding.BankOfIrelandGroupAutoOnboardingService;
import com.yolt.providers.openbanking.ais.bankofirelandgroup.common.service.restclient.BankOfIrelandGroupRestClient;

import java.util.Map;
import java.util.UUID;

import static com.yolt.providers.openbanking.ais.bankofirelandgroup.bankofirelandroi.auth.BankOfIrelandRoiAuthMeansMapper.*;

public class BankOfIrelandRoiAutoOnboardingService extends BankOfIrelandGroupAutoOnboardingService {

    public BankOfIrelandRoiAutoOnboardingService(BankOfIrelandGroupRestClient restClient,
                                                 String signatureAlgorithm,
                                                 String authMethod,
                                                 BankOfIrelandGroupProperties properties) {
        super(restClient, signatureAlgorithm, authMethod, properties);
    }

    @Override
    public String getSoftwareId(Map<String, BasicAuthenticationMean> authMeans) {
        return authMeans.get(SOFTWARE_ID_NAME).getValue();
    }

    @Override
    public UUID getPrivateKeyId(Map<String, BasicAuthenticationMean> authMeans) {
        return UUID.fromString(authMeans.get(SIGNING_PRIVATE_KEY_ID_NAME).getValue());
    }

    @Override
    protected Object getSubjectDomainName(Map<String, BasicAuthenticationMean> authMeans) {
        return null;
    }

    @Override
    protected Object getSoftwareStatementAssertion(Map<String, BasicAuthenticationMean> authMeans) {
        return authMeans.get(SOFTWARE_STATEMENT_ASSERTION_NAME).getValue();
    }

    @Override
    protected String getSigningKeyHeaderId(Map<String, BasicAuthenticationMean> authMeans) {
        return authMeans.get(SIGNING_KEY_HEADER_ID_NAME).getValue();
    }

    @Override
    protected String getClientIdKey() {
        return CLIENT_ID_NAME;
    }
}