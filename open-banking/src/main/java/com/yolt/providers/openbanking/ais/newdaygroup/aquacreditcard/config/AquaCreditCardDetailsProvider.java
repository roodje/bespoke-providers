package com.yolt.providers.openbanking.ais.newdaygroup.aquacreditcard.config;

import com.yolt.providers.common.providerdetail.AisDetailsProvider;
import com.yolt.providers.common.providerdetail.dto.AisSiteDetails;
import com.yolt.providers.common.providerdetail.dto.LoginRequirement;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.yolt.providers.common.providerdetail.dto.AisSiteDetails.site;
import static com.yolt.providers.common.providerdetail.dto.CountryCode.GB;
import static com.yolt.providers.common.providerdetail.dto.ProviderBehaviour.STATE;
import static com.yolt.providers.common.providerdetail.dto.ProviderType.DIRECT_CONNECTION;
import static java.util.List.of;
import static nl.ing.lovebird.providerdomain.AccountType.CREDIT_CARD;
import static nl.ing.lovebird.providerdomain.ServiceType.AIS;

@Service
public class AquaCreditCardDetailsProvider implements AisDetailsProvider {

    private static final String AQUA_CREDIT_CARD_SITE_ID = "c49aeca2-dc1e-45a3-8650-cfafd01f0b89";
    private static final String PROVIDER_NAME = "Aqua Credit Card";
    private static final String PROVIDER_KEY = "AQUA_CREDIT_CARD";

    private static final List<AisSiteDetails> AIS_SITE_DETAILS_LIST = List.of(
            site(AQUA_CREDIT_CARD_SITE_ID, PROVIDER_NAME, PROVIDER_KEY, DIRECT_CONNECTION, of(STATE), of(CREDIT_CARD), of(GB))
                    .groupingBy(PROVIDER_NAME)
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.REDIRECT)))
                    .loginRequirements(of(LoginRequirement.REDIRECT))
                    .build()
    );

    @Override
    public List<AisSiteDetails> getAisSiteDetails() {
        return AIS_SITE_DETAILS_LIST;
    }
}
