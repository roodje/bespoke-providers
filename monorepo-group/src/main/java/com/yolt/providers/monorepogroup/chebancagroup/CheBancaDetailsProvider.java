package com.yolt.providers.monorepogroup.chebancagroup;

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
import static nl.ing.lovebird.providerdomain.AccountType.CREDIT_CARD;
import static nl.ing.lovebird.providerdomain.AccountType.CURRENT_ACCOUNT;
import static nl.ing.lovebird.providerdomain.ServiceType.AIS;

@Service
public class CheBancaDetailsProvider implements AisDetailsProvider {

    public static final String CHEBANCA_SITE_ID = "73767692-df03-4fdd-b1a6-2bc35dbed792";
    public static final String CHEBANCA_PROVIDER_KEY = "CHEBANCA";
    public static final String CHEBANCA_PROVIDER_NAME = "CheBanca!";

    private static final List<AisSiteDetails> AIS_SITE_DETAILS = List.of(
            site(CHEBANCA_SITE_ID, CHEBANCA_PROVIDER_NAME, CHEBANCA_PROVIDER_KEY, DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT, CREDIT_CARD), of(IT))
                    .groupingBy(CHEBANCA_PROVIDER_NAME)
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.REDIRECT)))
                    .loginRequirements(of(LoginRequirement.REDIRECT))
                    .build());

    @Override
    public List<AisSiteDetails> getAisSiteDetails() {
        return AIS_SITE_DETAILS;
    }
}
