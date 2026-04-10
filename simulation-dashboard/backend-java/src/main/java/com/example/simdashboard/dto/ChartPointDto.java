package com.example.simdashboard.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ChartPointDto(
        String name,
        Number value,
        String binLabel,
        @JsonProperty("from")
        Double from,
        Double to,
        Integer count
) {
}
