package com.yolt.providers.common.service.authorization.form;

import com.yolt.providers.common.ais.form.EncryptionDetails;
import com.yolt.providers.common.domain.dynamic.step.FormStep;
import com.yolt.providers.deutschebank.common.service.authorization.form.DeutscheBankGroupFKDNFormStrategy;
import com.yolt.providers.deutschebank.common.service.authorization.form.DeutscheBankGroupFormStrategy;
import nl.ing.lovebird.providershared.form.FilledInUserSiteFormValues;
import nl.ing.lovebird.providershared.form.Form;
import nl.ing.lovebird.providershared.form.FormComponent;
import nl.ing.lovebird.providershared.form.TextField;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.time.temporal.ChronoUnit.HOURS;
import static org.assertj.core.api.Assertions.*;

class DeutscheBankGroupFKDNFormStrategyTest {

    private DeutscheBankGroupFormStrategy formStrategy;

    @BeforeEach
    void beforeEach() {
        formStrategy = new DeutscheBankGroupFKDNFormStrategy(Clock.systemUTC());
    }

    @Test
    void createFormTest() {
        // when
        FormStep formStep = formStrategy.createForm();

        // then
        assertThat(formStep.getEncryptionDetails()).isEqualTo(EncryptionDetails.noEncryption());
        assertThat(formStep.getTimeoutTime()).isCloseTo(Instant.now(), within(1, HOURS));
        assertThat(formStep.getProviderState()).isNull();

        Form form = formStep.getForm();
        assertThat(form).isNotNull();

        List<FormComponent> components = form.getFormComponents();
        assertThat(components).hasSize(2);

        TextField branchNumber = (TextField) form.getFormComponents().get(0);
        assertThat(branchNumber.getId()).isEqualTo("branch-number");
        assertThat(branchNumber.getDisplayName()).isEqualTo("Branch Number (three-digit)");
        assertThat(branchNumber.getLength()).isEqualTo(0);
        assertThat(branchNumber.getMaxLength()).isEqualTo(3);
        assertThat(branchNumber.getOptional()).isFalse();

        TextField accountNumber = (TextField) form.getFormComponents().get(1);
        assertThat(accountNumber.getId()).isEqualTo("account-number");
        assertThat(accountNumber.getDisplayName()).isEqualTo("Account Number (seven-digit, without sub-account number)");
        assertThat(accountNumber.getLength()).isEqualTo(0);
        assertThat(accountNumber.getMaxLength()).isEqualTo(7);
        assertThat(accountNumber.getOptional()).isFalse();
    }

    @Test
    void getPsuIdFromValidFormTest() {
        // given
        Map<String, String> valueMap = new HashMap<>();
        valueMap.put("branch-number", "123");
        valueMap.put("account-number", "1234567");

        FilledInUserSiteFormValues formValues = createFilledInUserSiteFormValues(valueMap);

        // when
        String psuId = formStrategy.getPsuId(formValues);

        // then
        assertThat(psuId).isEqualTo("1231234567");
    }

    @Test
    void throwIllegalStateExceptionWhenGettingPsuIdFromInvalidFormTest() {
        // given
        Map<String, String> valueMap = new HashMap<>();
        valueMap.put("wrong-number", "763");
        valueMap.put("error-number", "4335678");

        FilledInUserSiteFormValues formValues = createFilledInUserSiteFormValues(valueMap);

        // when
        ThrowableAssert.ThrowingCallable getPsuIdCallable = () ->
                formStrategy.getPsuId(formValues);

        // then
        assertThatThrownBy(getPsuIdCallable).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void throwNullPointerExceptionWhenGettingPsuIdFromNullableFormTest() {
        // given
        FilledInUserSiteFormValues formValues = null;

        // when
        ThrowableAssert.ThrowingCallable getPsuIdCallable = () ->
                formStrategy.getPsuId(formValues);

        // then
        assertThatThrownBy(getPsuIdCallable).isInstanceOf(NullPointerException.class);
    }

    private FilledInUserSiteFormValues createFilledInUserSiteFormValues(Map<String, String> valueMap) {
        FilledInUserSiteFormValues filledInUserSiteFormValues = new FilledInUserSiteFormValues();
        filledInUserSiteFormValues.setValueMap(valueMap);
        return filledInUserSiteFormValues;
    }
}
