package com.yolt.providers.common.service.authorization.form;

import com.yolt.providers.common.ais.form.EncryptionDetails;
import com.yolt.providers.common.domain.dynamic.step.FormStep;
import com.yolt.providers.deutschebank.common.service.authorization.form.DeutscheBankGroupAlphanumericFormStrategy;
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

class DeutscheBankGroupAlphanumericFormStrategyTest {

    private DeutscheBankGroupFormStrategy formStrategy;

    @BeforeEach
    void beforeEach() {
        formStrategy = new DeutscheBankGroupAlphanumericFormStrategy(Clock.systemUTC());
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
        assertThat(components).hasSize(1);

        TextField username = (TextField) form.getFormComponents().get(0);
        assertThat(username.getId()).isEqualTo("username");
        assertThat(username.getDisplayName()).isEqualTo("Username (self-defined or unique 2 to 59 alphanumeric characters)");
        assertThat(username.getLength()).isEqualTo(0);
        assertThat(username.getMaxLength()).isEqualTo(255);
        assertThat(username.getOptional()).isFalse();
    }

    @Test
    void getPsuIdFromValidFormTest() {
        // given
        FilledInUserSiteFormValues formValues = createFilledInUserSiteFormValues("username", "custom-username");

        // when
        String psuId = formStrategy.getPsuId(formValues);

        // then
        assertThat(psuId).isEqualTo("custom-username");
    }

    @Test
    void throwIllegalStateExceptionWhenGettingPsuIdFromInvalidFormTest() {
        // given
        FilledInUserSiteFormValues formValues = createFilledInUserSiteFormValues("invalid", "some-message");

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

    private FilledInUserSiteFormValues createFilledInUserSiteFormValues(String id, String value) {
        Map<String, String> valueMap = new HashMap<>(1);
        valueMap.put(id, value);

        FilledInUserSiteFormValues filledInUserSiteFormValues = new FilledInUserSiteFormValues();
        filledInUserSiteFormValues.setValueMap(valueMap);
        return filledInUserSiteFormValues;
    }
}
