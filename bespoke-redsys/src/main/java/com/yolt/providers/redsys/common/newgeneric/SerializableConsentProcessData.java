package com.yolt.providers.redsys.common.newgeneric;


import com.yolt.providers.redsys.common.model.RedsysAccessMeans;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class SerializableConsentProcessData {
    private int consentStepNumber;
    private RedsysAccessMeans accessMeans;
    private String aspspName;
}
