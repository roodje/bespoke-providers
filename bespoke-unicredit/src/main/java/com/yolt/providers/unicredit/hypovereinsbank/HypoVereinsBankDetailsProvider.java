package com.yolt.providers.unicredit.hypovereinsbank;

import com.yolt.providers.common.providerdetail.AisDetailsProvider;
import com.yolt.providers.common.providerdetail.dto.AisSiteDetails;
import com.yolt.providers.common.providerdetail.dto.LoginRequirement;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.yolt.providers.common.providerdetail.dto.AisSiteDetails.site;
import static com.yolt.providers.common.providerdetail.dto.CountryCode.DE;
import static com.yolt.providers.common.providerdetail.dto.ProviderBehaviour.STATE;
import static com.yolt.providers.common.providerdetail.dto.ProviderType.DIRECT_CONNECTION;
import static java.util.List.of;
import static nl.ing.lovebird.providerdomain.AccountType.CURRENT_ACCOUNT;
import static nl.ing.lovebird.providerdomain.ServiceType.AIS;

@Service
public class HypoVereinsBankDetailsProvider implements AisDetailsProvider {

    public static final String HYPOVEREINS_BANK_SITE_ID = "8a3befc5-78e7-4236-aac1-af91d5345139";

    private static final List<AisSiteDetails> AIS_SITE_DETAILS = List.of(
            site(HYPOVEREINS_BANK_SITE_ID, "HypoVereinsbank (UniCredit)", "HYPOVEREINSBANK", DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT), of(DE))
                    .groupingBy("HypoVereinsbank (UniCredit)")
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.REDIRECT)))
                    .loginRequirements(of(LoginRequirement.REDIRECT))
                    .build()
    );

    @Override
    public List<AisSiteDetails> getAisSiteDetails() {
        return AIS_SITE_DETAILS;
    }
}
