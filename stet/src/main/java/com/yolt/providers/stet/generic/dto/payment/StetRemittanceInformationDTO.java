package com.yolt.providers.stet.generic.dto.payment;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class StetRemittanceInformationDTO {

  private List<String> unstructured;
}
