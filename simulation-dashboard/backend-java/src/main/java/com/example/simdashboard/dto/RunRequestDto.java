package com.example.simdashboard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RunRequestDto(
        @JsonProperty("specification_path")
        String specificationPath
) {
}
