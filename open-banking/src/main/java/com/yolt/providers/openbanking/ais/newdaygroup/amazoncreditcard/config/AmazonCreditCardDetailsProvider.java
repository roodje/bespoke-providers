package com.yolt.providers.openbanking.ais.newdaygroup.amazoncreditcard.config;

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
public class AmazonCreditCardDetailsProvider implements AisDetailsProvider {

    private static final String AMAZON_CREDIT_CARD_SITE_ID = "eefe603d-4a58-499f-8c33-d416aafc8883";
    private static final String AMAZON_PROVIDER_NAME = "Amazon Credit Card";
    private static final String AMAZON_PROVIDER_KEY = "AMAZON_CREDIT_CARD";

    private static final List<AisSiteDetails> AIS_SITE_DETAILS_LIST = List.of(
            site(AMAZON_CREDIT_CARD_SITE_ID, AMAZON_PROVIDER_NAME, AMAZON_PROVIDER_KEY, DIRECT_CONNECTION, of(STATE), of(CREDIT_CARD), of(GB))
                    .groupingBy(AMAZON_PROVIDER_NAME)
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.REDIRECT)))
                    .loginRequirements(of(LoginRequirement.REDIRECT))
                    .build()
    );

    @Override
    public List<AisSiteDetails> getAisSiteDetails() {
        return AIS_SITE_DETAILS_LIST;
    }
}
