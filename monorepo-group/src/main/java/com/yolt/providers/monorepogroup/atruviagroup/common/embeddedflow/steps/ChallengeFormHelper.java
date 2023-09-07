package com.yolt.providers.monorepogroup.atruviagroup.common.embeddedflow.steps;

import com.yolt.providers.monorepogroup.atruviagroup.common.dto.external.AuthenticationObject;
import com.yolt.providers.monorepogroup.atruviagroup.common.dto.external.ChallengeData;
import lombok.experimental.UtilityClass;
import nl.ing.lovebird.providershared.form.*;
import org.jose4j.base64url.Base64;

import java.util.List;

import static com.yolt.providers.monorepogroup.atruviagroup.common.embeddedflow.steps.Constants.*;
import static java.util.Collections.singletonList;

@UtilityClass
class ChallengeFormHelper {
    static Form mapChallengeDataToForm(ChallengeData challengeData, AuthenticationObject authenticationObject) {
        var explanationField = new ExplanationField("challengeMethodExplanationId", "Explanation provided by ASPSP", challengeData.getAdditionalInformation());
        return switch (authenticationObject.getAuthenticationType()) {
            case SMS_OTP, PUSH_OTP -> getGeneralTextFieldChallengeForm(authenticationObject, explanationField);
            case PHOTO_OTP -> getPhotoOtp(challengeData, authenticationObject, explanationField);
            case CHIP_OTP -> createFormWithChallengeForChipOtp(challengeData, authenticationObject, explanationField);
        };
    }

    private static Form getGeneralTextFieldChallengeForm(AuthenticationObject authenticationObject, ExplanationField explanationField) {
        return new Form(singletonList(
                new TextField(CHALLENGE_DATA_FIELD_ID, authenticationObject.getName(), 0, 4096, false, false)), explanationField, null);
    }

    private static Form getPhotoOtp(ChallengeData challengeData, AuthenticationObject authenticationObject, ExplanationField explanationField) {
        return new Form(
                List.of(new ImageField(PHOTO_OTP_CHALLENGE_DATA_FIELD_ID, authenticationObject.getName(), Base64.encode(challengeData.getImage()), "image/png"),
                        new TextField(CHALLENGE_DATA_FIELD_ID, authenticationObject.getName(), 0, 4096, false, false)
                ), explanationField, null);
    }

    private static Form createFormWithChallengeForChipOtp(ChallengeData challengeData, AuthenticationObject authenticationObject, ExplanationField explanationField) {
        if (challengeData.getData() == null || challengeData.getData().isEmpty()) {
            return getGeneralTextFieldChallengeForm(authenticationObject, explanationField);
        } else {
            return new Form(
                    List.of(new FlickerCodeField(FLICKER_CODE_OTP_CHALLENGE_DATA_FIELD_ID, authenticationObject.getName(), challengeData.getData().get(0)),
                            new TextField(CHALLENGE_DATA_FIELD_ID, authenticationObject.getName(), 0, 4096, false, false)), explanationField, null);
        }
    }
}
