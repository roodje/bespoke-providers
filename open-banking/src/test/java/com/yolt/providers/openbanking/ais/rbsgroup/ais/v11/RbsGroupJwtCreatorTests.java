package com.yolt.providers.openbanking.ais.rbsgroup.ais.v11;

import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequest;
import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequestBuilder;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.openbanking.ais.generic2.common.OpenBankingTokenScope;
import com.yolt.providers.openbanking.ais.rbsgroup.RbsSampleAuthenticationMeansV4;
import com.yolt.providers.openbanking.ais.rbsgroup.common.service.autoonboarding.JwtCreator;
import com.yolt.providers.openbanking.ais.rbsgroup.common.service.autoonboarding.RbsGroupDynamicRegistrationArguments;
import nl.ing.lovebird.providerdomain.TokenScope;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.yolt.providers.openbanking.ais.rbsgroup.common.auth.RbsGroupAuthMeansBuilderV4.*;
import static org.assertj.core.api.Assertions.assertThat;

public class RbsGroupJwtCreatorTests {

    private static final String SOFTWARE_ID = "software_id";
    private static final String SCOPE = "scope";
    private static Map<String, BasicAuthenticationMean> authenticationMeans;

    private JwtCreator subject = new JwtCreator();

    @BeforeAll
    static void beforeAll() throws Exception {
        authenticationMeans = RbsSampleAuthenticationMeansV4.getRbsSampleAuthenticationMeansForAis();
    }


    @Test
    public void shouldReturnJwsWithEncoding() throws MalformedClaimException {
        String expectedAudience = "test";
        String expectedIssuerAndSoftwareId = "someSoftwareId";
        String expectedScope = "openid accounts payments";

        //given
        UrlAutoOnboardingRequest autoOnboardingRequest = new UrlAutoOnboardingRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRedirectUrls(Collections.singletonList("http://fake-redirect.com"))
                .setScopes(Set.of(TokenScope.ACCOUNTS, TokenScope.PAYMENTS))
                .build();

        RbsGroupDynamicRegistrationArguments registrationArguments = createRegistrationArguments(autoOnboardingRequest);

        //when
        JwtClaims createdClaims = subject.prepareJwt(registrationArguments);

        //then
        assertThat(createdClaims.getAudience()).hasSize(1);
        assertThat(createdClaims.getAudience().get(0)).isEqualTo(expectedAudience);
        assertThat(createdClaims.getIssuer()).isEqualTo(expectedIssuerAndSoftwareId);
        assertThat(createdClaims.getClaimValue(SOFTWARE_ID)).isEqualTo(expectedIssuerAndSoftwareId);
        assertThat(createdClaims.getClaimValue(SCOPE)).isEqualTo(expectedScope);
    }

    RbsGroupDynamicRegistrationArguments createRegistrationArguments(UrlAutoOnboardingRequest autoOnboardingRequest) {
        Map<String, BasicAuthenticationMean> authenticationMeans = autoOnboardingRequest.getAuthenticationMeans();
        RbsGroupDynamicRegistrationArguments registrationArguments = new RbsGroupDynamicRegistrationArguments();
        registrationArguments.setSigner(autoOnboardingRequest.getSigner());
        registrationArguments.setPrivateSigningKeyId(UUID.fromString(authenticationMeans.get(SIGNING_PRIVATE_KEY_ID_NAME).getValue()));
        registrationArguments.setSigningKeyHeaderId(authenticationMeans.get(SIGNING_KEY_HEADER_ID_NAME).getValue());
        registrationArguments.setSoftwareId(authenticationMeans.get(SOFTWARE_ID_NAME).getValue());
        registrationArguments.setInstitutionId(authenticationMeans.get(INSTITUTION_ID_NAME).getValue());
        registrationArguments.setOrganizationId(authenticationMeans.get(ORGANIZATION_ID_NAME).getValue());
        registrationArguments.setRedirectUris(Collections.singletonList(autoOnboardingRequest.getBaseClientRedirectUrl()));
        registrationArguments.setSoftwareStatementAssertion(authenticationMeans.get(SOFTWARE_STATEMENT_ASSERTION_NAME).getValue());

        OpenBankingTokenScope tokenScope = OpenBankingTokenScope.getByTokenScopes(autoOnboardingRequest.getScopes());
        registrationArguments.setScope(tokenScope.getRegistrationScope());
        return registrationArguments;
    }
}
