package com.yolt.providers.redsys.cajarural.consentretrieval;

import com.yolt.providers.redsys.common.newgeneric.SerializableConsentProcessData;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CajaRuralSerializableConsentProcessData extends SerializableConsentProcessData {
    String baseRedirectUrl;

    public CajaRuralSerializableConsentProcessData() {
        super(0, null, null);
    }
}
