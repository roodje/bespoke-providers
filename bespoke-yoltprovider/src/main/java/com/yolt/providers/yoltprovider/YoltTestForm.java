package com.yolt.providers.yoltprovider;

import lombok.experimental.UtilityClass;
import nl.ing.lovebird.providershared.form.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * A form for testing purposes with many components / fields / options present.  Much more elaborate than anything we'd ever see in reality.
 */
@UtilityClass
class YoltTestForm {

    Form createFormWithAllComponents() {
        final Form form = new Form();
        form.setExplanationField(new ExplanationField("explanation-field-id", "Explanation field.", "Please fill out this form."));

        SectionFormComponent choiceComponents = new SectionFormComponent("section-choice-components", "Choice components", false);
        choiceComponents.addComponent(requiredChoiceFormComponent());
        choiceComponents.addComponent(optionalChoiceFormComponent());

        SectionFormComponent multiLineComponents = new SectionFormComponent("section-multiline-components", "Multiline components", false);
        multiLineComponents.addComponent(multiLineFormComponent());

        SectionFormComponent dateFields = new SectionFormComponent("section-date-fields", "Date fields", false);
        dateFields().forEach(dateFields::addComponent);

        SectionFormComponent numberFields = new SectionFormComponent("section-number-fields", "Number fields", false);
        numberFields().forEach(numberFields::addComponent);

        SectionFormComponent radioCheckFields = new SectionFormComponent("section-radio-check-fields", "Radiobuttons and checkboxes", false);
        radioFields().forEach(radioCheckFields::addComponent);
        checkFields().forEach(radioCheckFields::addComponent);

        SectionFormComponent textFields = new SectionFormComponent("text-fields", "Text fields", false);
        textFields().forEach(textFields::addComponent);

        SectionFormComponent miscFields = new SectionFormComponent("section-miscellaneous-fields", "Miscellaneous fields", false);
        miscFields.addComponent(new IbanField("iban-field", "IBAN", true));
        miscFields.addComponent(new PasswordField("password-field", "Password", 10, 10, true, null));
        miscFields.addComponent(new TextAreaField("text-area", "Text area", 100, true, 5));

        form.setFormComponents(asList(
                choiceComponents,
                multiLineComponents,
                dateFields,
                numberFields,
                radioCheckFields,
                textFields,
                miscFields
        ));

        return form;
    }

    /**
     * Choice component of which exactly 1 field must be filled in.
     */
    ChoiceFormComponent requiredChoiceFormComponent() {
        final ChoiceFormComponent requiredChoiceFormComponent = new ChoiceFormComponent(
                "required-choice-form-component",
                "Fill in exactly 1 of these fields.",
                false
        );
        requiredChoiceFormComponent.addComponent(new TextField("required-choice-form-component-a", "A", 1, 1, true, null, false));
        requiredChoiceFormComponent.addComponent(new TextField("required-choice-form-component-b", "B", 1, 1, true, null, false));
        return requiredChoiceFormComponent;
    }

    /**
     * Choice component of which at most 1 field must be filled in.
     */
    ChoiceFormComponent optionalChoiceFormComponent() {
        final ChoiceFormComponent requiredChoiceFormComponent = new ChoiceFormComponent(
                "optional-choice-form-component",
                "Fill in at most 1 of these fields.",
                true
        );
        requiredChoiceFormComponent.addComponent(new TextField("optional-choice-form-component-a", "A", 1, 1, true, null, false));
        requiredChoiceFormComponent.addComponent(new TextField("optional-choice-form-component-b", "B", 1, 1, true, null, false));
        return requiredChoiceFormComponent;
    }

    MultiFormComponent multiLineFormComponent() {
        MultiFormComponent multiFormComponent = new MultiFormComponent(
                "multi-form-component",
                "Display A and B on single line.",
                true
        );
        multiFormComponent.addComponent(new TextField("multi-form-component-a", "A", 1, 1, true, null, false));
        multiFormComponent.addComponent(new TextField("multi-form-component-b", "B", 1, 1, true, null, false));
        return multiFormComponent;
    }

