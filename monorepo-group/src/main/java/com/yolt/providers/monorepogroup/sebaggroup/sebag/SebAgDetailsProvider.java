package com.yolt.providers.monorepogroup.sebaggroup.sebag;

import com.yolt.providers.common.providerdetail.AisDetailsProvider;
import com.yolt.providers.common.providerdetail.dto.AisSiteDetails;
import com.yolt.providers.common.providerdetail.dto.LoginRequirement;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.yolt.providers.common.providerdetail.dto.CountryCode.DE;
import static com.yolt.providers.common.providerdetail.dto.ProviderBehaviour.STATE;
import static com.yolt.providers.common.providerdetail.dto.ProviderType.DIRECT_CONNECTION;
import static com.yolt.providers.monorepogroup.sebaggroup.sebag.SebAgBeanConfig.PROVIDER_DISPLAY_NAME;
import static com.yolt.providers.monorepogroup.sebaggroup.sebag.SebAgBeanConfig.PROVIDER_IDENTIFIER;
import static java.util.List.of;
import static nl.ing.lovebird.providerdomain.AccountType.CURRENT_ACCOUNT;
import static nl.ing.lovebird.providerdomain.AccountType.SAVINGS_ACCOUNT;
import static nl.ing.lovebird.providerdomain.ServiceType.AIS;

@Service
public class SebAgDetailsProvider implements AisDetailsProvider {

    public static final String SEB_AG_SITE_ID = "e59e2e59-efa4-4ae6-a9b8-7a3ec83be4d3";

    private static final List<AisSiteDetails> AIS_SITE_DETAILS = of(
            AisSiteDetails.site(SEB_AG_SITE_ID, PROVIDER_DISPLAY_NAME, PROVIDER_IDENTIFIER, DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT, SAVINGS_ACCOUNT), of(DE))
                    .groupingBy(PROVIDER_DISPLAY_NAME)
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.REDIRECT)))
                    .loginRequirements(of(LoginRequirement.REDIRECT))
                    .consentExpiryInDays(90)
                    .build());

    @Override
    public List<AisSiteDetails> getAisSiteDetails() {
        return AIS_SITE_DETAILS;
    }
}
