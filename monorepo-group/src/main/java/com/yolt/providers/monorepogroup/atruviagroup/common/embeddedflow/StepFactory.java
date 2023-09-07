package com.yolt.providers.monorepogroup.atruviagroup.common.embeddedflow;

import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.FormStep;
import com.yolt.providers.monorepogroup.atruviagroup.common.authenticationmeans.AtruviaGroupAuthenticationMeansFactory;
import com.yolt.providers.monorepogroup.atruviagroup.common.dto.internal.StepState;
import com.yolt.providers.monorepogroup.atruviagroup.common.mapper.AtruviaGroupProviderStateMapper;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providershared.AccessMeansDTO;

import java.sql.Date;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@RequiredArgsConstructor
public class StepFactory {

    private static final Duration DURATION = Duration.ofHours(1L);

    private final Clock clock;
    private final AtruviaGroupProviderStateMapper atruviaGroupProviderStateMapper;
    private final AtruviaGroupAuthenticationMeansFactory atruviaGroupAuthenticationMeansFactory;

    public ProcessStepData createProcessStepData(UrlCreateAccessMeansRequest request) {
        var stepState = atruviaGroupProviderStateMapper.fromJson(request.getProviderState(), StepState.class);
        var atruviaGroupAuthenticationMeans = atruviaGroupAuthenticationMeansFactory.toAuthenticationMeans(request.getAuthenticationMeans());
        return new ProcessStepData(request.getProviderState(), request.getFilledInUserSiteFormValues(), request.getRestTemplateManager(),
                request.getPsuIpAddress(), request.getSigner(), atruviaGroupAuthenticationMeans, request.getUserId(), stepState);
    }

    public FormStep toStep(StepOutcome outcome) {
        if (outcome instanceof FormStepOutcome formStepOutcome) {
            var timeoutTime = Instant.now(clock).plus(DURATION);
            return new FormStep(formStepOutcome.getForm(), formStepOutcome.getEncryptionDetails(), timeoutTime, atruviaGroupProviderStateMapper.toJson(formStepOutcome.getStepState()));
        }
        throw new IllegalStateException("Invalid Step Outcome!");
    }

    public AccessMeansOrStepDTO toAccessMeansOrStepDTO(StepOutcome outcome) {
        if (outcome instanceof FormStepOutcome formStepOutcome) {
            var timeoutTime = Instant.now(clock).plus(DURATION);
            return new AccessMeansOrStepDTO(new FormStep(formStepOutcome.getForm(), formStepOutcome.getEncryptionDetails(), timeoutTime, atruviaGroupProviderStateMapper.toJson(formStepOutcome.getStepState())));
        } else if (outcome instanceof AccessMeansStepOutcome accessMeansStepOutcome) {
            var atruviaAccessMeans = atruviaGroupProviderStateMapper.toJson(accessMeansStepOutcome.getAtruviaAccessMeans());
            var accessMeansDto = new AccessMeansDTO(accessMeansStepOutcome.getUserId(), atruviaAccessMeans, Date.from(Instant.now(clock)), Date.from(Instant.now(clock).plus(accessMeansStepOutcome.getConsentValidity(), ChronoUnit.DAYS)));
            return new AccessMeansOrStepDTO(accessMeansDto);
        }
        throw new IllegalStateException("Invalid Step Outcome!");
    }
}