    Collection<DateField> dateFields() {
        final String isoDateFormat = "yyyy-MM-dd";

        final DateField date = new DateField(
                "required-date-field",
                "Date",
                false,
                isoDateFormat,
                false
        );

        final DateField dateAfter2000 = new DateField(
                "date-field-after-2000",
                "Date after 2000-01-01",
                true,
                isoDateFormat,
                false
        );
        dateAfter2000.setMinDate(LocalDate.of(2000, 1, 1));

        final DateField dateBefore2000 = new DateField(
                "date-field-before-2000",
                "Date before 2000-01-01",
                true,
                isoDateFormat,
                false
        );
        dateBefore2000.setMaxDate(LocalDate.of(2000, 1, 1));

        final DateField dateInDutchFormat = new DateField(
                "date-field-dd-MM-yyyy",
                "Date as dd-MM-yyyy",
                true,
                "dd-MM-yyyy",
                false
        );

        return asList(
                date,
                dateAfter2000,
                dateBefore2000,
                dateInDutchFormat
        );
    }

    Collection<NumberField> numberFields() {

        NumberField numberGt100 = new NumberField(
                "number-field-gt-100",
                "Number > 100",
                true,
                BigDecimal.valueOf(100L),
                BigDecimal.valueOf(1000000L),
                new BigDecimal("0.01"),
                false
        );

        NumberField numberLt100 = new NumberField(
                "number-field-lt-100",
                "Number < 100",
                true,
                BigDecimal.valueOf(-1000000L),
                BigDecimal.valueOf(100L),
                new BigDecimal("0.01"),
                false
        );

        NumberField wholeNumber = new NumberField(
                "number-field-gt-100",
                "Whole number",
                true,
                BigDecimal.valueOf(-100000L),
                BigDecimal.valueOf(100000L),
                BigDecimal.ONE,
                false
        );

        NumberField numberDefault10 = new NumberField(
                "number-field-with-default",
                "Default 10",
                true,
                BigDecimal.ONE,
                BigDecimal.valueOf(100L),
                new BigDecimal("0.01"),
                false
        );
        numberDefault10.setDefaultValue(BigDecimal.valueOf(10L));

        return asList(
                numberGt100,
                numberLt100,
                wholeNumber,
                numberDefault10
        );
    }

    Collection<RadioField> radioFields() {
        RadioField radioField = new RadioField("radio-field", "Radio", true);
        radioField.addSelectOptionValue(new SelectOptionValue("A", "A"));
        radioField.addSelectOptionValue(new SelectOptionValue("B", "B"));

        RadioField radioFieldWithDefault = new RadioField("radio-field-with-default", "Radio with default", true);
        final SelectOptionValue defaultValue = new SelectOptionValue("A", "A");
        radioFieldWithDefault.addSelectOptionValue(defaultValue);
        radioFieldWithDefault.addSelectOptionValue(new SelectOptionValue("B", "B"));
        radioFieldWithDefault.setDefaultValue(defaultValue);

        return asList(
                radioField,
                radioFieldWithDefault
        );
    }

    Collection<SelectField> checkFields() {
        SelectField selectField = new SelectField("select-field", "Select", 5, 5, true);
        selectField.addSelectOptionValue(new SelectOptionValue("A", "A"));
        selectField.addSelectOptionValue(new SelectOptionValue("B", "B"));

        SelectField selectFieldWithDefault = new SelectField("select-field-with-default", "Select with default", 5, 5, true);
        final SelectOptionValue defaultValue = new SelectOptionValue("A", "A");
        selectFieldWithDefault.addSelectOptionValue(defaultValue);
        selectFieldWithDefault.addSelectOptionValue(new SelectOptionValue("B", "B"));
        selectFieldWithDefault.setDefaultValue(defaultValue);

        return asList(
                selectField,
                selectFieldWithDefault
        );
    }

    Collection<TextField> textFields() {
        TextField textField = new TextField(
                "text-field",
                "Text",
                5,
                5,
                true,
                null,
                false
        );

        TextField textFieldOnlyLetters = new TextField(
                "text-field-only-lowercase-az",
                "Only [a-z]",
                5,
                5,
                true,
                "[a-z]*",
                false);

        TextField textFieldOnlyNumbers = new TextField(
                "text-field-only-numbers",
                "Only [a-z]",
                5,
                5,
                true,
                "[0-9]*",
                false);

        return asList(
                textField,
                textFieldOnlyLetters,
                textFieldOnlyNumbers
        );
    }

    public static Form simpleForm() {
        final Form form = new Form();
        form.setExplanationField(new ExplanationField("explanation-field-id", "Explanation field.", "A simple form."));

        form.setFormComponents(List.of(
                EmailForm.EMAIL_TEXT_FIELD
        ));

        return form;
    }
}
