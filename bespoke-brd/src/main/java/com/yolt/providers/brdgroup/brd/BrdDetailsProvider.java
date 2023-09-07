package com.yolt.providers.brdgroup.brd;

import com.yolt.providers.common.providerdetail.AisDetailsProvider;
import com.yolt.providers.common.providerdetail.dto.AisSiteDetails;
import com.yolt.providers.common.providerdetail.dto.LoginRequirement;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.yolt.providers.common.providerdetail.dto.AisSiteDetails.site;
import static com.yolt.providers.common.providerdetail.dto.CountryCode.RO;
import static com.yolt.providers.common.providerdetail.dto.ProviderBehaviour.STATE;
import static com.yolt.providers.common.providerdetail.dto.ProviderType.DIRECT_CONNECTION;
import static java.util.List.of;
import static nl.ing.lovebird.providerdomain.AccountType.CURRENT_ACCOUNT;
import static nl.ing.lovebird.providerdomain.ServiceType.AIS;

@Service
public class BrdDetailsProvider implements AisDetailsProvider {

    public static final String BRD_SITE_ID = "a4c3dd27-62e2-4fa8-9996-309708964c44";
    public static final String BRD_PROVIDER_KEY = "BRD";
    public static final String BRD_PROVIDER_NAME = "Banca Romana pentru Dezvoltare";
    private static final List<AisSiteDetails> AIS_SITE_DETAILS = List.of(
            site(BRD_SITE_ID, BRD_PROVIDER_NAME, BRD_PROVIDER_KEY, DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT), of(RO))
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.FORM)))
                    .loginRequirements(of(LoginRequirement.FORM))
                    .build());

    @Override
    public List<AisSiteDetails> getAisSiteDetails() {
        return AIS_SITE_DETAILS;
    }
}
