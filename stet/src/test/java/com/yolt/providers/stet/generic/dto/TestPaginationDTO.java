package com.yolt.providers.stet.generic.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TestPaginationDTO implements PaginationDTO {
    
    private String first;
    private String previous;
    private String next;
    private String last;
    private String self;
}
