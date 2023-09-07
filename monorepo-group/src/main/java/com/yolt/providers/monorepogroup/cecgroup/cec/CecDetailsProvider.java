package com.yolt.providers.monorepogroup.cecgroup.cec;

import com.yolt.providers.common.providerdetail.AisDetailsProvider;
import com.yolt.providers.common.providerdetail.dto.AisSiteDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.yolt.providers.common.providerdetail.dto.AisSiteDetails.site;
import static com.yolt.providers.common.providerdetail.dto.CountryCode.RO;
import static com.yolt.providers.common.providerdetail.dto.LoginRequirement.REDIRECT;
import static com.yolt.providers.common.providerdetail.dto.ProviderBehaviour.STATE;
import static com.yolt.providers.common.providerdetail.dto.ProviderType.DIRECT_CONNECTION;
import static java.util.List.of;
import static nl.ing.lovebird.providerdomain.AccountType.CURRENT_ACCOUNT;
import static nl.ing.lovebird.providerdomain.ServiceType.AIS;

@Service
public class CecDetailsProvider implements AisDetailsProvider {

    public static final String CEC_SITE_ID = "7eb47e4e-91a0-4737-a74b-b68fc2b679fa";
    public static final String CEC_PROVIDER_KEY = "CEC";
    public static final String CEC_PROVIDER_NAME = "CEC";

    private static final List<AisSiteDetails> AIS_SITE_DETAILS = List.of(
            site(CEC_SITE_ID, CEC_PROVIDER_NAME, CEC_PROVIDER_KEY, DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT), of(RO))
                    .groupingBy(CEC_PROVIDER_NAME)
                    .usesStepTypes(Map.of(AIS, of(REDIRECT)))
                    .loginRequirements(of(REDIRECT))
                    .consentExpiryInDays(89)
                    .build());

    @Override
    public List<AisSiteDetails> getAisSiteDetails() {
        return AIS_SITE_DETAILS;
    }
}
