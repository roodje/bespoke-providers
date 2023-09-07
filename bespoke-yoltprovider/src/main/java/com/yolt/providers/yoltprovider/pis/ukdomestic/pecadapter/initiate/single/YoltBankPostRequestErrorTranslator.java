package com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.initiate.single;

import com.yolt.providers.common.exception.PaymentValidationException;
import com.yolt.providers.common.exception.dto.DetailedErrorInformation;
import com.yolt.providers.common.exception.dto.FieldName;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YoltBankPostRequestErrorTranslator {

    private Pattern errorPattern = Pattern.compile(".*Field ([a-zA-Z0-9\\-]*?) is too long\\. Maximum ([0-9]{1,3}?) characters allowed.*");

    public void translate(String errorMessage) {
        Matcher matcher = errorPattern.matcher(errorMessage);
        if (matcher.matches()) {
            if ("EndToEndIdentification".equals(matcher.group(1))) {
                throw new PaymentValidationException(new DetailedErrorInformation(FieldName.ENDTOENDIDENTIFICATION, "^.*{1," + matcher.group(2) + "}$"));
            } else {
                return;
            }
        }
    }
}
