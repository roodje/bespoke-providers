package com.yolt.providers.stet.bnpparibasgroup.hellobank;

import com.yolt.providers.common.providerdetail.AisDetailsProvider;
import com.yolt.providers.common.providerdetail.dto.AisSiteDetails;
import com.yolt.providers.common.providerdetail.dto.LoginRequirement;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.yolt.providers.common.providerdetail.dto.AisSiteDetails.site;
import static com.yolt.providers.common.providerdetail.dto.CountryCode.FR;
import static com.yolt.providers.common.providerdetail.dto.ProviderBehaviour.STATE;
import static com.yolt.providers.common.providerdetail.dto.ProviderType.DIRECT_CONNECTION;
import static java.util.List.of;
import static nl.ing.lovebird.providerdomain.AccountType.CURRENT_ACCOUNT;
import static nl.ing.lovebird.providerdomain.ServiceType.AIS;

@Service
public class HelloBankAisDetailsProvider implements AisDetailsProvider {

    private static final String HELLO_BANK_SITE_ID = "7d1b1bff-e04b-446a-9ab0-44b456d14d42";
    private static final List<AisSiteDetails> AIS_SITE_DETAILS = List.of(
            site(HELLO_BANK_SITE_ID, "Hello bank!", "HELLO_BANK", DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT), of(FR))
                    .groupingBy("Hello bank!")
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.REDIRECT)))
                    .loginRequirements(of(LoginRequirement.REDIRECT))
                    .build()
    );

    @Override
    public List<AisSiteDetails> getAisSiteDetails() {
        return AIS_SITE_DETAILS;
    }

}
