package com.yolt.providers.triodosbank.nl;

import com.yolt.providers.common.providerdetail.AisDetailsProvider;
import com.yolt.providers.common.providerdetail.dto.AisSiteDetails;
import com.yolt.providers.common.providerdetail.dto.LoginRequirement;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.yolt.providers.common.providerdetail.dto.AisSiteDetails.site;
import static com.yolt.providers.common.providerdetail.dto.CountryCode.NL;
import static com.yolt.providers.common.providerdetail.dto.ProviderBehaviour.STATE;
import static com.yolt.providers.common.providerdetail.dto.ProviderType.DIRECT_CONNECTION;
import static java.util.List.of;
import static nl.ing.lovebird.providerdomain.AccountType.CURRENT_ACCOUNT;
import static nl.ing.lovebird.providerdomain.ServiceType.AIS;

@Service
public class TriodosBankDetailsProvider implements AisDetailsProvider {

    public static final String TRIODOS_BANK_SITE_ID = "8993cd94-8000-47e3-b21b-f7ec323b5340";
    private static final List<AisSiteDetails> AIS_SITE_DETAILS = List.of(
            site(TRIODOS_BANK_SITE_ID, "Triodos Bank (NL)", "TRIODOS_BANK_NL", DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT), of(NL))
                    .groupingBy("Triodos Bank (NL)")
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.REDIRECT)))
                    .loginRequirements(of(LoginRequirement.REDIRECT))
                    .build());

    @Override
    public List<AisSiteDetails> getAisSiteDetails() {
        return AIS_SITE_DETAILS;
    }
}
