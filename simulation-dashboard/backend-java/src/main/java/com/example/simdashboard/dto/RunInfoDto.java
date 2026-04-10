package com.example.simdashboard.dto;

public record RunInfoDto(
        String runId,
        String status,
        boolean hasResult
) {
}
