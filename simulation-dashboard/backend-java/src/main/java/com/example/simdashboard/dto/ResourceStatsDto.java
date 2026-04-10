package com.example.simdashboard.dto;

public record ResourceStatsDto(
        String name,
        Integer blockCount,
        Integer availableCount,
        Double observedWorkTimeSec,
        Double utilizationPercent
) {
}
