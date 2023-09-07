package com.yolt.providers.openbanking.ais.bankofirelandgroup.autoonboarding;

import com.yolt.providers.openbanking.ais.bankofirelandgroup.bankofireland.autoonboarding.BankOfIrelandAutoOnboardingService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class SubjectDomainNameTest {
    private BankOfIrelandAutoOnboardingService autoOnboardingService = new BankOfIrelandAutoOnboardingService(null, "RSA256", null, null);

    @Test
    public void shouldReturnModifiedSubjectDomainName() {
        //given
        String tls_client_auth_subject_dn = "C=GB,O=SuperClient Limited,2.5.4.97=PSDGB-FCA-916866,CN=any";
        //when
        String formattedSubjectDomainName = autoOnboardingService.formatSubjectDomainName(tls_client_auth_subject_dn);
        //then
        assertThat(formattedSubjectDomainName).isEqualTo("CN=any,OID.2.5.4.97=PSDGB-FCA-916866,O=SuperClient Limited,C=GB");
    }
}
