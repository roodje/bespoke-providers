package com.yolt.providers.stet.generic.dto;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

@ProjectedPayload
public interface PaginationDTO {

    @JsonPath("$.first.href")
    String getFirst();

    @JsonPath("$.prev.href")
    String getPrevious();

    @JsonPath("$.next.href")
    String getNext();

    @JsonPath("$.last.href")
    String getLast();

    @JsonPath("$.self.href")
    String getSelf();
}
