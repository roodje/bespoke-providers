package com.yolt.providers.cbiglobe.nexi;

import com.yolt.providers.common.providerdetail.AisDetailsProvider;
import com.yolt.providers.common.providerdetail.dto.AisSiteDetails;
import com.yolt.providers.common.providerdetail.dto.LoginRequirement;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.yolt.providers.common.providerdetail.dto.AisSiteDetails.site;
import static com.yolt.providers.common.providerdetail.dto.CountryCode.IT;
import static com.yolt.providers.common.providerdetail.dto.ProviderBehaviour.STATE;
import static com.yolt.providers.common.providerdetail.dto.ProviderType.DIRECT_CONNECTION;
import static java.util.List.of;
import static nl.ing.lovebird.providerdomain.AccountType.CURRENT_ACCOUNT;
import static nl.ing.lovebird.providerdomain.ServiceType.AIS;

@Service
public class NexiDetailsProvider implements AisDetailsProvider {

    public static final String NEXI_SITE_ID = "be1de011-9d85-44d3-87cf-a0095b9c9c71";
    private static final String NEXI_DISPLAY_NAME = "Nexi";
    private static final String NEXI_PROVIDER_IDENTIFIER = "NEXI";

    private static final List<AisSiteDetails> AIS_SITE_DETAILS = List.of(
            site(NEXI_SITE_ID, NEXI_DISPLAY_NAME, NEXI_PROVIDER_IDENTIFIER, DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT), of(IT))
                    .groupingBy(NEXI_DISPLAY_NAME)
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.REDIRECT, LoginRequirement.FORM)))
                    .loginRequirements(of(LoginRequirement.REDIRECT, LoginRequirement.FORM))
                    .build()
    );

    @Override
    public List<AisSiteDetails> getAisSiteDetails() {
        return AIS_SITE_DETAILS;
    }
}
