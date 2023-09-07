package com.yolt.providers.openbanking.ais.bankofirelandgroup.bankofireland.autoonboarding;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.util.KeyUtil;
import com.yolt.providers.openbanking.ais.bankofirelandgroup.bankofireland.BankOfIrelandProperties;
import com.yolt.providers.openbanking.ais.bankofirelandgroup.common.service.autoonboarding.BankOfIrelandGroupAutoOnboardingService;
import com.yolt.providers.openbanking.ais.bankofirelandgroup.common.service.restclient.BankOfIrelandGroupRestClient;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.yolt.providers.openbanking.ais.bankofirelandgroup.bankofireland.auth.BankOfIrelandAuthMeansMapper.*;

public class BankOfIrelandAutoOnboardingService extends BankOfIrelandGroupAutoOnboardingService {

    public BankOfIrelandAutoOnboardingService(BankOfIrelandGroupRestClient restClient, String signatureAlgorithm, String authMethod, BankOfIrelandProperties properties) {
        super(restClient, signatureAlgorithm, authMethod, properties);
    }

    @Override
    public String getSoftwareId(Map<String, BasicAuthenticationMean> authMeans) {
        return authMeans.get(SOFTWARE_ID_NAME_V2).getValue();
    }

    @Override
    public UUID getPrivateKeyId(Map<String, BasicAuthenticationMean> authMeans) {
        return UUID.fromString(authMeans.get(SIGNING_PRIVATE_KEY_ID_NAME_V2).getValue());
    }

    @Override
    protected Object getSubjectDomainName(Map<String, BasicAuthenticationMean> authMeans) throws CertificateException {
        X509Certificate x509Certificate = KeyUtil.createCertificateFromPemFormat(authMeans.get(TRANSPORT_CERTIFICATE_NAME_V2).getValue());
        return formatSubjectDomainName(x509Certificate.getSubjectDN().getName());
    }

    @Override
    protected Object getSoftwareStatementAssertion(Map<String, BasicAuthenticationMean> authMeans) {
        return authMeans.get(SOFTWARE_STATEMENT_ASSERTION_NAME_V2).getValue();
    }

    @Override
    protected String getSigningKeyHeaderId(Map<String, BasicAuthenticationMean> authMeans) {
        return authMeans.get(SIGNING_KEY_HEADER_ID_NAME_V2).getValue();
    }

    @Override
    protected String getClientIdKey() {
        return CLIENT_ID_NAME_V2;
    }

    public String formatSubjectDomainName(String subjectDomainName) {
        var a = Arrays.stream(subjectDomainName.split(","))
                .map(this::addOID)
                .collect(Collectors.toCollection(LinkedList::new))
                .descendingIterator();
        StringBuilder stringBuilder = new StringBuilder();
        while (a.hasNext()) {
            stringBuilder.append(a.next());
            if (a.hasNext()) {
                stringBuilder.append(',');
            }
        }
        return stringBuilder.toString();
    }

    private String addOID(String rdn) {
        String[] pair = rdn.split("=");

        if (pair.length != 2)
            return rdn;

        if (pair[0].matches("^([0-9]*\\.)*[0-9]*$")) {
            return "OID." + pair[0] + '=' + pair[1];
        }

        return rdn;
    }
}