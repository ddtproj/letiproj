package com.example.simdashboard.dto;

public record ActivityStatsDto(
        String name,
        Integer count,
        Double avgDurationSec,
        Double minDurationSec,
        Double maxDurationSec
) {
}
