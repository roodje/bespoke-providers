package com.yolt.providers.amexgroup.amex.config;

import com.yolt.providers.common.providerdetail.AisDetailsProvider;
import com.yolt.providers.common.providerdetail.dto.AisSiteDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.yolt.providers.common.providerdetail.dto.AisSiteDetails.site;
import static com.yolt.providers.common.providerdetail.dto.CountryCode.GB;
import static com.yolt.providers.common.providerdetail.dto.LoginRequirement.REDIRECT;
import static com.yolt.providers.common.providerdetail.dto.ProviderBehaviour.STATE;
import static com.yolt.providers.common.providerdetail.dto.ProviderType.DIRECT_CONNECTION;
import static java.util.List.of;
import static nl.ing.lovebird.providerdomain.AccountType.CREDIT_CARD;
import static nl.ing.lovebird.providerdomain.ServiceType.AIS;

@Service
public class AmexProviderDetailsProvider implements AisDetailsProvider {

    public static final List<AisSiteDetails> AIS_SITE_DETAILS = List.of(
            site("75457be7-96d3-4fc1-98a2-98ded940b563", "American Express Cards", "AMEX", DIRECT_CONNECTION, of(STATE), of(CREDIT_CARD), of(GB))
                    .groupingBy("American Express Cards")
                    .usesStepTypes(Map.of(AIS, of(REDIRECT)))
                    .loginRequirements(List.of(REDIRECT))
                    .build()
    );

    @Override
    public List<AisSiteDetails> getAisSiteDetails() {
        return AIS_SITE_DETAILS;
    }
}
