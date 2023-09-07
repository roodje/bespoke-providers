package com.yolt.providers.cbiglobe.common.config;

import com.yolt.providers.cbiglobe.common.model.CbiGlobeAccessMeansDTO;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Size;
import java.util.List;

@Data
@RequiredArgsConstructor
@Validated
public class CbiGlobeBaseProperties {

    private String baseUrl;
    private String basePaymentsUrl;
    private String tokenUrl;
    private int transactionsPaginationLimit;
    private int frequencyPerDay;
    private int consentValidityInDays;

    @Size(min = 1)
    private List<AspspData> aspsps;

    public AspspData getAdjustedAspspData(CbiGlobeAccessMeansDTO accessMeans) {
        if (hasSingleAspsp()) {
            return getFirstAspspData();
        }
        return getSelectedAspspData(accessMeans);
    }

    public boolean hasSingleAspsp() {
        return getAspsps().size() == 1;
    }

    public AspspData getFirstAspspData() {
        return aspsps.get(0);
    }

    public AspspData getSelectedAspspData(CbiGlobeAccessMeansDTO accessMeans) {
        if (getAspsps().size() > 1) {
            for (AspspData data : getAspsps()) {
                if (data.getCode().equals(accessMeans.getBank())) {
                    return data;
                }
            }
        }
        throw new IllegalStateException("ASPSP was not selected by user");
    }
}
