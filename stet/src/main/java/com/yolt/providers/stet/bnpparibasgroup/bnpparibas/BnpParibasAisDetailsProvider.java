package com.yolt.providers.stet.bnpparibasgroup.bnpparibas;

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
import static nl.ing.lovebird.providerdomain.ServiceType.PIS;

@Service
public class BnpParibasAisDetailsProvider implements AisDetailsProvider {

    private static final String BNP_SITE_ID = "1c270f38-ce5d-4b23-876f-fa73574d26ba";
    private static final List<AisSiteDetails> AIS_SITE_DETAILS = List.of(
            site(BNP_SITE_ID, "BNP Paribas", "BNP_PARIBAS", DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT), of(FR))
                    .groupingBy("BNP Paribas")
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.REDIRECT), PIS, of(LoginRequirement.REDIRECT)))
                    .loginRequirements(of(LoginRequirement.REDIRECT))
                    .build()
    );

    @Override
    public List<AisSiteDetails> getAisSiteDetails() {
        return AIS_SITE_DETAILS;
    }
}
