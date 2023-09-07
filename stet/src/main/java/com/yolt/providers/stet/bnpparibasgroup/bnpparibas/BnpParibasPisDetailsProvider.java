package com.yolt.providers.stet.bnpparibasgroup.bnpparibas;

import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.providerdetail.PisDetailsProvider;
import com.yolt.providers.common.providerdetail.dto.DynamicFieldOptions;
import com.yolt.providers.common.providerdetail.dto.LoginRequirement;
import com.yolt.providers.common.providerdetail.dto.PaymentMethod;
import com.yolt.providers.common.providerdetail.dto.PisSiteDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.yolt.providers.common.providerdetail.dto.DynamicFieldNames.CREDITOR_AGENT_BIC;
import static com.yolt.providers.common.providerdetail.dto.DynamicFieldNames.CREDITOR_AGENT_NAME;
import static java.util.List.of;

@Service
public class BnpParibasPisDetailsProvider implements PisDetailsProvider {

    private static final List<PisSiteDetails> PIS_SITE_DETAILS = List.of(
            PisSiteDetails.builder()
                    .id(UUID.fromString("1c270f38-ce5d-4b23-876f-fa73574d26ba"))
                    .providerKey("BNP_PARIBAS")
                    .supported(true)
                    .paymentType(PaymentType.SINGLE)
                    .dynamicFields(Map.of(CREDITOR_AGENT_BIC, new DynamicFieldOptions(true),
                            CREDITOR_AGENT_NAME, new DynamicFieldOptions(true)))
                    .requiresSubmitStep(true)
                    .paymentMethod(PaymentMethod.SEPA)
                    .loginRequirements(of(LoginRequirement.REDIRECT))
                    .build()
    );

    @Override
    public List<PisSiteDetails> getPisSiteDetails() {
        return PIS_SITE_DETAILS;
    }
}
